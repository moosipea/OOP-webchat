import java.util.ArrayList;

/*
    TODO: Access to messages (getter -> sth else)
    TODO: edit messages
    TODO: leave conv
    TODO: edit messages
    TODO: Check access modifiers for all classes in the branch

*/

public class User {
    // User info
    private String username;
    private boolean online;
    private ArrayList<Conversation> conversations;

    // Chat customization
    private String color;


    public User(String username){
        this.username = username;
        this.online = false;    // default value??
        this.conversations = new ArrayList<Conversation>();

        this.color = "white";   // add color codes
    }

    /*
    Conversation methods
    */
    public void createConv(int convID, User...users){
        Conversation conv = new Conversation(convID, this);
        conversations.add(conv);
        for(User user : users) conv.addMember(this, user);
    }

    public void addMember(Conversation conversation, User member){
        conversation.addMember(this, member);
    }

    public void removeMember(Conversation conversation, User member){
        conversation.removeMember(this, member);
    }

    public void addMessage(Conversation conversation, String message){
        conversation.addEntry(this, message);
    }

    // use other ID for Entry reference?
    public void removeMessage(Conversation conversation, Entry message){
        conversation.removeEntry(this, message);
    }

    /*
    String / print methods
    */

    @Override
    public String toString(){
        return String.format("""
                Username: %s
                Online status: %s
                Chat color: %s
                """,this.username, this.online, this.color);
    }

    public String getUsername(){
        return this.username;
    }


    public ArrayList<Conversation> getConvs(){
        return this.conversations;
    }
}

