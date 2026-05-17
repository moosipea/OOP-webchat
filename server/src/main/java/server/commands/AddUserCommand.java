package server.commands;

import common.networking.packets.MessageToServerPacket;
import org.apache.logging.log4j.util.TriConsumer;
import server.ConnectionHandler;
import server.ServerCommand;

import java.util.function.BiPredicate;

public class AddUserCommand implements ServerCommand {
    private final BiPredicate<String, String> checkHasPerms;
    private final TriConsumer<String, String, Boolean> addUserToChannel;

    public AddUserCommand(BiPredicate<String, String> checkHasPerms, TriConsumer<String, String, Boolean> addUserToChannel) {
        this.checkHasPerms = checkHasPerms;
        this.addUserToChannel = addUserToChannel;
    }

    @Override
    public void run(MessageToServerPacket msg, ConnectionHandler conn) {
        String[] args = msg.getContent().split("\\s+");

        if (args.length != 3) {
            return;
        }

        String channel = args[1];
        String user = args[2];

        if (checkHasPerms.test(conn.getUsername(), channel)) {
            addUserToChannel.accept(user, channel, false);
        }
    }

    @Override
    public String description() {
        return "Adds a user to the specified channel";
    }

    @Override
    public String prefix() {
        return "/adduser";
    }
}
