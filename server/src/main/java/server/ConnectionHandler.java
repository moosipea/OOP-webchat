package server;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.networking.AbstractPacket;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Haldab ühe kliendi ühendust serveriga.
 */
public class ConnectionHandler implements Runnable, Closeable {
    // Packetite järjekord.
    private final LinkedBlockingQueue<AbstractPacket> queuedPackets = new LinkedBlockingQueue<>();

    // Viit kõikide aktiivsete ühenduste hulgale (vajame seda registreerimiseks).
    private final CopyOnWriteArraySet<ConnectionHandler> allConnectionHandlers;

    // Socket, mille kaudu suhtlus kliendiga käib.
    private final Socket clientSocket;

    public ConnectionHandler(CopyOnWriteArraySet<ConnectionHandler> allConnectionHandlers, Socket clientSocket) {
        this.allConnectionHandlers = allConnectionHandlers;
        this.clientSocket = clientSocket;
    }

    /**
     * Käivitab ühenduse kliendiga.
     */
    @Override
    public void run() {
        // Registreerime oma ühenduse.
        register();

        // TODO: kogu selles asjas on vaja tagada, et see thread viisakalt
        //  ennast ära tapab siis, kui klient ühenduse katkestab.
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonParser jsonParser = objectMapper.getFactory().createParser(clientSocket.getInputStream());

            Thread receiver = Thread.ofVirtual().start(() -> {
                try {
                    while (jsonParser.nextToken() != null && !Thread.currentThread().isInterrupted()) {
                        if (jsonParser.currentToken() == JsonToken.START_OBJECT) {
                            AbstractPacket packet = objectMapper.readValue(jsonParser, AbstractPacket.class);
                            handlePacket(packet);
                        }
                    }
                } catch (IOException e) {
                    // TODO: log error, cancel connection
                }
            });

            // Siia võib panna mingi muu welcome sõnumi või mis iganes peab
            // toimuma siis, kui klient ühendab.
            queueClientMessage("Welcome to the test server!");

            // Sõnumeid *saadetakse* selles lõimes.
            while (!Thread.currentThread().isInterrupted()) {
                AbstractPacket packetToBeSent = queuedPackets.take();
                objectMapper.writeValue(out, packetToBeSent);
                out.flush();
            }

            receiver.interrupt();
        } catch (IOException ignored) {
            // TODO: log exception, but don't crash!
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            unregister();
        }
    }

    /**
     * Lisab selle ühenduse aktiivsete ühenduste hulka.
     */
    private void register() {
        allConnectionHandlers.add(this);
    }

    /**
     * Eemaldab selle ühenduse aktiivsete ühenduste hulgast.
     */
    private void unregister() {
        allConnectionHandlers.remove(this);
    }

    /**
     * Lisab sõnumi selle ühenduse sõnumite järjekorda.
     *
     * @param message sõnum sõnena
     */
    private void queueClientMessage(String message) {
        // TODO
        // queuedPackets.add(message);
    }

    /**
     * Edastab antud sõnumi kõigile teistele ühendatud klientidele.
     *
     * @param message    sõnum sõnena
     * @param ignoreSelf kas on tarvis ka saatjale tagasi saata?
     */
    private void broadcastMessage(String message, boolean ignoreSelf) {
        for (ConnectionHandler conn : allConnectionHandlers) {
            if (ignoreSelf && conn == this) {
                continue;
            }
            conn.queueClientMessage(message);
        }
    }

    public void handlePacket(AbstractPacket packet) {

    }
}
