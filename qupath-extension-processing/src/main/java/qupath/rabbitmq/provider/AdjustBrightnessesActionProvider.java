package qupath.rabbitmq.provider;

import com.google.inject.Inject;
import qupath.rabbitmq.ActionLogger;
import qupath.rabbitmq.actions.AdjustBrightnessesAction;
import qupath.lib.gui.QuPathGUI;

public class AdjustBrightnessesActionProvider {

    private final QuPathGUI quPathGUI;
    private final ActionLogger logger;

    @Inject
    public AdjustBrightnessesActionProvider(QuPathGUI quPathGUI, ActionLogger logger) {
        this.quPathGUI = quPathGUI;
        this.logger = logger;
    }

    public AdjustBrightnessesAction get(String antibodyType, String antibodyInfoPayloadJson) {
        return new AdjustBrightnessesAction(antibodyType, antibodyInfoPayloadJson, quPathGUI, logger);
    }
}
