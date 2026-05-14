package server.commands;

import common.networking.packets.MessageToServerPacket;
import org.apache.logging.log4j.util.TriConsumer;
import server.ConnectionHandler;
import server.ServerCommand;

import java.util.function.BiConsumer;

public class NewChannelCommand implements ServerCommand {

    private BiConsumer<String, Boolean> createChannel;
    private TriConsumer<String, String, Boolean> addUser; // see on millegipärast pärit log4j paketist, aga kasutame ära

    public NewChannelCommand(BiConsumer<String, Boolean> createChannel, TriConsumer<String, String, Boolean> addUser) {
        this.createChannel = createChannel;
        this.addUser = addUser;
    }

    @Override
    public void run(MessageToServerPacket msg, ConnectionHandler conn) {
        String[] args = msg.getContent().split("\\s+");
        if (args.length < 2) {
            return;
        }

        String channelName = args[1];
        if (!channelName.startsWith("#")) {
            return;
        }

        // Teeme uue kanali
        createChannel.accept(args[1], false);

        // Anname kasutajale õigused kanalis
        addUser.accept(conn.getUsername(), args[1], true);

        // uuendame se
    }

    @Override
    public String description() {
        return "Create a new channel.";
    }

    @Override
    public String prefix() {
        return "/newchannel";
    }
}
