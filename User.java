public class User {
    // User info
    private String username;
    private boolean online;

    // Chat customization
    private String color;

    public User(String username){
        this.username = username;
        this.online = false;    // default value??
        this.color = "white";   // add color codes
    }

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
}

