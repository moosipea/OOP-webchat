package common.networking;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.networking.packets.AbstractPacket;
import common.networking.packets.PackagedPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class DuplexConnection {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final JsonFactory jsonFactory = objectMapper.getFactory();
    private static final Logger log = LogManager.getLogger(DuplexConnection.class);

    private final Socket socket;
    private final LinkedBlockingQueue<AbstractPacket> queuedPackets;

    private final CountDownLatch timeToBlowUpSignal = new CountDownLatch(1);

    public DuplexConnection(Socket socket, LinkedBlockingQueue<AbstractPacket> queuedPackets) {
        this.socket = socket;
        this.queuedPackets = queuedPackets;
    }

    public void runConnection(Consumer<AbstractPacket> handler) throws IOException {
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            Thread receiver = Thread.ofVirtual().start(() -> {
                try {
                    Reader reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
                    JsonParser jsonParser = jsonFactory.createParser(reader);
                    while (jsonParser.nextToken() != null && !Thread.currentThread().isInterrupted()) {
                        if (jsonParser.currentToken() == JsonToken.START_OBJECT) {
                            AbstractPacket packet = objectMapper.readValue(jsonParser, AbstractPacket.class);
                            handler.accept(packet);
                            // TODO: handle funktsiooni asemel oleks võib-olla parem teha mingid subscriber asjad
                        }
                    }
                } catch (IOException e) {
                    log.error("IO exception on receiving end, ending connection: {}", e.getMessage());
                } finally {
                    timeToBlowUpSignal.countDown();
                }
            });

            Thread transmitter = Thread.ofVirtual().start(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            AbstractPacket packet = queuedPackets.take();
                            if (packet instanceof PackagedPacket packaged) {
                                for (AbstractPacket p : packaged.getPackets()) {
                                    out.write(objectMapper.writeValueAsString(p));
                                }
                            } else {
                                out.write(objectMapper.writeValueAsString(packet));
                            }
                            out.flush();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } catch (JsonProcessingException e) {
                            log.warn("Garbage JSON: {}", e.getMessage());
                        }
                    }
                } catch (IOException e) {
                    log.error("IO exception transmitting end, ending connection: {}", e.getMessage());
                } finally {
                    timeToBlowUpSignal.countDown();
                }
            });

            timeToBlowUpSignal.await();
            receiver.interrupt();
            transmitter.interrupt();
        } catch (InterruptedException e) {
            // TODO: midagi siin teha
        }
    }

    // TODO: seda kutsuda siis, kui klient serverile soga annab näiteks
    public void abort() {
        timeToBlowUpSignal.countDown();
    }

    @Override
    public String toString() {
        return socket.toString();
    }
}
