package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ServerConnection implements Runnable {
    private final CopyOnWriteArraySet<ConnectionHandler> allConnectionHandlers = new CopyOnWriteArraySet<>();

    @Override
    public void run() {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
             ServerSocket serverSocket = new ServerSocket(6969)) {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket client = serverSocket.accept();
                    executor.submit(new ConnectionHandler(allConnectionHandlers, client));
                } catch (IOException ignored) {
                    // TODO: log exception, but don't crash!
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
