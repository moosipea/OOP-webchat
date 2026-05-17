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

public class HelpCommand implements ServerCommand {
    List<ServerCommand> commands;

    public HelpCommand(List<ServerCommand> commands) {
        this.commands = commands;
    }

    @Override
    public void run(MessageToServerPacket msg, ConnectionHandler conn) {
        Timestamp timestamp = Timestamp.from(Instant.now());
        List<MessageToClientPacket> lines = new ArrayList<>();

        for (ServerCommand cmd : commands) {
            String line = cmd.prefix() + " - " + cmd.description();
            lines.add(new MessageToClientPacket(msg.getTargetChannel(), null, line, timestamp));
        }

        conn.addPacket(new PackagedPacket(lines));
    }

    @Override
    public String description() {
        return "Print this menu.";
    }

    @Override
    public String prefix() {
        return "/help";
    }
}
