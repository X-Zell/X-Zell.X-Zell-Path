package qupath.rabbitmq.actions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import qupath.rabbitmq.ActionLogger;
import qupath.rabbitmq.data.AddImagePayload;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.ProjectCommands;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.*;
import qupath.lib.projects.Project;
import qupath.lib.projects.ProjectImageEntry;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import java.util.concurrent.CountDownLatch;

import static qupath.lib.gui.scripting.QPEx.getCurrentViewer;

public class AddProjectImageAction implements IQuPathExtensionAction {

    private String IMAGE_FILENAME_ENDING = "_MMStack_Pos0.ome.tif";
    private String imagesPath;
    private String projectsPath;
    private String timestampedImageName;
    private String antibodyType;
    private QuPathGUI qupath;
    private ActionLogger logger;
    private String actionId = "AddProjectImageAction";

    private volatile ImageData<BufferedImage> imageData;

    public AddProjectImageAction(String addImagePayloadJson, QuPathGUI qupath, ActionLogger logger) {

        Gson gson = new GsonBuilder().create();
        AddImagePayload addImagePayload = gson.fromJson(addImagePayloadJson, AddImagePayload.class);

        this.imagesPath = addImagePayload.ImagesPath;
        this.projectsPath = addImagePayload.ProjectsPath;
        this.timestampedImageName = addImagePayload.TimestampedImageName;
        this.antibodyType = addImagePayload.AntibodyType;
        logger.logInfo("Add Image Payload:", this.actionId);
        logger.logInfo("    ImagesPath: " + this.imagesPath, this.actionId);
        logger.logInfo("    ProjectsPath: " + this.projectsPath, this.actionId);
        logger.logInfo("    AntibodyType: " + this.antibodyType, this.actionId);
        logger.logInfo("    TimestampedImageName: " + this.timestampedImageName, this.actionId);
        this.qupath = qupath;
        this.logger = logger;
    }

