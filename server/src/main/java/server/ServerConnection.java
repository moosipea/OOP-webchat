package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class ServerConnection implements Runnable {
    private volatile boolean stillRunning = true;
    private Consumer<RawMessage> consumer;

    public ServerConnection(Consumer<RawMessage> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(6969)) {
            Socket client = serverSocket.accept();
            try (PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))
            ) {
                while (stillRunning) {
                    String messageFromClient = in.readLine();
                    consumer.accept(new RawMessage(messageFromClient, 1));
                    out.write("echo: " + messageFromClient + "\n");
                    out.flush();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void endConnection() {
        this.stillRunning = false;
    }

    public record RawMessage(String content, int ip) {
    }
}
