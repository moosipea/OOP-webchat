package common.networking.packets;
import java.time.Instant;

public class RequestHistoryPacket extends AbstractPacket{
    private String channel;
    private Instant before;
    private Instant notBefore;

    public RequestHistoryPacket(String channel, Instant before, Instant notBefore){
        this.channel = channel;
        this.before = before;
        this.notBefore = notBefore;
    }
    public RequestHistoryPacket(){}

    public String getChannel(){
        return channel;
    }
    public Instant getBefore(){
        return before;
    }
    public Instant getNotBefore(){
        return notBefore;
    }
}
