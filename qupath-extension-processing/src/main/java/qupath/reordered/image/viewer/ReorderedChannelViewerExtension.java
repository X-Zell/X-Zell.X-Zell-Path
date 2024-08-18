package qupath.reordered.image.viewer;

import qupath.lib.common.Version;
// import qupath.lib.gui.ActionTools;
// import qupath.lib.gui.ActionTools.ActionDescription;
// import qupath.lib.gui.ActionTools.ActionMenu;
import qupath.lib.gui.ActionTools;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.extensions.QuPathExtension;

//import qupath.ext.rabbitmq.commands.ShowReorderedChannelViewer;
import qupath.lib.gui.tools.MenuTools;
import qupath.reordered.image.viewer.commands.ShowReorderedChannelViewerCommand;

/**
 * Extension for implementing a RabbitMq consumer.
 */
public class ReorderedChannelViewerExtension implements QuPathExtension {

    @Override
    public void installExtension(QuPathGUI qupath) {
//        RabbitMQConsumer rabbitMQConsumer = null;
//        try {
//            var logger = new ActionLogger();
//            rabbitMQConsumer = new RabbitMQConsumer(logger);
//            rabbitMQConsumer.ProcessMessages(qupath);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        var actionWriter = ActionTools.createAction(new ShowReorderedChannelViewerCommand(), "Channel Viewer");
        actionWriter.setLongText("Custom version of Channel Viewer that implements bespoke tile ordering.");
        actionWriter.disabledProperty().bind(qupath.imageDataProperty().isNull());
        MenuTools.addMenuItems(
                qupath.getMenu("X-Zell", true),
                actionWriter);
    }

    @Override
    public String getName() {
        return "ReorderedChannel extension";
    }

    @Override
    public String getDescription() {
        return "Implements a channel viewer with bespoke tile ordering.";
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

