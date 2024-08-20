package qupath.rabbitmq;

import qupath.rabbitmq.actions.*;
import qupath.rabbitmq.commands.ZoomToFitAction;
import qupath.rabbitmq.data.ActionMessage;
import qupath.rabbitmq.provider.AdjustBrightnessesActionProvider;
import qupath.lib.gui.QuPathGUI;

public class ActionFactory {
    private final QuPathGUI qupath;
    private ActionLogger logger;
    private final AdjustBrightnessesActionProvider adjustBrightnessesActionProvider;

    public ActionFactory(QuPathGUI qupath, ActionLogger logger, AdjustBrightnessesActionProvider adjustBrightnessesActionProvider) {
        this.qupath = qupath;
        this.logger = logger;
        this.adjustBrightnessesActionProvider = adjustBrightnessesActionProvider;
    }

    public IQuPathExtensionAction createAction(ActionMessage actionMessage) {
        switch (actionMessage.ActionContext) {
            case "Logger Setup":
                return new LoggerSetupAction(logger, actionMessage.ActionType);
            case "QuPath Project":
                if (actionMessage.ActionType.equals("Create")) {
                    return new CreateProjectAction(actionMessage.ActionValue, qupath, logger);
                } else if (actionMessage.ActionType.equals("Add Image")) {
                    return new AddProjectImageAction(actionMessage.ActionValue, qupath, logger);
                }
            case "QuPath Launch":
                if (actionMessage.ActionType.equals("Contrast Window")) {
                    return new LaunchSubWindowAction("Contrast Window", "{}",qupath, logger);
                } else if (actionMessage.ActionType.equals("Channel Viewer")) {
                    return new LaunchSubWindowAction("Channel Viewer", actionMessage.ActionValue, qupath, logger);
                }
            case "QuPath Brightness":
                return adjustBrightnessesActionProvider.get(actionMessage.ActionType, actionMessage.ActionValue);
            case "QuPath Save":
                return new SaveProjectAction(qupath, logger);
            case "QuPath Activate":
                if (actionMessage.ActionType.equals("ZoomToFit")) {
                    return new ZoomToFitAction(qupath, logger);
                } else if (actionMessage.ActionType.equals("Image")) {
                    return new ActivateImageAction(qupath, logger);
                }
                return null;
            default:
                return null;
        }
    }
}
