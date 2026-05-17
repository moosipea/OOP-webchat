package server;

import common.networking.packets.MessageToServerPacket;

public interface ServerCommand {
    void run(MessageToServerPacket msg, ConnectionHandler conn);

    String description();

    String prefix();
}
