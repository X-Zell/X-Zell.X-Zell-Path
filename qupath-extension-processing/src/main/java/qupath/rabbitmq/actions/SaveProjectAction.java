package qupath.rabbitmq.actions;

import qupath.rabbitmq.ActionLogger;
//import qupath.rabbitmq.actions.IQuPathExtensionAction;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.images.ImageData;
import qupath.lib.projects.Project;
import qupath.lib.projects.ProjectImageEntry;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static qupath.lib.gui.scripting.QPEx.getCurrentViewer;
import static qupath.lib.scripting.QP.getProjectEntry;

public class SaveProjectAction implements IQuPathExtensionAction {
    private QuPathGUI qupath;
    private ActionLogger logger;
    private String actionId = "SaveProjectAction";

    public SaveProjectAction(QuPathGUI qupath, ActionLogger logger) {
        this.qupath = qupath;
        this.logger = logger;
    }

    public void runAction() throws IOException {
        this.logger.logInfo("Commencing saving of QuPath Project", "SaveProjectAction");

        try {
            ProjectImageEntry<BufferedImage> imageEntry = getProjectEntry();
            QuPathViewer viewer = qupath.getViewer();
            ImageData<BufferedImage> imageDataCurrent = viewer.getImageData();
            if (imageDataCurrent != null) {
                logger.logInfo("Saving current image", actionId);
                imageEntry.saveImageData(imageDataCurrent);
            } else {
                logger.logError("Failed to load ImageData for current image", actionId);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "QuPath Save Project Exception - " + e.getMessage(), "X-Zell: RabbitMQ", JOptionPane.ERROR_MESSAGE);
        }
    }
}
