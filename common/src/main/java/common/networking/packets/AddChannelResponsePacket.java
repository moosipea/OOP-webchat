package common.networking.packets;

public class AddChannelResponsePacket extends AbstractPacket {
    private String channelName;

    public AddChannelResponsePacket(String channelName) {
        this.channelName = channelName;
    }

    public AddChannelResponsePacket() {
    }

    public String getChannelName() {
        return channelName;
    }
}
