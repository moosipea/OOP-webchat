package server;

import common.networking.*;
import common.networking.packets.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Haldab ühe kliendi ühendust serveriga.
 */
public class ConnectionHandler implements Runnable {
    private static final Logger log = LogManager.getLogger(ConnectionHandler.class);

    private final Socket client;

    private final ServerMain server;

    private final LinkedBlockingQueue<AbstractPacket> queuedPackets = new LinkedBlockingQueue<>();

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
        DuplexConnection duplexConnection = new DuplexConnection(client, queuedPackets);
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
        if (!authenticated && !(packet instanceof LoginRequestPacket)) {
            return;
        }

        switch (packet) {
            case MessageToServerPacket msg ->
                    server.broadcastMessage(msg, username);
            case GetChannelsRequestPacket ignored -> {
                for (String channel : server.getChannelList(username)) {
                    addPacket(new AddChannelResponsePacket(channel));
                }
            }
            case RegisterRequestPacket register -> {
                boolean success = server.attemptToRegisterUser(register);
                addPacket(new RegisterResponsePacket(success));
            }
            case LoginRequestPacket login -> {
                boolean success = server.attemptToLogInUser(login);
                if (!authenticated) {
                    if (success) {
                        server.register(this);
                        authenticated = true;
                        username = login.getUsername();
                    }
                    addPacket(new LoginResponsePacket(success));
                }
            }
            default -> log.warn("Unexpected packet: {}", packet);
        }
    }

    public void addPacket(AbstractPacket packet) {
        queuedPackets.add(packet);
    }
}
