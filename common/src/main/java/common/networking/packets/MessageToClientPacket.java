package common.networking.packets;


public class MessageToClientPacket extends MessageToServerPacket {
    private long timestampMillis = -1; // TODO: kas on ikka vaja kasutada java.sql.Timestamp java.time.Instant asemel? ei.
    private String user;
    private long id;

    public MessageToClientPacket(String targetChannel, String user, String content, long timestampMillis, long id) {
        super(targetChannel, content);
        this.timestampMillis = timestampMillis;
        this.user = user;
        this.id = id;
    }

    public MessageToClientPacket(MessageToServerPacket incoming, String user, long timestampMillis, long id) {
        super(incoming.getTargetChannel(), incoming.getContent());
        this.timestampMillis = timestampMillis;
        this.user = user;
        this.id = id;
    }

    public MessageToClientPacket() {
    }

    public long getTimestampMillis() {
        return timestampMillis;
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
