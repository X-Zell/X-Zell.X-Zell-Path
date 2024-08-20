package qupath.rabbitmq.commands;

import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import qupath.rabbitmq.ActionLogger;
import qupath.rabbitmq.actions.IQuPathExtensionAction;
import qupath.lib.gui.QuPathGUI;

import javax.swing.*;

public class ZoomToFitAction implements IQuPathExtensionAction {
    private static String TOOLTIP_PROP_KEY = "javafx.scene.control.Tooltip";
    private final QuPathGUI qupath;
    private ActionLogger logger;

    public ZoomToFitAction(QuPathGUI qupath, ActionLogger logger) {
        this.qupath = qupath;
        this.logger = logger;
    }

    public void runAction() {
        this.logger.logInfo("Commencing zoom to fit", "ZoomToFitAction");
        try {
            ToolBar toolbar = qupath.getToolBar();
            toolbar.getItems().forEach(item -> {
                Node node = item;
                if (node instanceof ToggleButton) {
                    ToggleButton toggleButton = (ToggleButton) node;

                    var properties = node.getProperties();
                    var tooltip = (Tooltip)properties.get(TOOLTIP_PROP_KEY);
                    if (tooltip != null) {
                        var tooltipText = tooltip.getText();

                        if (tooltipText != null && tooltipText.equals("Zoom to fit")) {
                            toggleButton.setSelected(true);
                            toggleButton.fire();
                        }
                    }
                }
            });

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "QuPath ZoomToFit Exception - " + e.getMessage(), "X-Zell: RabbitMQ", JOptionPane.ERROR_MESSAGE);
        }
    }
}
