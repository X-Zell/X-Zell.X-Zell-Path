package qupath.reordered.image.viewer;

import qupath.lib.common.Version;
import qupath.lib.gui.ActionTools;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.extensions.QuPathExtension;
import qupath.lib.gui.tools.MenuTools;
import qupath.reordered.image.viewer.commands.AdjustBrightnessCommand;
import qupath.reordered.image.viewer.commands.ShowReorderedChannelViewerCommand;

public class AdjustBrightnessExtension implements QuPathExtension {

    @Override
    public void installExtension(QuPathGUI qupath) {

        var actionWriter = ActionTools.createAction(new AdjustBrightnessCommand(), "Adjust Brightness");
        actionWriter.setLongText("Adjust Brightness for all channels.");
        actionWriter.disabledProperty().bind(qupath.imageDataProperty().isNull());
        MenuTools.addMenuItems(
                qupath.getMenu("X-Zell", true),
                actionWriter);
    }

    @Override
    public String getName() {
        return "AdjustBrightness extension";
    }

    @Override
    public String getDescription() {
        return "Implements the adjustment of bBrightness for all channels";
    }

    @Override
    public Version getVersion() {
        return Version.parse("1.18.5");
    }

    @Override
    public Version getQuPathVersion() {
        return Version.parse("0.4.2");
//        return QuPathExtension.super.getQuPathVersion();
    }
}
