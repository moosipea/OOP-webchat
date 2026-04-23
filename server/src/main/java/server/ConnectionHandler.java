package server;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.networking.AbstractPacket;
import common.networking.AddChannelResponsePacket;
import common.networking.DuplexConnection;
import common.networking.GetChannelsRequestPacket;
import common.networking.LoginPacket;
import common.networking.MessageToClientPacket;
import common.networking.MessageToServerPacket;
import common.objects.Message;

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
        if (!authenticated && !(packet instanceof LoginPacket)) {
            return;
        }

        switch (packet) {
            case MessageToServerPacket msg ->
                    server.broadcastMessage(msg, username);
            case GetChannelsRequestPacket channelRequest -> {
                if (channelRequest.getRequestList()){
                    for (String channel : server.getChannelList()) {
                        addPacket(new AddChannelResponsePacket(channel));
                    }
                }
                else{
                    String channel = channelRequest.getChannel();
                    List<Message> messages = server.getMessages(channel);
                    for (Message message : messages){
                        if ((channelRequest.getBefore() == null || message.time.compareTo(channelRequest.getBefore()) <= 0) && 
                            (channelRequest.getNotBefore() == null || message.time.compareTo(channelRequest.getNotBefore()) > 0))
                        addPacket(new MessageToClientPacket(channel, message.user, message.content, message.time, message.id));
                    }
                }
            }
            case LoginPacket login -> {
                if (!authenticated) {
                    // TODO: magic check here
                    server.register(this);
                    authenticated = true;
                    username = login.getUsername();
                }
            }
            default -> log.warn("Unexpected packet: {}", packet);
        }
    }

    public void addPacket(AbstractPacket packet) {
        queuedPackets.add(packet);
    }
}
