package server.commands;

import common.networking.packets.MessageToServerPacket;
import server.ConnectionHandler;
import server.ServerCommand;

public class WhisperCommand implements ServerCommand {
    @Override
    public boolean run(MessageToServerPacket msg, ConnectionHandler conn) {
        return false;
    }
}
