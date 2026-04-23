package server;

import common.networking.packets.LoginRequestPacket;
import common.networking.packets.MessageToClientPacket;
import common.networking.packets.RegisterRequestPacket;

import java.sql.Timestamp;
import java.util.List;

public interface ChatDataStore {
    void saveMessage(MessageToClientPacket message);

    void saveChannel(String channelName);

    List<MessageToClientPacket> retrieveMessages(String channelName, Timestamp from);

    // TODO
    // List<MessageToClientPacket> retrieveLatestMessages(String channelName);

    boolean attemptToRegisterUser(RegisterRequestPacket registerPacket);
    boolean attemptToLogInUser(LoginRequestPacket loginPacket);
}
