package server.commands;

import common.networking.packets.MessageToClientPacket;
import common.networking.packets.MessageToServerPacket;
import common.networking.packets.PackagedPacket;
import server.ConnectionHandler;
import server.ServerCommand;

import java.util.ArrayList;
import java.util.List;

public class MotdCommand implements ServerCommand {
    private static String MOTD =
            """
            * =============================================== *
            * LTAT.03.03 Objektorienteeritud programmeerimine *
            * Maailma parim webchat server! *
            * Tilbert Tolber Wolbert Nolbert *
            * =============================================== *
            """;

    @Override
    public boolean run(MessageToServerPacket msg, ConnectionHandler conn) {
        if (!msg.getContent().startsWith("/motd")) {
            return false;
        }

        List<MessageToClientPacket> lines = new ArrayList<>();

        for (String line : MOTD.split("\n")) {
            lines.add(new MessageToClientPacket(msg.getTargetChannel(), null, line, null));
        }

        conn.addPacket(new PackagedPacket(lines));
        return true;
    }

    @Override
    public String description() {
        return "Print the message of the day.";
    }
}
