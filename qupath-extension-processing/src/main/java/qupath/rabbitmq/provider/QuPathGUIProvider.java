package qupath.rabbitmq.provider;

import com.google.inject.Provider;
import qupath.lib.gui.QuPathGUI;

public class QuPathGUIProvider implements Provider<QuPathGUI> {
    @Override
    public QuPathGUI get() {
        // Logic to obtain an instance of QuPathGUI, typically this might be passed in or looked up
        return QuPathGUI.getInstance();
    }
}
