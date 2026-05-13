package server.commands;

import common.networking.packets.MessageToServerPacket;
import server.ConnectionHandler;
import server.ServerCommand;

public class NewChannelCommand implements ServerCommand {
    @Override
    public void run(MessageToServerPacket msg, ConnectionHandler conn) {
        // TODO
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
