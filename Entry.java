import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class Entry {
    private final int index;
    private final User sender;
    private final LocalDateTime timestamp;
    private String content;

    public Entry(int index, User sender, String content){
        this.index = index;
        this.sender = sender;
        this.content = content;

        this.timestamp = LocalDateTime.now();
    }


    @Override
    public String toString(){
        
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
        String formatted = this.timestamp.format(formatter);

        return String.format("""
                Sender: %s
                Timestamp: %s
                Content: %s
                """, this.sender.getUsername(), formatted, this.content);
    }

    public int getIndex(){
        return this.index;
    }

    public User getSender(){
        return this.sender;
    }
}
