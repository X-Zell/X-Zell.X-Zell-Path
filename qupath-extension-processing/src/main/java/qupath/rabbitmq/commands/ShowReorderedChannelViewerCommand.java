package qupath.rabbitmq.commands;

import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewerPlus;

import java.util.List;

public class ShowReorderedChannelViewerCommand implements Runnable {

    private String[] orderedChannelNames;

    public ShowReorderedChannelViewerCommand(String[] orderedChannelNames) {
        this.orderedChannelNames = orderedChannelNames;
    }

    @Override
    public void run() {

        QuPathViewerPlus viewer = QuPathGUI.getInstance().getViewer();
        if (viewer == null)
            return;

        ReorderableMiniViewer.createDialog(viewer, this.orderedChannelNames).show();
    }
}
