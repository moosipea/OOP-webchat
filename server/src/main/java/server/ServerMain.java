package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import common.networking.MessageToClientPacket;
import common.networking.MessageToServerPacket;
import common.objects.Message;

/**
 * Serveri põhiklass. TODO: ilmselt võiks maini siia tõsta hoopis.
 */
public class ServerMain {
    // Kõigi aktiivsete ühenduste hulk.
    private final CopyOnWriteArraySet<ConnectionHandler> allConnectionHandlers = new CopyOnWriteArraySet<>();

    private final List<String> channelList = Collections.synchronizedList(new ArrayList<>());

    private final List<Message> messages;

    public ServerMain() {
        // Suvalised näidiskanalid
        channelList.add("#general");
        channelList.add("#server-loodud-kanal-1");
        channelList.add("#server-loodud-kanal-2");
        messages = new ArrayList<>();
    }

    public static void main(String[] args) {
        new ServerMain().start();
    }

    

    /**
     * Käivitab serveri.
     */
    public void start() {

        SSLServerSocketFactory ssf;
        // Kasutame virtuaalseid lõimesid, et ei peaks mingi async asjadega eraldi jamama.
        // TODO: teha port konfigureeritavaks.
        
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance("PKCS12");
        } catch (KeyStoreException ex) {
            System.getLogger(ServerMain.class.getName()).log(System.Logger.Level.ERROR, "keystore error", ex);
            return;
        }
        try (FileInputStream fis = new FileInputStream("./keystore.p12")) {
            String password = "123456";
            keyStore.load(fis, password.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, password.toCharArray());
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());
            ssf = sslContext.getServerSocketFactory();

        } catch (IOException ex){
            System.getLogger(ServerMain.class.getName()).log(System.Logger.Level.ERROR, "no key file", ex); // TODO: paremad exceptionid
            return;
        } catch (NoSuchAlgorithmException ex) {
            System.getLogger(ServerMain.class.getName()).log(System.Logger.Level.ERROR, "bad algorithm?", ex);
            return;
        } catch (CertificateException ex) {
            System.getLogger(ServerMain.class.getName()).log(System.Logger.Level.ERROR, "problem with certificate?", ex);
            return;
        } catch (UnrecoverableKeyException ex) {
            System.getLogger(ServerMain.class.getName()).log(System.Logger.Level.ERROR, "key exception?", ex);
            return;
        } catch (KeyStoreException ex) {
            System.getLogger(ServerMain.class.getName()).log(System.Logger.Level.ERROR, "keystore exception?", ex);
            return;
        } catch (KeyManagementException ex) {
            System.getLogger(ServerMain.class.getName()).log(System.Logger.Level.ERROR, "key management exception?", ex);
            return;
        }

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
             SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(6969)) {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket client = serverSocket.accept(); // Blokib, kuni uus klient ühendab
                    executor.submit(new ConnectionHandler(client, this)); // Loob sellele vastava handler'i.
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

    public List<String> getChannelList() {
        return channelList;
    }

    /**
     * Edastab sõnumi kõigile ühendatud ja autenditud kasutajatele.
     */
    public void broadcastMessage(MessageToServerPacket message, String author) {
        // TODO: SIIN KOHA PEAL TOPPIDA ANDMEBAASI
        Timestamp now = Timestamp.from(Instant.now());
        long id = messages.size();
        messages.add(new Message(message.getTargetChannel(), message.getContent(), now, author, id));
        MessageToClientPacket packetToBeSent = new MessageToClientPacket(message, author, now, id);
        for (ConnectionHandler conn : allConnectionHandlers) {
            conn.addPacket(packetToBeSent);
        }
    }

    public List<Message> getMessages(){
        return messages;
    }

    public List<Message> getMessages(String channel){
        return messages.stream().filter(m -> channel.equals(m.channel)).toList();
    }
}
