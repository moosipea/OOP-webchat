package server;

import common.networking.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Haldab ühe kliendi ühendust serveriga.
 */
public class ConnectionHandler implements Runnable {
    private static final Logger log = LogManager.getLogger(ConnectionHandler.class);

    private final Socket client;

    private final ServerMain server;

    private DuplexConnection duplex;

    private String username;
    private boolean authenticated = false;

    public ConnectionHandler(Socket client, ServerMain server) {
        this.client = client;
        this.server = server;
    }

    /**
     * Käivitab ühenduse kliendiga.
     */
    @Override
    public void run() {
        DuplexConnection duplexConnection = new DuplexConnection(client);
        try {
            duplexConnection.runConnection(this::handlePacket);
        } catch (IOException e) {
            log.error("IO exception while running packet handler: {}", e.getMessage());
        } finally {
            server.unregister(this);
            try {
                client.close();
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

    private void handlePacket(AbstractPacket packet) {

        // Kui pole veel autentinud, siis me teisi asju ei parsi
        if (!authenticated && !(packet instanceof LoginPacket)) {
            return;
        }

        switch (packet) {
            case MessageToServerPacket msg ->
                    server.broadcastMessage(msg, username);
            case GetChannelsRequestPacket ignored -> {
                for (String channel : server.getChannelList()) {
                    duplex.addPacket(new AddChannelResponsePacket(channel));
                }
            }
            case LoginPacket login -> {
                // TODO: magic check here
                server.register(this);
                authenticated = true;
                username = login.getUsername();
            }
            default -> {
                log.warn("Unexpected packet: {}", packet);
            }
        }
    }

    public DuplexConnection getDuplex() {
        return duplex;
    }
}
