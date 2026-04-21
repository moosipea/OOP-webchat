package client;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.networking.AbstractPacket;
import common.networking.AddChannelResponsePacket;
import common.networking.DuplexConnection;
import common.networking.GetChannelsRequestPacket;
import common.networking.LoginPacket;
import common.networking.MessageToClientPacket;
import common.networking.MessageToServerPacket;

/**
 * Haldab kliendi ühendust serveriga.
 */
public class ClientConnection implements Runnable {
    private static final Logger log = LogManager.getLogger(ClientConnection.class);

    // Serveri detailid.
    private final InetAddress ip;
    private final int port;

    private Consumer<MessageToClientPacket> onMessageReceived = null;
    private Consumer<AddChannelResponsePacket> onChannelAdded = null;

    private final LinkedBlockingQueue<AbstractPacket> queuedPackets = new LinkedBlockingQueue<>();

    public ClientConnection(String ip, String port) throws UnknownHostException {
        // default to localhost
        if (port.isEmpty()) {
            ip = "localhost";
        }
        // default to port 6969
        if (port.isEmpty()) {
            port = "6969";
        }

        this.ip = InetAddress.getByName(ip);
        this.port = Integer.parseInt(port);
    }

    /**
     * Käivitab ühenduse serveriga.
     */
    @Override
    public void run() {
        SSLSocketFactory sf;
        // TODO: sertifikaatide paremini saamine
        KeyStore keyStore;
        try (FileInputStream fis = new FileInputStream("../client-truststore.p12")) {
            keyStore = KeyStore.getInstance("PKCS12");
            String password = "123456"; // FIXME: paroolindus
            keyStore.load(fis, password.toCharArray());
            //KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            //kmf.init(keyStore, password.toCharArray());
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());
            sf = sslContext.getSocketFactory();

        } catch (IOException ex){
            System.getLogger(ClientConnection.class.getName()).log(System.Logger.Level.ERROR, "no key file", ex);
            return;
        } catch (NoSuchAlgorithmException ex) {
            System.getLogger(ClientConnection.class.getName()).log(System.Logger.Level.ERROR, "bad algorithm?", ex);
            return;
        } catch (CertificateException ex) {
            System.getLogger(ClientConnection.class.getName()).log(System.Logger.Level.ERROR, "problem with certificate?", ex);
            return;
        } catch (KeyStoreException ex) {
            System.getLogger(ClientConnection.class.getName()).log(System.Logger.Level.ERROR, "keystore exception?", ex);
            return;
        } catch (KeyManagementException ex) {
            System.getLogger(ClientConnection.class.getName()).log(System.Logger.Level.ERROR, "key management exception?", ex);
            return;
        }
        try (SSLSocket sock = (SSLSocket) sf.createSocket(this.ip, this.port)) {
            sock.startHandshake();
            DuplexConnection duplexConnection = new DuplexConnection(sock, queuedPackets);
            duplexConnection.runConnection(this::handlePacket);
        } catch (IOException e) {
            log.error("IO exception, ending connection: {}", e.getMessage());
        }
    }

    /**
     * Määrab tegevuse, mis on tarvis teha saabunud sõnumi korral.
     *
     * @param onMessageReceived event handler
     */
    public void setOnMessageReceived(Consumer<MessageToClientPacket> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public void setOnChannelAdded(Consumer<AddChannelResponsePacket> onChannelAdded) {
        this.onChannelAdded = onChannelAdded;
    }

    /**
     * Lisab sõnumi järjekorda, et see serverile saata.
     *
     * @param message sõnum.
     */
    public void sendMessage(String targetChannel, String message) {
        addPacket(new MessageToServerPacket(targetChannel, message));
    }

    public void requestChannelList() {
        addPacket(new GetChannelsRequestPacket());
    }

    public void loginWithCredentials(String username, String password) {
        addPacket(new LoginPacket(username, password));
    }

    private void handlePacket(AbstractPacket packet) {
        switch (packet) {
            case MessageToClientPacket msg -> {
                if (onMessageReceived != null) {
                    onMessageReceived.accept(msg);
                }
            }
            case AddChannelResponsePacket addChannelResponse -> {
                if (onChannelAdded != null) {
                    onChannelAdded.accept(addChannelResponse);
                }
            }
            default -> log.warn("Unexpected packet: {}", packet);
        }
    }

    private void addPacket(AbstractPacket packet) {
        queuedPackets.add(packet);
    }
}
