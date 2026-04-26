package client;

import common.networking.*;
import common.networking.packets.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * Haldab kliendi ühendust serveriga.
 */
public class ClientConnection implements Runnable {
    private static final Logger log = LogManager.getLogger(ClientConnection.class);

    // Serveri detailid.
    private final InetAddress ip;
    private final int port;

    private final LinkedBlockingQueue<AbstractPacket> queuedPackets = new LinkedBlockingQueue<>();

    private Consumer<MessageToClientPacket> onMessageReceived = null;
    private Consumer<AddChannelResponsePacket> onChannelAdded = null;
    private Consumer<LoginResponsePacket> onLoginResponse = null;
    private Consumer<RegisterResponsePacket> onRegisterResponse = null;

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

    public void setOnMessageReceived(Consumer<MessageToClientPacket> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public void setOnChannelAdded(Consumer<AddChannelResponsePacket> onChannelAdded) {
        this.onChannelAdded = onChannelAdded;
    }

    public void setOnLoginResponse(Consumer<LoginResponsePacket> onLoginResponse) {
        this.onLoginResponse = onLoginResponse;
    }

    public void setOnRegisterResponse(Consumer<RegisterResponsePacket> onRegisterResponse) {
        this.onRegisterResponse = onRegisterResponse;
    }

    /**
     * Käivitab ühenduse serveriga.
     */
    @Override
    public void run() {
        SSLSocketFactory sf;
        KeyStore keyStore;

        // TODO: sertifikaatide paremini saamine
        // TODO: sertifikaat jar faili sisse pakitud?
        try (FileInputStream fis = new FileInputStream("../client-truststore.p12")) {
            keyStore = KeyStore.getInstance("PKCS12");
            String password = "123456"; // FIXME: paroolindus
            keyStore.load(fis, password.toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());
            sf = sslContext.getSocketFactory();

        } catch (Exception e) {
            log.error("Failed to initialise SSL context: {}", e.getMessage());
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
        addPacket(new LoginRequestPacket(username, password));
    }

    public void registerWithCredentials(String username, String password) {
        addPacket(new RegisterRequestPacket(username, password));
    }

    public void requestHistory(String channel, Instant before, Instant notBefore){
        addPacket(new RequestHistoryPacket(channel, before, notBefore));
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
                    requestHistory(addChannelResponse.getChannelName(), null, null);
                    System.out.println("requesting history");
                }
            }
            case RegisterResponsePacket registerResponsePacket -> {
                if (onRegisterResponse != null) {
                    onRegisterResponse.accept(registerResponsePacket);
                }
            }
            case LoginResponsePacket loginResponsePacket -> {
                if (onLoginResponse != null) {
                    onLoginResponse.accept(loginResponsePacket);
                }
            }
            default -> log.warn("Unexpected packet: {}", packet);
        }
    }

    private void addPacket(AbstractPacket packet) {
        queuedPackets.add(packet);
    }
}
