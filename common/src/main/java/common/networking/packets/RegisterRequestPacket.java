package common.networking.packets;

public class RegisterRequestPacket extends LoginRequestPacket {
    public RegisterRequestPacket(String username, String password) {
        super(username, password);
    }

    public RegisterRequestPacket() {
    }
}
