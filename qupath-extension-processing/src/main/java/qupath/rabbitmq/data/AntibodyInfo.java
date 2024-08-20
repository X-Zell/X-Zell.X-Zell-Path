package qupath.rabbitmq.data;

public class AntibodyInfo {
    public Integer ChannelNumber;
    public String ChannelName;
    public String Antibody;
    public String ColorCode;
    public Integer BrightnessMin;
    public Integer BrightnessMax;

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
