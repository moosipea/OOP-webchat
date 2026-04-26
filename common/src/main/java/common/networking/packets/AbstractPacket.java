package common.networking.packets;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AddChannelResponsePacket.class, name = "add_channel_response"),
        @JsonSubTypes.Type(value = GetChannelsRequestPacket.class, name = "get_channels_request"),
        @JsonSubTypes.Type(value = MessageToClientPacket.class, name = "message_to_client"),
        @JsonSubTypes.Type(value = MessageToServerPacket.class, name = "message_to_server"),
        @JsonSubTypes.Type(value = LoginRequestPacket.class, name = "login_request"),
        @JsonSubTypes.Type(value = LoginResponsePacket.class, name = "login_response"),
        @JsonSubTypes.Type(value = RegisterRequestPacket.class, name = "register_request"),
        @JsonSubTypes.Type(value = RegisterResponsePacket.class, name = "register_response"),
        @JsonSubTypes.Type(value = RequestHistoryPacket.class, name = "history_request"),
})
public abstract class AbstractPacket {
}
