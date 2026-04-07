package common.networking;

import java.sql.Timestamp;

public class MessageToClientPacket extends MessageToServerPacket {
    private final Timestamp timestamp;

    public MessageToClientPacket(String targetChannel, String content, Timestamp timestamp) {
        super(targetChannel, content);
        this.timestamp = timestamp;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