    public void runAction() {
        logger.logInfo("-------------------------------------", this.actionId);
        logger.logInfo("Commencing adding file to the project", this.actionId);
        try {
            String absoluteImageFileName = imagesPath + timestampedImageName + "\\" + timestampedImageName + IMAGE_FILENAME_ENDING;
            //logger.logInfo("AbsoluteImageFileName: " + absoluteImageFileName, actionId);

            CountDownLatch latch = new CountDownLatch(1);

            runInnerAction(latch, absoluteImageFileName);

            // Wait for the JavaFX Application Thread work to complete
            latch.await();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "QuPath Project (" + timestampedImageName + ") Add Image  Exception - " + e.getMessage(), "X-Zell: RabbitMQ", JOptionPane.ERROR_MESSAGE);
        }

    }

    private void runInnerAction(CountDownLatch latch, String absoluteImageFileName) {
        if (!Platform.isFxApplicationThread()) {
            logger.logInfo("Passing to Application thread", this.actionId);
            Platform.runLater(() -> runInnerAction(latch, absoluteImageFileName));
            return;
        }

        try {

            Project<BufferedImage> project = qupath.getProject();
            logger.logInfo("Project: " + project.getName(), actionId);
            ImageServerBuilder.UriImageSupport<BufferedImage> support = ImageServerProvider.getPreferredUriImageSupport(BufferedImage.class, absoluteImageFileName);
            logger.logInfo("Support created for : " + absoluteImageFileName, actionId);
            List<ImageServerBuilder.ServerBuilder<BufferedImage>> builders = support.getBuilders();
            ImageServerBuilder.ServerBuilder<BufferedImage> builder = builders.get(0);
            logger.logInfo("Builder: " + builder.toString(), actionId);
            ProjectImageEntry<BufferedImage> entry = project.addImage(builder);
            logger.logInfo("Entry: " + entry.toString(), actionId);

            try {
                imageData = entry.readImageData();
            } catch (IOException e) {
                logger.logError("Unable to read ImageData for " + entry.getImageName(), actionId);
                return;
            }
            imageData.setImageType(ImageData.ImageType.FLUORESCENCE);
            //logger.logInfo("Image type set to FLUORESCENCE", actionId);

            logger.logInfo("Commence writing of thumbnail", actionId);

            BufferedImage img = null;
            synchronized (imageData) {
                var server = imageData.getServer();
                BufferedImage defaultThumbnail = null;
                int retryCount = 3;
                while (defaultThumbnail == null && retryCount > 0) {
                    try {
                        defaultThumbnail = server.getDefaultThumbnail(server.nZSlices() / 2, 0);
                        if (defaultThumbnail == null) {
                            logger.logError("Thumbnail generation returned null, retrying...", actionId);
                            Thread.sleep(200); // wait a bit before retrying
                        }
                    } catch (Exception e) {
                        logger.logError("Error during thumbnail generation: " + e.getMessage(), actionId);
                        JOptionPane.showMessageDialog(null, "Error during thumbnail generation: " + e.getMessage(), "X-Zell: Add Image", JOptionPane.ERROR_MESSAGE);
                    }
                    retryCount--;
                }

                img = ProjectCommands.getThumbnailRGB(imageData.getServer());
            }
            logger.logInfo("Thumbnail is " + img.getWidth() + " x " + img.getHeight(), actionId);
            entry.setThumbnail(img);
            logger.logInfo("Thumbnail applied", actionId);

            logger.logInfo("Setting image name to " + timestampedImageName, actionId);
            entry.setImageName(timestampedImageName);

            logger.logInfo("Commence saving of image data", actionId);


            synchronized (imageData) {
                entry.saveImageData(imageData);
            }
            if (entry.hasImageData()) {
                logger.logInfo("Image data saved", actionId);
            } else {
                logger.logError("Image data not saved", actionId);
            }

            qupath.refreshProject();

            if (entry.hasImageData()) {
                logger.logInfo("Reconfirming Image data saved", actionId);
            } else {
                logger.logError("Reconfirming Image data not saved", actionId);
            }

            Project guiProject = QuPathGUI.getInstance().getProject();
            logger.logInfo("QuPath project obtained", actionId);

            QuPathViewer viewer = getCurrentViewer();
            logger.logInfo("QuPath viewer obtained", actionId);

            synchronized (imageData) {
                viewer.setImageData(imageData);
            }
            logger.logInfo("QuPath viewer set with image data", actionId);
            qupath.refreshProject();

            ImageData<BufferedImage> imageDataCurrent = viewer.getImageData();
            if (imageDataCurrent != null) {
                logger.logInfo("QuPath current image data successfully found", actionId);
                List<ProjectImageEntry> imageEntries = (List<ProjectImageEntry>) guiProject.getImageList();
                if (!imageEntries.isEmpty()) {
                    logger.logInfo("QuPath Project image entries successfully found", actionId);
                    ProjectImageEntry latestImageEntry = imageEntries.get(imageEntries.size() - 1);
                    if (latestImageEntry != null) {
                        logger.logInfo("Latest image entry successfully found", actionId);
                        latestImageEntry.saveImageData(imageDataCurrent);
                    } else {
                        logger.logError("Latest image entry not found", actionId);
                    }
                    // ...
                } else {
                    logger.logError("QuPath Project has no image entries", actionId);
                }
            } else {
                logger.logError("QuPath current image data not found", actionId);
            }

            guiProject.syncChanges();
        } catch (Exception e) {
            logger.logErrorWithStackTrace("QuPath Project with Added Image Exception - " + e.getMessage(), e.getStackTrace(), actionId);
            JOptionPane.showMessageDialog(null, "QuPath Project Add Image Inner Exception - " + e.getMessage(), "X-Zell: RabbitMQ", JOptionPane.ERROR_MESSAGE);
        } finally {
            latch.countDown(); // Signal that work is done
        }
    }
}
