package qupath.rabbitmq.actions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import qupath.rabbitmq.ActionLogger;
import qupath.rabbitmq.data.AntibodyInfoPayload;
import qupath.rabbitmq.commands.ShowReorderedChannelViewerCommand;
import qupath.rabbitmq.data.ChannelOrderPayload;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.BrightnessContrastCommand;

import javax.swing.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class LaunchSubWindowAction implements IQuPathExtensionAction {

    private final String windowType;
    private QuPathGUI qupath;
    private ActionLogger logger;

    private String channelOrderPayloadJson;

    public LaunchSubWindowAction(String windowType, String channelOrderPayloadJson, QuPathGUI qupath, ActionLogger logger) {
        this.windowType = windowType;
        this.qupath = qupath;
        this.logger = logger;
        this.channelOrderPayloadJson = channelOrderPayloadJson;
    }

    public void runAction() {
        try {
            this.logger.logInfo("Commencing opening of Window", "LaunchSubWindowAction(" + this.windowType + ")");

            CountDownLatch latch = new CountDownLatch(1);

            if (windowType.equals("Contrast Window")) {
                runContrastWindowAction(latch);
            } else if (windowType.equals("Channel Viewer")) {
                Gson gson = new GsonBuilder().create();
                ChannelOrderPayload channelOrderPayload = gson.fromJson(this.channelOrderPayloadJson, ChannelOrderPayload.class);
                String[] orderedChannelNames = channelOrderPayload.OrderedChannelNames;

                runChannelViewerAction(latch, orderedChannelNames);
            }

            // Wait for the JavaFX Application Thread work to complete
            latch.await();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "QuPath Launch Sub Window Exception - " + e.getMessage(), "X-Zell: RabbitMQ", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void runContrastWindowAction(CountDownLatch latch) {
        if (!Platform.isFxApplicationThread()) {
            logger.logInfo("Passing to Application thread", "LaunchSubWindowAction(" + this.windowType + ")");
            Platform.runLater(() -> runContrastWindowAction(latch));
            return;
        }
        try {
            BrightnessContrastCommand bcCommand = new BrightnessContrastCommand(qupath);
            bcCommand.run();
        } finally {
            latch.countDown(); // Signal that work is done
        }
    }

    private void runChannelViewerAction(CountDownLatch latch, String[] orderedChannelNames) {
        if (!Platform.isFxApplicationThread()) {
            logger.logInfo("Passing to Application thread", "LaunchSubWindowAction(" + this.windowType + ")");
            Platform.runLater(() -> runChannelViewerAction(latch, orderedChannelNames));
            return;
        }
        try {
            new ShowReorderedChannelViewerCommand(orderedChannelNames).run();
        } finally {
            latch.countDown(); // Signal that work is done
        }
    }
}
