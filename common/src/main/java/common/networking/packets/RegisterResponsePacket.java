package common.networking.packets;

public class RegisterResponsePacket extends LoginResponsePacket {
    public RegisterResponsePacket(boolean success) {
        super(success);
    }

    public RegisterResponsePacket() {
    }
}
