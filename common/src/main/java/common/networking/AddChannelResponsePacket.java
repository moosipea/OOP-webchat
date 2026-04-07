package common.networking;

public class AddChannelResponsePacket extends AbstractPacket {
    private final String channelName;

    public AddChannelResponsePacket(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelName() {
        return channelName;
    }
}
