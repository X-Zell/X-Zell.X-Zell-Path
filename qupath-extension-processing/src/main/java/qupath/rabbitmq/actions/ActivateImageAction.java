package qupath.rabbitmq.actions;

import javafx.application.Platform;
import qupath.rabbitmq.ActionLogger;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.projects.Project;
import qupath.lib.projects.ProjectImageEntry;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static qupath.lib.gui.scripting.QPEx.getCurrentViewer;

public class ActivateImageAction implements IQuPathExtensionAction {
    private static String TOOLTIP_PROP_KEY = "javafx.scene.control.Tooltip";
    private final QuPathGUI qupath;
    private ActionLogger logger;
    private String actionId = "ActivateImageAction";

    public ActivateImageAction(QuPathGUI qupath, ActionLogger logger) {
        this.qupath = qupath;
        this.logger = logger;
    }

    public void runAction() {
        this.logger.logInfo("Commencing", "ActivateImageAction");

        CountDownLatch latch = new CountDownLatch(1);

        runInnerAction(latch);

        // Wait for the JavaFX Application Thread work to complete
        try {
            latch.await();
            this.logger.logInfo("Image activation process completed", actionId);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void runInnerAction(CountDownLatch latch) {
        if (!Platform.isFxApplicationThread()) {
            logger.logInfo("Passing to Application thread", this.actionId);
            Platform.runLater(() -> runInnerAction(latch));
            return;
        }
        try {

            Project project = QuPathGUI.getInstance().getProject();
            List<ProjectImageEntry> imageEntries = (List<ProjectImageEntry>)project.getImageList();

            if (imageEntries.size() > 0) {
                var imageEntry = imageEntries.get(0);
                var imageData = imageEntry.readImageData();

                QuPathViewer viewer = getCurrentViewer();
                viewer.setImageData(imageData);
                qupath.refreshProject();
                this.logger.logInfo("Image activated", actionId);
            } else {
                this.logger.logInfo("No Image found for activation", actionId);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "QuPath Activate Image Exception - " + e.getMessage(), "X-Zell: RabbitMQ", JOptionPane.ERROR_MESSAGE);
        } finally {
            latch.countDown(); // Signal that work is done
        }
    }
}
