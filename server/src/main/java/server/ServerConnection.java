package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * Serveri põhiklass. TODO: ilmselt võiks maini siia tõsta hoopis.
 */
public class ServerConnection implements Runnable {
    // Kõigi aktiivsete ühenduste hulk.
    private final CopyOnWriteArraySet<ConnectionHandler> allConnectionHandlers = new CopyOnWriteArraySet<>();

    private final List<String> channelList = Collections.synchronizedList(new ArrayList<>());

    public ServerConnection() {
        channelList.add("#general");
        channelList.add("#server-loodud-kanal-1");
        channelList.add("#server-loodud-kanal-2");
    }

    /**
     * Käivitab serveri.
     */
    @Override
    public void run() {
        // Kasutame virtuaalseid lõimesid, et ei peaks mingi async asjadega eraldi jamama.
        // TODO: teha port konfigureeritavaks.
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
             ServerSocket serverSocket = new ServerSocket(6969)) {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket client = serverSocket.accept(); // Blokib, kuni uus klient ühendab
                    executor.submit(new ConnectionHandler(this, client)); // Loob sellele vastava handler'i.
                } catch (IOException ignored) {
                    // TODO: log exception, but don't crash!
                }
            }
        } catch (IOException e) {
            // Siin võiks midagi targemat teha
            throw new RuntimeException(e);
        }
    }

    public void register(ConnectionHandler handler) {
        allConnectionHandlers.add(handler);
    }

    public void unregister(ConnectionHandler handler) {
        allConnectionHandlers.remove(handler);
    }

    public CopyOnWriteArraySet<ConnectionHandler> getAllConnectionHandlers() {
        return allConnectionHandlers;
    }

    public List<String> getChannelList() {
        return channelList;
    }
}
