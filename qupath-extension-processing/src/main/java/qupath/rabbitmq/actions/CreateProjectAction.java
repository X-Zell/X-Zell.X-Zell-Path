package qupath.rabbitmq.actions;

import javafx.application.Platform;
import qupath.rabbitmq.ActionLogger;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.projects.Project;
import qupath.lib.projects.Projects;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.CountDownLatch;

public class CreateProjectAction implements IQuPathExtensionAction {

    private String projectFilePath;
    private QuPathGUI qupath;
    private ActionLogger logger;
    private String actionId = "CreateProjectAction";

    public CreateProjectAction(String projectFilePath, QuPathGUI qupath, ActionLogger logger) {

        try {
            this.projectFilePath = projectFilePath;
            this.qupath = qupath;
            this.logger = logger;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "QuPath Project Creation Exception - " + e.getMessage(), "X-Zell: Qupath Extension", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void runAction() {
        logger.logInfo("-------------------------------------", this.actionId);
        logger.logInfo("Commencing creation of new project", this.actionId);

        CountDownLatch latch = new CountDownLatch(1);

        runInnerAction(latch);

        // Wait for the JavaFX Application Thread work to complete
        try {
            latch.await();
            this.logger.logInfo("File added to the project", actionId);
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
            File file = new File(projectFilePath);
            if (!file.isDirectory()) {
                JOptionPane.showMessageDialog(null, "Failed to create project as " + projectFilePath + " is not a directory", "X-Zell: RabbitMQ", JOptionPane.ERROR_MESSAGE);
            }
            File[] filesInDirectory = file.listFiles(f -> !f.isHidden());
            if (filesInDirectory.length != 0) {
                JOptionPane.showMessageDialog(null, "Failed to create project as " + projectFilePath + " is not empty", "X-Zell: RabbitMQ", JOptionPane.ERROR_MESSAGE);
            }

            Project<BufferedImage> newProject = Projects.createProject(file, BufferedImage.class);
            qupath.setProject(newProject);
            newProject.syncChanges();
            qupath.refreshProject();

            Project<BufferedImage> project = qupath.getProject();
            logger.logInfo("Project: " + project.getName(), actionId);

            project.syncChanges();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to create project in " + projectFilePath + " - " + e.getMessage(), "X-Zell: RabbitMQ", JOptionPane.ERROR_MESSAGE);
        } finally {
            latch.countDown(); // Signal that work is done
        }
    }
}
