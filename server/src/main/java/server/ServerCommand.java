package server;

import common.networking.packets.MessageToServerPacket;

/**
 * Liides serveri käskude jaoks. Kui käsk tagastab {@code true}, siis
 * tähenedab, et läks läbi.
 */
@FunctionalInterface
public interface ServerCommand {
    boolean run(MessageToServerPacket msg, ConnectionHandler conn);
}
