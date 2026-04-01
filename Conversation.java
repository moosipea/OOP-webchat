import java.util.ArrayList;

public class Conversation {
    private int id;
    private User owner;
    private ArrayList<User> participants;
    private ArrayList<Entry> messages;

    private int entryID;

    /*
    id - unique id for this conversation.
        TODO: automatically manage id

    owner - has permissions to add/remove participants
    
    participants: internal users list
        can be managed by owner
        used for:
        - creating entries to this.messages
        - TODO: validation for server>user sync 

    messages: ArrayList<Entry>, see Entry class for more info
    entryID: internal counter & instance field value for entries. !! Dont change outside of this.addmessage()
    */

    public Conversation(int id, User owner){
        this.id = id;
        this.owner = owner;
        this.participants = new ArrayList<User>();
        this.messages = new ArrayList<Entry>();
        this.entryID = 0;

        this.participants.add(owner);

        System.out.printf("""
                Chat created
                %s
                """, this.toString());
    }

    /*
    
    Manage members
    
    */

    public void addMember(User member){
        if(this.participants.contains(member)){
            System.out.printf("Member %s already in conversation %s\n", member.getUsername(), this.id);
            return;
        }

        this.participants.add(member);
        System.out.printf("Member %s added to conversation %s\n", member.getUsername(), this.id);
    }

    // TODO: delete conversation when member count reaches 0
    public void removeMember(User member){
        if(this.participants.contains(member)){
            this.participants.remove(member);
            System.out.printf("Member %s removed from conversation %s\n", member.getUsername(), this.id);
        }
        else{
            System.out.printf("Member %s can't be removed from conversation %s\n", member.getUsername(), this.id);
        }
    }

    /*
    
    Manage messages

    */

    public void addMessage(User sender, String message){
        this.messages.add(new Entry(this.entryID++, sender, message));
    }

    public void removeMessage(Entry message){
        this.messages.remove(message);
    }

    /*
    
    Get chat info (String, print)

    */

    public void printAll(){
        for(Entry entry : this.messages){
            System.out.println(entry);
        }
        System.out.println("");
    }

    // Owner -> username
    @Override
    public String toString(){
        return String.format("""
                Chat ID: %s
                Owner: %s
                Participants: %s
                Message count: %s
                """,this.id, this.owner.getUsername(), this.strParticipants(), this.messages.size());
    }

    private String strParticipants(){
        StringBuilder temp = new StringBuilder();
        for(User participant : participants){
            temp.append(participant.getUsername() + ", ");
        }
        temp.delete(Math.max(0, temp.length()-2), temp.length());

        return temp.toString();
    }
}
