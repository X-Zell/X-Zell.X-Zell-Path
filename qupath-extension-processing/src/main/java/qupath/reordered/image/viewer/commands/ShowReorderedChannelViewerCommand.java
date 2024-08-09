package qupath.reordered.image.viewer.commands;

import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewerPlus;

public class ShowReorderedChannelViewerCommand implements Runnable {

    public ShowReorderedChannelViewerCommand() {
    }

    @Override
    public void run() {

        QuPathViewerPlus viewer = QuPathGUI.getInstance().getViewer();
        if (viewer == null)
            return;

        ReorderableMiniViewer.createDialog(viewer).show();
    }
}

