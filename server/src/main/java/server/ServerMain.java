package server;

import common.networking.packets.LoginRequestPacket;
import common.networking.packets.MessageToClientPacket;
import common.networking.packets.MessageToServerPacket;
import common.networking.packets.RegisterRequestPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * Serveri põhiklass.
 */
public class ServerMain implements AutoCloseable {
    private static final Logger log = LogManager.getLogger(ServerMain.class);

    // Kõigi aktiivsete ühenduste hulk.
    private final CopyOnWriteArraySet<ConnectionHandler> allConnectionHandlers = new CopyOnWriteArraySet<>();

    private final List<String> channelList = Collections.synchronizedList(new ArrayList<>());
    private final DatabaseBackend chatDataStore;

    public ServerMain() throws SQLException {
        chatDataStore = new DatabaseBackend();

        // Suvalised näidiskanalid
        chatDataStore.saveChannel("#general");
        chatDataStore.saveChannel("#server-loodud-kanal-1");
        chatDataStore.saveChannel("#server-loodud-kanal-2");
    }

    public static void main(String[] args) {
        try (ServerMain server = new ServerMain()) {
            server.start();
        } catch (SQLException e) {
            log.error("Failed to initialise server due to SQL exception: {}", e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Käivitab serveri.
     */
    public void start() {
        // Kasutame virtuaalseid lõimesid, et ei peaks mingi async asjadega eraldi jamama.
        // TODO: teha port konfigureeritavaks.
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
             ServerSocket serverSocket = new ServerSocket(6969)) {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket client = serverSocket.accept(); // Blokib, kuni uus klient ühendab
                    executor.submit(new ConnectionHandler(client, this)); // Loob sellele vastava handler'i.
                } catch (IOException e) {
                    log.error("Creating client connection failed, dismissing: {}", e.getMessage());
                }
            }
        } catch (IOException e) {
            // TODO: Siin võiks midagi targemat teha
            throw new RuntimeException(e);
        }
    }

    public void register(ConnectionHandler handler) {
        allConnectionHandlers.add(handler);
    }

    public void unregister(ConnectionHandler handler) {
        allConnectionHandlers.remove(handler);
    }

    public List<String> getChannelList() {
        return channelList;
    }

    /**
     * Edastab sõnumi kõigile ühendatud ja autenditud kasutajatele.
     */
    public void broadcastMessage(MessageToServerPacket message, String author) {
        Timestamp now = Timestamp.from(Instant.now());
        MessageToClientPacket packetToBeSent = new MessageToClientPacket(message, author, now);

        // TODO: see on sitt, tuleks teha mingi eraldi queue nende jaoks.
        //  aga ma hetkel ei viitsi
        // TODO: kasutada HikariCP-d
        synchronized (this) {
            chatDataStore.saveMessage(packetToBeSent);
        }

        for (ConnectionHandler conn : allConnectionHandlers) {
            conn.addPacket(packetToBeSent);
        }
    }

    @Override
    public void close() {
        try {
            chatDataStore.close();
        } catch (SQLException e) {
            log.error("Closing database failed: {}", e.getMessage());
        }
    }

    public boolean attemptToRegisterUser(RegisterRequestPacket registerPacket) {
        // TODO: kasutada HikariCP-d
        synchronized (this) {
            return chatDataStore.attemptToRegisterUser(registerPacket);
        }
    }

    public boolean attemptToLogInUser(LoginRequestPacket loginRequestPacket) {
        // TODO: kasutada HikariCP-d
        synchronized (this) {
            return chatDataStore.attemptToLogInUser(loginRequestPacket);
        }
    }
}
