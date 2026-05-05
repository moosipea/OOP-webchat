package server;

import common.networking.packets.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Serveri põhiklass.
 */
public class ServerMain implements AutoCloseable {
    private static final Logger log = LogManager.getLogger(ServerMain.class);

    // Kõigi aktiivsete ühenduste hulk.
    private final CopyOnWriteArraySet<ConnectionHandler> allConnectionHandlers = new CopyOnWriteArraySet<>();
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

        // TODO: kliendi poolel sama jura, abstraheerida

        SSLServerSocketFactory ssf;
        KeyStore keyStore;

        try {
            keyStore = KeyStore.getInstance("PKCS12");
        } catch (KeyStoreException e) {
            log.error("Key store exception: {}", e.getMessage());
            return;
        }

        // TODO: paroolindus
        try (FileInputStream fis = new FileInputStream("./keystore.p12")) {
            String password = "123456";
            keyStore.load(fis, password.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, password.toCharArray());
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());
            ssf = sslContext.getServerSocketFactory();
        } catch (Exception e) {
            log.error("Failed to create SSL context: {}", e.getMessage());
            return;
        }

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
             SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(6969)) {
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

    public List<String> getChannelList(String forWhom) {
        return chatDataStore.getChannels(forWhom);
    }

    public List<MessageToClientPacket> getHistory(RequestHistoryPacket packet) {
        return chatDataStore.retrieveMessages(packet);
    }

    /**
     * Edastab sõnumi kõigile ühendatud ja autenditud kasutajatele.
     */
    public void broadcastMessage(MessageToServerPacket message, String author) {
        Timestamp now = Timestamp.from(Instant.now());
        MessageToClientPacket packetToBeSent = new MessageToClientPacket(message, author, now);

        chatDataStore.saveMessage(packetToBeSent);

        for (ConnectionHandler conn : allConnectionHandlers) {
            conn.addPacket(packetToBeSent);
        }
    }

    @Override
    public void close() {
        chatDataStore.close();
    }

    public boolean attemptToRegisterUser(RegisterRequestPacket registerPacket) {
        return chatDataStore.attemptToRegisterUser(registerPacket);
    }

    public boolean attemptToLogInUser(LoginRequestPacket loginRequestPacket) {
        return chatDataStore.attemptToLogInUser(loginRequestPacket);
    }
}
