package common.networking.packets;

public class RequestHistoryPacket extends AbstractPacket{
    private String channel;
    private long before;
    private long notBefore;

    public RequestHistoryPacket(String channel){
        this.channel = channel;
        this.before = Long.MAX_VALUE;
        this.notBefore = Long.MIN_VALUE;
    }
    
    public RequestHistoryPacket(String channel, long before, long notBefore){
        this.channel = channel;
        this.before = before;
        this.notBefore = notBefore;
    }
    public RequestHistoryPacket(){}

    public String getChannel(){
        return channel;
    }
    public long getBefore(){
        return before;
    }
    public long getNotBefore(){
        return notBefore;
    }
}
