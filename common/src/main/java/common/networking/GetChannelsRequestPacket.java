package common.networking;

import java.util.Date;

/**
 * tegeleb kanalite nimekirja küsimisega ja kanali sõnumite küsimisega
 */
public class GetChannelsRequestPacket extends AbstractPacket {
    private boolean requestList;
    private String channel;
    private Date before;
    private Date notBefore;

    public GetChannelsRequestPacket(){
        requestList = true;
        channel = null;
        before = null;
        notBefore = null;
    }

    public GetChannelsRequestPacket(String channel, Date before, Date notBefore){
        requestList = false;
        this.channel = channel;
        this.before = before;
        this.notBefore = notBefore;
    }

    public boolean getRequestList(){
        return requestList;
    }
    public String getChannel(){
        return channel;
    }
    public Date getBefore(){
        return before;
    }
    public Date getNotBefore(){
        return notBefore;
    }
}
