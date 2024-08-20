package qupath.rabbitmq.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import qupath.rabbitmq.ActionFactory;
import qupath.rabbitmq.ActionLogger;
import qupath.lib.gui.QuPathGUI;

public class ActionFactoryProvider implements Provider<ActionFactory> {
    private final QuPathGUI quPathGUI;
    private final ActionLogger logger;
    private final AdjustBrightnessesActionProvider adjustBrightnessesActionProvider;

    @Inject
    public ActionFactoryProvider(QuPathGUI quPathGUI, ActionLogger logger, AdjustBrightnessesActionProvider adjustBrightnessesActionProvider) {
        this.quPathGUI = quPathGUI;
        this.logger = logger;
        this.adjustBrightnessesActionProvider = adjustBrightnessesActionProvider;
    }

    @Override
    public ActionFactory get() {
        return new ActionFactory(quPathGUI, logger, adjustBrightnessesActionProvider);
    }
}