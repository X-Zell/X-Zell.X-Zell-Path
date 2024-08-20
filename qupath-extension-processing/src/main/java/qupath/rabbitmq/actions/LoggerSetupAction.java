package qupath.rabbitmq.actions;

import qupath.rabbitmq.ActionLogger;
//import qupath.rabbitmq.actions.IQuPathExtensionAction;

import javax.swing.*;

public class LoggerSetupAction implements IQuPathExtensionAction {
    private ActionLogger logger;
    private String logFilePath;

    public LoggerSetupAction(ActionLogger logger, String logFilePath) {
        try {
            this.logger = logger;
            this.logFilePath = logFilePath;
            this.logger.logInfo("Logger setup to use " + this.logFilePath, "LoggerSetupAction");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "QuPath Logger Setup Exception - " + e.getMessage(), "X-Zell: Qupath Extension", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void runAction() {
        logger.setFilePath(this.logFilePath);
    }
}
