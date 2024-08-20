package qupath.rabbitmq;

import javax.swing.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActionLogger {

    private String logFilePath;
    private SimpleDateFormat dateFormatter;
    private String extensionVersionNumber;

    public ActionLogger(String extensionVersionNumber) {
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.extensionVersionNumber = extensionVersionNumber;
    }


    public void setFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
        this.logInfo("version " + this.extensionVersionNumber, "X-Zell QuPath Extension");
    }

    public void logInfo(String logMessage, String actionId) {
        if (this.logFilePath != null) {
            try {
                // Create the log file if it does not exist
                if (!Files.exists(Paths.get(logFilePath))) {
                    Files.createFile(Paths.get(logFilePath));
                }

                // Open the log file in append mode
                Files.write(Paths.get(logFilePath), (dateFormatter.format(new Date()) + " INFO " + actionId + " - " + logMessage + System.lineSeparator()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "QuPath Logger Exception - " + e.getMessage(), "X-Zell: Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void logError(String logMessage, String processName) {
        if (this.logFilePath != null) {
            try {
                // Create the log file if it does not exist
                if (!Files.exists(Paths.get(logFilePath))) {
                    Files.createFile(Paths.get(logFilePath));
                }

                // Open the log file in append mode
                Files.write(Paths.get(logFilePath), (dateFormatter.format(new Date()) + " ##ERROR " + processName + ": " + logMessage + System.lineSeparator()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "QuPath Logger Exception - " + e.getMessage(), "X-Zell: Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    public void logErrorWithStackTrace(String logMessage, StackTraceElement[] stackTraceElements, String processName) {
        logError(logMessage, processName);
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            // Construct the log message using the stack trace information
            String stackMessage = "Class: " + stackTraceElement.getClassName() +
                    ", Method: " + stackTraceElement.getMethodName() +
                    ", Line: " + stackTraceElement.getLineNumber();

            // Call the logInfo method on the logger with the log message
            logInfo(stackMessage, "StackTrace");
        }
    }
}
