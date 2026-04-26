package server;

import common.networking.packets.LoginRequestPacket;
import common.networking.packets.MessageToClientPacket;
import common.networking.packets.RegisterRequestPacket;
import common.networking.packets.RequestHistoryPacket;

import java.sql.Timestamp;
import java.util.List;

public interface ChatDataStore {
    void saveMessage(MessageToClientPacket message);

    void saveChannel(String channelName);

    List<String> getChannels(String forWhom);

    // TODO: saata kliendile vahepeal saadetud sõnumeid
    List<MessageToClientPacket> retrieveMessages(RequestHistoryPacket packet);

    // TODO: List<MessageToClientPacket> retrieveLatestMessages(String channelName);

    boolean attemptToRegisterUser(RegisterRequestPacket registerPacket);
    boolean attemptToLogInUser(LoginRequestPacket loginPacket);
}
