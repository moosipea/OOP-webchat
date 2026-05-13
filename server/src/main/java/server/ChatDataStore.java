package server;

import common.networking.packets.LoginRequestPacket;
import common.networking.packets.MessageToClientPacket;
import common.networking.packets.RegisterRequestPacket;
import common.networking.packets.RequestHistoryPacket;

import java.util.List;
import java.util.Set;

public interface ChatDataStore {
    void saveMessage(MessageToClientPacket message);

    void saveChannel(String channelName, boolean publicChannel);

    List<String> getChannels(String forWhom);

    List<MessageToClientPacket> retrieveMessages(RequestHistoryPacket packet);

    boolean attemptToRegisterUser(RegisterRequestPacket registerPacket);

    boolean attemptToLogInUser(LoginRequestPacket loginPacket);

    Set<String> getChannelUsers(String channel);
}
