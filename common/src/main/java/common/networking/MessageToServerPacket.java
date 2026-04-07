package common.networking;

public class MessageToServerPacket extends AbstractPacket {
    private final String targetChannel;
    private final String content;

    public MessageToServerPacket(String targetChannel, String content) {
        this.targetChannel = targetChannel;
        this.content = content;
    }

    public String getTargetChannel() {
        return targetChannel;
    }

    public String getContent() {
        return content;
    }
}
