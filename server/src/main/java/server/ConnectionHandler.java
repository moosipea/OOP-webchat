package server;

import java.io.*;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionHandler implements Runnable, Closeable {
    private final Queue<String> localMessageEvents = new LinkedBlockingQueue<>();
    private final CopyOnWriteArraySet<ConnectionHandler> allConnectionHandlers;
    private final Socket clientSocket;

    public ConnectionHandler(CopyOnWriteArraySet<ConnectionHandler> allConnectionHandlers, Socket clientSocket) {
        this.allConnectionHandlers = allConnectionHandlers;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        allConnectionHandlers.add(this);

        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
        ) {
            Thread receiver = Thread.ofVirtual().start(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        String line = in.readLine();
                        if (line != null) {
                            System.out.println("line = " + line);
                            broadcastMessage(line, true);
                        }
                    } catch (IOException ignored) {
                        // TODO: log exception, but don't crash!
                    }
                }
            });

            addClientMessage("Welcome to the test server!");

            while (!Thread.currentThread().isInterrupted()) {
                pollAndSendMessages(out);
            }

            receiver.interrupt();
        } catch (IOException ignored) {
            // TODO: log error, but don't crash!
        } finally {
            close();
        }
    }

    @Override
    public void close() {
        allConnectionHandlers.remove(this);
    }

    private void pollAndSendMessages(PrintWriter out) {
        String newMessage;

        while ((newMessage = localMessageEvents.poll()) != null) {
            out.write(newMessage + "\n");
            out.flush();
        }
    }

    private synchronized void addClientMessage(String message) {
        localMessageEvents.add(message);
    }

    private void broadcastMessage(String message, boolean ignoreSelf) {
        for (ConnectionHandler conn : allConnectionHandlers) {
            if (ignoreSelf && conn == this) {
                continue;
            }
            conn.addClientMessage(message);
        }
    }
}
