package server;

import java.util.ArrayList;
import java.util.List;

public class ServerMain {
    private List<ServerConnection.RawMessage> allMessages = new ArrayList<>();

    public static void main(String[] args) {
        ServerConnection conn = new ServerConnection();
        Thread.Builder threadBuilder = Thread.ofVirtual();

        Thread t = threadBuilder.start(conn);
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("Connection over!");
        }
    }

    public synchronized void addMessage(ServerConnection.RawMessage message) {
        this.allMessages.add(message);
    }
}
