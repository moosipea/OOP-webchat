package server;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.networking.AbstractPacket;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionHandler implements Runnable, Closeable {
    private final LinkedBlockingQueue<AbstractPacket> queuedPackets = new LinkedBlockingQueue<>();
    private final CopyOnWriteArraySet<ConnectionHandler> allConnectionHandlers;
    private final Socket clientSocket;

    public ConnectionHandler(CopyOnWriteArraySet<ConnectionHandler> allConnectionHandlers, Socket clientSocket) {
        this.allConnectionHandlers = allConnectionHandlers;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        allConnectionHandlers.add(this);


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

            queueClientMessage("Welcome to the test server!");

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
            close();
        }
    }

    @Override
    public void close() {
        allConnectionHandlers.remove(this);
    }

    private void queueClientMessage(String message) {
        // TODO
        // queuedPackets.add(message);
    }

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
