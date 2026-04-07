package client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.networking.AbstractPacket;
import common.networking.MessageToServerPacket;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class ClientConnection implements Runnable {
    private final InetAddress ip;
    private final int port;

    private final LinkedBlockingQueue<AbstractPacket> queuedPackets = new LinkedBlockingQueue<>();
    private Consumer<String> onMessageReceived = null;

    public ClientConnection(String ip, String port) throws UnknownHostException {
        this.ip = InetAddress.getByName(ip);
        this.port = Integer.parseInt(port);
    }

    @Override
    public void run() {

        try (Socket sock = new Socket(ip, port)) {
            // TODO: siin on hästi sarnane kood serveri ConnectionHandler
            //  klassile, äkki saaks mingi ilusama abstraktsiooni teha?
            try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()))) {

                ObjectMapper objectMapper = new ObjectMapper();
                JsonParser jsonParser = objectMapper.getFactory().createParser(sock.getInputStream());

                // TODO: See ei tööta. Miks?
                Thread receiver = Thread.ofVirtual().start(() -> {
                    try {
                        while (jsonParser.nextToken() != null && !Thread.currentThread().isInterrupted()) {
                            if (jsonParser.currentToken() == JsonToken.START_OBJECT) {
                                AbstractPacket packet = objectMapper.readValue(jsonParser, AbstractPacket.class);
                                handlePacket(packet);
                            }
                        }
                    } catch (IOException e) {
                        // todo: log exception, cancel connection
                    }
                });

                while (!Thread.currentThread().isInterrupted()) {
                    AbstractPacket packetToBeSent = queuedPackets.take();
                    objectMapper.writeValue(out, packetToBeSent);
                    out.flush();
                }

                receiver.interrupt();
                receiver.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setOnMessageReceived(Consumer<String> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public void sendMessage(String targetChannel, String message) {
        // TODO: check for null
        queuedPackets.add(new MessageToServerPacket(targetChannel, message));
    }

    public void handlePacket(AbstractPacket packet) {

    }
}
