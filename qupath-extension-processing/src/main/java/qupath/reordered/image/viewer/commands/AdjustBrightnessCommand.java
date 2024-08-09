package qupath.reordered.image.viewer.commands;

import javafx.application.Platform;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.gui.viewer.QuPathViewerPlus;
import qupath.lib.images.ImageData;
import qupath.lib.projects.Project;
import qupath.lib.projects.ProjectImageEntry;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static qupath.lib.gui.scripting.QPEx.getCurrentViewer;
import static qupath.lib.scripting.QP.makeRGB;

public class AdjustBrightnessCommand implements Runnable {

    private AntibodyInfo[] antibodyInfos;
    private QuPathGUI qupath;
    private final Consumer<Runnable> runLater;
    private final QPExWrapper qpExWrapper;

    public AdjustBrightnessCommand() {
        antibodyInfos = new AntibodyInfo[] {
        new AntibodyInfo(1, "AF594","CK7","#FF9900",100,2000),
        new AntibodyInfo(2, "PE","Ki-67","#FFFF00",100,1500),
        new AntibodyInfo(3, "AF488", "TTF1", "#00FF19",100,1000),
        new AntibodyInfo(4, "BV480", "CD45", "#00FFFF",100,1400),
        new AntibodyInfo(5, "BV421", "CD56", "#0000FF",100,3000),
        new AntibodyInfo(6, "DRAQ5", "Nucleus", "#FF0000",100,1200),
        new AntibodyInfo(7, "PerCp", "EpCAM", "#FF33FF",100,1200),
        new AntibodyInfo(8, "AF790", "Calretinin", "#FFC0CB",100,1200)
        };

        this.qupath = QuPathGUI.getInstance();
        this.runLater = Platform::runLater;
        this.qpExWrapper = new QPExWrapperImpl();
    }

    @Override
    public void run() {
        try {
            QuPathViewer viewer = getCurrentViewer();
            ImageData<BufferedImage> imageDataCurrent = viewer.getImageData();
            imageDataCurrent.setImageType(ImageData.ImageType.FLUORESCENCE);
//            this.logger.logInfo("ImageType set to FLUORESCENCE", actionId);
            imageDataCurrent.removeProperty("qupath.lib.display.ImageDisplay");
//            this.logger.logInfo("qupath.lib.display.ImageDisplay property removed", actionId);

            String[] names = Arrays.stream(antibodyInfos)
                    .map(AntibodyInfo::getAntibody)
                    .toArray(String[]::new);

//            this.logger.logInfo("Setting " + names.length + " channel names", actionId);

//            for (int i = 0; i < names.length; i++) {
//                this.logger.logInfo("Name " + (i + 1) + ": " + names[i], this.actionId);
//            }
            qpExWrapper.setChannelNames(imageDataCurrent, names);
//            this.logger.logInfo("Channel names set", this.actionId);

            CountDownLatch latch = new CountDownLatch(1);

//            ActionLogger logger = this.logger;

            runLater.accept(() -> {
                try {
                    Integer[] colors = Arrays.stream(antibodyInfos)
                            .map(AntibodyInfo::getColorCode)
                            .map(AdjustBrightnessCommand::getRgbFromColorCode)
                            .toArray(Integer[]::new);
                    qpExWrapper.setChannelColors(imageDataCurrent, colors);
//                    logger.logInfo("Channel colors set", actionId);
//                    logger.logInfo("Commencing setting of channel display ranges", actionId);
                    for (int i = 0; i < antibodyInfos.length; i++) {
                        qpExWrapper.setChannelDisplayRange(imageDataCurrent, i, antibodyInfos[i].BrightnessMin, antibodyInfos[i].BrightnessMax);
//                        logger.logInfo("Channel " + i + " display range set to range " + antibodyInfos[i].BrightnessMin + " to " + antibodyInfos[i].BrightnessMax, actionId);
                    }
//                    logger.logInfo("All channel display ranges set", actionId);

                    Project project = QuPathGUI.getInstance().getProject();

                    qupath.refreshProject();

//                    logger.logInfo("Current Image Data server path:" + imageDataCurrent.getServerPath(), actionId);


                    List<ProjectImageEntry> imageEntries = (List<ProjectImageEntry>)project.getImageList();
                    if (!imageEntries.isEmpty()) {
                        var numberOfEntries = imageEntries.size();
//                        logger.logInfo("Number of Entries in Project: " + numberOfEntries, actionId);
                        ProjectImageEntry latestImageEntry = imageEntries.get(numberOfEntries - 1);
                        latestImageEntry.saveImageData(imageDataCurrent);
                    } else {
//                        logger.logError("No Entries found for Project: ", actionId);
                    }
    //                    viewer.repaintEntireImage();

                    project.syncChanges();
//                    logger.logInfo("Project synced", actionId);
                } catch (Exception e) {
//                    logger.logErrorWithStackTrace("QuPath Adjust Brightnesses Inner Exception - " + e.getMessage(), e.getStackTrace(), actionId);
                    JOptionPane.showMessageDialog(null, "QuPath Adjust Brightnesses Inner Exception: " + e.getMessage(), "X-Zell: RabbitMQ", JOptionPane.ERROR_MESSAGE);
                } finally {
                    latch.countDown(); // Signal that work is done
                }
            });

            // Wait for the JavaFX Application Thread work to complete
            latch.await();
        } catch (Exception e) {
//            logger.logErrorWithStackTrace("QuPath Adjust Brightnesses Exception - " + e.getMessage(), e.getStackTrace(), actionId);
            JOptionPane.showMessageDialog(null, "QuPath Adjust Brightnesses Exception - " + e.getMessage(), "X-Zell: RabbitMQ", JOptionPane.ERROR_MESSAGE);
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
