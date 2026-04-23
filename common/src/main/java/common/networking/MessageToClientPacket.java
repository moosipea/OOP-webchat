package common.networking;

import java.sql.Timestamp;
import java.util.Date;

public class MessageToClientPacket extends MessageToServerPacket {
    private Date timestamp = null;
    private String user;
    private long id;

    public MessageToClientPacket(String targetChannel, String user, String content, Date timestamp, long id) {
        super(targetChannel, content);
        this.timestamp = timestamp;
        this.user = user;
        this.id = id;
    }

    public MessageToClientPacket(MessageToServerPacket incoming, String user, Timestamp timestamp, long id) {
        super(incoming.getTargetChannel(), incoming.getContent());
        this.timestamp = timestamp;
        this.user = user;
        this.id = id;
    }

    public MessageToClientPacket() {
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getUser(){
        return user;
    }

    public long getId(){
        return id;
    }
}
