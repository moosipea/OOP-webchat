package common.networking;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class DuplexConnection {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static JsonFactory jsonFactory = objectMapper.getFactory();
    private static final Logger log = LogManager.getLogger(DuplexConnection.class);

    private final Socket socket;
    private final LinkedBlockingQueue<AbstractPacket> queuedPackets = new LinkedBlockingQueue<>();

    public DuplexConnection(Socket socket) {
        this.socket = socket;
    }

    public void runConnection(Consumer<AbstractPacket> handler) throws IOException {
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            CountDownLatch timeToBlowUpSignal = new CountDownLatch(1);

            Thread receiver = Thread.ofVirtual().start(() -> {
                try {
                    Reader reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
                    JsonParser jsonParser = jsonFactory.createParser(reader);
                    while (jsonParser.nextToken() != null && !Thread.currentThread().isInterrupted()) {
                        if (jsonParser.currentToken() == JsonToken.START_OBJECT) {
                            AbstractPacket packet = objectMapper.readValue(jsonParser, AbstractPacket.class);
                            handler.accept(packet);
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
                            AbstractPacket packetToBeSent = queuedPackets.take();
                            String packet = objectMapper.writeValueAsString(packetToBeSent);
                            out.write(packet);
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

    public void addPacket(AbstractPacket packet) {
        queuedPackets.add(packet);
    }
}
