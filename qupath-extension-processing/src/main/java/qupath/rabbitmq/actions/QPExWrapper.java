package qupath.rabbitmq.actions;

import qupath.lib.gui.scripting.QPEx;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.images.ImageData;
import qupath.lib.scripting.QP;

import java.awt.image.BufferedImage;

public interface QPExWrapper {
    QuPathViewer getCurrentViewer();
    void setChannelNames(ImageData<BufferedImage> imageData, String[] names);
    void setChannelColors(ImageData<BufferedImage> imageData, Integer[] colors);
    void setChannelDisplayRange(ImageData<BufferedImage> imageData, int channel, double min, double max);
}

class QPExWrapperImpl implements QPExWrapper {
    @Override
    public QuPathViewer getCurrentViewer() {
        return QPEx.getCurrentViewer();
    }

    @Override
    public void setChannelNames(ImageData<BufferedImage> imageData, String[] names) {
        QP.setChannelNames(imageData, names);
    }

    @Override
    public void setChannelColors(ImageData<BufferedImage> imageData, Integer[] colors) {
        QP.setChannelColors(imageData, colors);
    }

    @Override
    public void setChannelDisplayRange(ImageData<BufferedImage> imageData, int channel, double min, double max) {
        QPEx.setChannelDisplayRange(imageData, channel, min, max);
    }
}
