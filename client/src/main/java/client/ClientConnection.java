package client;

import common.networking.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.function.Consumer;

/**
 * Haldab kliendi ühendust serveriga.
 */
public class ClientConnection implements Runnable {
    private static final Logger log = LogManager.getLogger(ClientConnection.class);

    // Serveri detailid.
    private final InetAddress ip;
    private final int port;
    private final String username;
    private final String password;

    private Consumer<MessageToClientPacket> onMessageReceived = null;
    private Consumer<AddChannelResponsePacket> onChannelAdded = null;
    private DuplexConnection duplexConnection = null;

    public ClientConnection(String ip, String port, String username, String password) throws UnknownHostException {
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
        this.username = username;
        this.password = password;
    }

    /**
     * Käivitab ühenduse serveriga.
     */
    @Override
    public void run() {
        try (Socket sock = new Socket(ip, port)) {
            duplexConnection = new DuplexConnection(sock);
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
        duplexConnection.addPacket(new MessageToServerPacket(targetChannel, message));
    }

    public void requestChannelList() {
        duplexConnection.addPacket(new GetChannelsRequestPacket());
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
}
