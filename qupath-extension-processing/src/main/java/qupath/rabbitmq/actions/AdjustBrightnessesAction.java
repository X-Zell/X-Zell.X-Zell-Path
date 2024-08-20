package qupath.rabbitmq.actions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import qupath.rabbitmq.ActionLogger;
import qupath.rabbitmq.data.AntibodyInfo;
import qupath.rabbitmq.data.AntibodyInfoPayload;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.scripting.QPEx;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageChannel;
import qupath.lib.projects.Project;
import qupath.lib.projects.ProjectImageEntry;
import qupath.lib.scripting.QP;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static qupath.lib.gui.scripting.QPEx.getCurrentViewer;
import static qupath.lib.gui.scripting.QPEx.setChannelDisplayRange;
import static qupath.lib.scripting.QP.*;

public class AdjustBrightnessesAction implements IQuPathExtensionAction {
    private final String antibodyType;

    private AntibodyInfo[] antibodyInfos;
    private QuPathGUI qupath;
    private ActionLogger logger;
    private String actionId = "";
    private final Consumer<Runnable> runLater;
    private final QPExWrapper qpExWrapper;

    public AdjustBrightnessesAction(String antibodyType, String antibodyInfoPayloadJson, QuPathGUI qupath, ActionLogger logger) {
        this(antibodyType, antibodyInfoPayloadJson, qupath, logger, Platform::runLater, new QPExWrapperImpl());
    }

    public AdjustBrightnessesAction(String antibodyType, String antibodyInfoPayloadJson, QuPathGUI qupath, ActionLogger logger, Consumer<Runnable> runLater, QPExWrapper qpExWrapper) {
        this.antibodyType = antibodyType;

        Gson gson = new GsonBuilder().create();
        AntibodyInfoPayload antibodyInfoPayload = gson.fromJson(antibodyInfoPayloadJson, AntibodyInfoPayload.class);
        antibodyInfos = antibodyInfoPayload.AntibodyInfos;

        logger.logInfo("Number of Antibody Infos: " + antibodyInfoPayload.AntibodyInfos.length, actionId);

        this.qupath = qupath;
        this.logger = logger;
        this.actionId = "AdjustBrightnessesAction(" + this.antibodyType + ")";

        this.runLater = runLater;
        this.qpExWrapper = qpExWrapper;
    }

    public void runAction() {
        try {
            this.logger.logInfo("Commencing setting of Brightnesses", actionId);
            QuPathViewer viewer = qupath.getViewer();
            ImageData<BufferedImage> imageDataCurrent = viewer.getImageData();
            imageDataCurrent.setImageType(ImageData.ImageType.FLUORESCENCE);
            this.logger.logInfo("ImageType set to FLUORESCENCE", actionId);
            imageDataCurrent.removeProperty("qupath.lib.display.ImageDisplay");
            this.logger.logInfo("qupath.lib.display.ImageDisplay property removed", actionId);

            String[] names = Arrays.stream(antibodyInfos)
                    .map(AntibodyInfo::getAntibody)
                    .toArray(String[]::new);

            this.logger.logInfo("Setting " + names.length + " channel names", actionId);

            for (int i = 0; i < names.length; i++) {
                this.logger.logInfo("Name " + (i + 1) + ": " + names[i], this.actionId);
            }
            qpExWrapper.setChannelNames(imageDataCurrent, names);
            this.logger.logInfo("Channel names set", this.actionId);

            CountDownLatch latch = new CountDownLatch(1);

//            ActionLogger logger = this.logger;

            runInnerAction(latch, imageDataCurrent);

            // Wait for the JavaFX Application Thread work to complete
            latch.await();
        } catch (Exception e) {
            logger.logErrorWithStackTrace("QuPath Adjust Brightnesses Exception - " + e.getMessage(), e.getStackTrace(), actionId);
            JOptionPane.showMessageDialog(null, "QuPath Adjust Brightnesses Exception - " + e.getMessage(), "X-Zell: RabbitMQ", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void runInnerAction(CountDownLatch latch, ImageData<BufferedImage> imageDataCurrent) {
        if (!Platform.isFxApplicationThread()) {
            logger.logInfo("Passing to Application thread", this.actionId);
            Platform.runLater(() -> runInnerAction(latch, imageDataCurrent));
            return;
        }
        try {
            Integer[] colors = Arrays.stream(antibodyInfos)
                    .map(AntibodyInfo::getColorCode)
                    .map(AdjustBrightnessesAction::getRgbFromColorCode)
                    .toArray(Integer[]::new);
            setChannelColors(imageDataCurrent, colors);
            logger.logInfo("Channel colors set", actionId);
            logger.logInfo("Commencing setting of channel display ranges", actionId);
            for (int i = 0; i < antibodyInfos.length; i++) {
                setChannelDisplayRange(imageDataCurrent, i, antibodyInfos[i].BrightnessMin, antibodyInfos[i].BrightnessMax);
                logger.logInfo("Channel " + i + " display range set to range " + antibodyInfos[i].BrightnessMin + " to " + antibodyInfos[i].BrightnessMax, actionId);
            }
            logger.logInfo("All channel display ranges set", actionId);

            Project project = qupath.getProject();

            qupath.refreshProject();

            logger.logInfo("Current Image Data server path:" + imageDataCurrent.getServerPath(), actionId);


            List<ProjectImageEntry> imageEntries = (List<ProjectImageEntry>)project.getImageList();
            if (!imageEntries.isEmpty()) {
                var numberOfEntries = imageEntries.size();
                logger.logInfo("Number of Entries in Project: " + numberOfEntries, actionId);
                ProjectImageEntry latestImageEntry = imageEntries.get(numberOfEntries - 1);
                latestImageEntry.saveImageData(imageDataCurrent);
//                for (int i = 0; i < numberOfEntries; i++) {
//                    ProjectImageEntry imageEntry = imageEntries.get(i);
//                    logger.logInfo("Saving image " + (i + 1) + " of " + numberOfEntries, actionId);
//                    imageEntry.saveImageData(imageDataCurrent);
//                }
            } else {
                logger.logError("No Entries found for Project: ", actionId);
            }
//                    viewer.repaintEntireImage();

            project.syncChanges();
            logger.logInfo("Project synced", actionId);
        } catch (Exception e) {
            logger.logErrorWithStackTrace("QuPath Adjust Brightnesses Inner Exception - " + e.getMessage(), e.getStackTrace(), actionId);
            JOptionPane.showMessageDialog(null, "QuPath Adjust Brightnesses Inner Exception: " + e.getMessage(), "X-Zell: RabbitMQ", JOptionPane.ERROR_MESSAGE);
        } finally {
            latch.countDown(); // Signal that work is done
        }
    }

    public static Integer getRgbFromColorCode(String colorCode_) {
        // logger.logInfo("Color Code: " + colorCode_, actionId);

        // Remove the "#" character from the color code
        var colorCode = colorCode_.substring(1);

        // Convert the hex values to decimal values
        int red = Integer.parseInt(colorCode.substring(0, 2), 16);
        int green = Integer.parseInt(colorCode.substring(2, 4), 16);
        int blue = Integer.parseInt(colorCode.substring(4, 6), 16);

        return makeRGB(red, green, blue);
    }
}
