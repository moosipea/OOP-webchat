package common.networking.packets;

import java.sql.Timestamp;
import java.time.Instant;

public class MessageToClientPacket extends MessageToServerPacket {
    private Instant timestamp = null; // TODO: kas on ikka vaja kasutada java.sql.Timestamp java.time.Instant asemel? ei.
    private String user;
    private long id;

    public MessageToClientPacket(String targetChannel, String user, String content, Instant timestamp, long id) {
        super(targetChannel, content);
        this.timestamp = timestamp;
        this.user = user;
        this.id = id;
    }

    public MessageToClientPacket(MessageToServerPacket incoming, String user, Instant timestamp, long id) {
        super(incoming.getTargetChannel(), incoming.getContent());
        this.timestamp = timestamp;
        this.user = user;
        this.id = id;
    }

    public MessageToClientPacket() {
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getUser(){
        return user;
    }

    public long getId(){
        return id;
    }

    public void setId(long value){
        id = value;
    }
}
