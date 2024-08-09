package qupath.reordered.image.viewer.commands;

public class AntibodyInfo {
    public Integer ChannelNumber;
    public String ChannelName;
    public String Antibody;
    public String ColorCode;
    public Integer BrightnessMin;
    public Integer BrightnessMax;

    public AntibodyInfo() {

    }

    public AntibodyInfo(Integer channelNumber, String channelName, String antibody, String colorCode, Integer brightnessMin, Integer brightnessMax) {
        this.ChannelNumber = channelNumber;
        this.ChannelName = channelName;
        this.Antibody = antibody;
        this.ColorCode = colorCode;
        this.BrightnessMin = brightnessMin;
        this.BrightnessMax = brightnessMax;
    }

    public String getChannelName() {
        return ChannelName;
    }

    public String getAntibody() {
        return Antibody;
    }

    public String getColorCode() {
        return ColorCode;
    }

}
