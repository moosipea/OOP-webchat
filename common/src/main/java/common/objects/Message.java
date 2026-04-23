package common.objects;

import java.util.Date;

public class Message {
    public final String channel;
    public final String content;
    public final Date time;
    public final String user;
    public final long id;
    public Message(String channel, String content, Date time, String user, long id){
        this.channel = channel;
        this.content = content;
        this.time = time;
        this.user = user;
        this.id = id;
    }
}
