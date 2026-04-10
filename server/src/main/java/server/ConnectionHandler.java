package server;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.networking.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.jmx.Server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Haldab ühe kliendi ühendust serveriga.
 */
public class ConnectionHandler implements Runnable {
    private static final Logger log = LogManager.getLogger(ConnectionHandler.class);

    // Packetite järjekord.
    private final LinkedBlockingQueue<AbstractPacket> queuedPackets = new LinkedBlockingQueue<>();

    // Viit serveri olekule
    private final ServerConnection serverConnection;
    private String username;

    // Socket, mille kaudu suhtlus kliendiga käib.
    private final Socket clientSocket;

    public ConnectionHandler(ServerConnection serverConnection, Socket clientSocket) {
        this.serverConnection = serverConnection;
        this.clientSocket = clientSocket;
    }

    /**
     * Käivitab ühenduse kliendiga.
     */
    @Override
    public void run() {
        // Registreerime oma ühenduse.
        serverConnection.register(this);

        // TODO: kogu selles asjas on vaja tagada, et see thread viisakalt
        //  ennast ära tapab siis, kui klient ühenduse katkestab.
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {
            ObjectMapper objectMapper = new ObjectMapper();

            Thread receiver = Thread.ofVirtual().start(() -> {
                try {
                    // TODO: only instantiate factory once (in common?)
                    Reader reader = new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8);
                    JsonParser jsonParser = objectMapper.getFactory().createParser(reader);
                    // sisse logimine esimesena
                    AbstractPacket packet = objectMapper.readValue(jsonParser, AbstractPacket.class);
                    switch (packet){
                        case LoginPacket p -> {
                            username = p.getUsername();
                        }
                        default ->{}
                    }

                    while (jsonParser.nextToken() != null && !Thread.currentThread().isInterrupted()) {
                        if (jsonParser.currentToken() == JsonToken.START_OBJECT) {
                            packet = objectMapper.readValue(jsonParser, AbstractPacket.class);
                            handlePacket(packet);
                        }
                    }
                } catch (IOException e) {
                    log.error(e);
                }
            });

            //while (username == null) // ootame, kuni on autentimine lõpuni jõudnud

            // Sõnumeid *saadetakse* selles lõimes.
            while (!Thread.currentThread().isInterrupted() && receiver.isAlive()) {
                AbstractPacket packetToBeSent = queuedPackets.take();
                String asString = objectMapper.writeValueAsString(packetToBeSent);
                out.write(asString);
                out.flush();
            }

            receiver.interrupt();
            receiver.join();
        } catch (IOException e) {
            log.error(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            serverConnection.unregister(this);
            try {
                clientSocket.close();
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

    /**
     * Lisab sõnumi selle ühenduse sõnumite järjekorda.
     *
     * @param message sõnum
     */
    private void queueClientMessage(MessageToClientPacket message) {
        queuedPackets.add(message);
    }

    /**
     * Edastab sõnumi kõigile ühendatud kasutajatele.
     *
     * @param message sõnum
     */
    private void broadcastMessage(MessageToServerPacket message) {
        Timestamp now = Timestamp.from(Instant.now());
        MessageToClientPacket packetToBeSent = new MessageToClientPacket(message, username, now);
        for (ConnectionHandler conn : serverConnection.getAllConnectionHandlers()) {
            conn.queueClientMessage(packetToBeSent);
        }
    }

    public void handlePacket(AbstractPacket packet) {
        switch (packet) {
            case MessageToServerPacket msg -> broadcastMessage(msg);
            case GetChannelsRequestPacket ignored -> {
                for (String channel : serverConnection.getChannelList()) {
                    queuedPackets.add(new AddChannelResponsePacket(channel));
                }
            }
            default -> {
                // TODO: Report unexpected packet
            }
        }
    }
}
