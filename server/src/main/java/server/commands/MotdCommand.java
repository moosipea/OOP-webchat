package server.commands;

import common.networking.packets.MessageToClientPacket;
import common.networking.packets.MessageToServerPacket;
import common.networking.packets.PackagedPacket;
import server.ConnectionHandler;
import server.ServerCommand;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MotdCommand implements ServerCommand {
    private static final String MOTD =
            """
            * =============================================== *
            * LTAT.03.03 Objektorienteeritud programmeerimine *
            * Maailma parim webchat server! *
            * Tilbert Tolber Wolbert Nolbert *
            * =============================================== *
            """;

    @Override
    public void run(MessageToServerPacket msg, ConnectionHandler conn) {
        Timestamp timestamp = Timestamp.from(Instant.now());
        List<MessageToClientPacket> lines = new ArrayList<>();

        for (String line : MOTD.split("\n")) {
            lines.add(new MessageToClientPacket(msg.getTargetChannel(), null, line, timestamp));
        }

        conn.addPacket(new PackagedPacket(lines));
    }

    @Override
    public String description() {
        return "Print the message of the day.";
    }

    @Override
    public String prefix() {
        return "/motd";
    }
}
