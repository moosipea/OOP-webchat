package server;

import common.networking.MessageToClientPacket;

import java.sql.Timestamp;
import java.util.List;

public interface ChatDataStore {
    void saveMessage(MessageToClientPacket message);

    void saveChannel(String channelName);

    List<MessageToClientPacket> retrieveMessages(String channelName, Timestamp from);

    // List<MessageToClientPacket> retrieveLatestMessages(String channelName);
}
