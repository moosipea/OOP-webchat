package server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.networking.AbstractPacket;
import common.networking.GetChannelsRequestPacket;
import common.networking.MessageToClientPacket;
import common.networking.MessageToServerPacket;

import java.sql.Timestamp;
import java.time.Instant;

public class ServerMain {
    public static void main(String[] args) throws JsonProcessingException {
        // MessageToServerPacket packet = new MessageToServerPacket("general", "hello, world");
        GetChannelsRequestPacket packet = new GetChannelsRequestPacket();

        ObjectMapper om = new ObjectMapper();

        String serialized = om.writeValueAsString(packet);
        AbstractPacket deserialized = om.readValue(serialized, AbstractPacket.class);

        System.out.println(deserialized);

        // Disabled for testing: new ServerConnection().run();
    }
}
