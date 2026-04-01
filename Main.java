public class Main {
    public static void main(String[] args) {
        int serverConvID = 0;

        User owner = new User("owner");
        User[] users = {new User("user1"), new User("user2")};

        owner.createConv(0, users);
        Conversation conv = owner.getConvs().get(serverConvID++);
        conv.setConvName("abcde");

        for(int i = 0; i<users.length; i++){
            users[i].addMessage(conv, String.format("Message %s content", i));
        }

        conv.printAll();

        System.out.println(conv);
    }
}
