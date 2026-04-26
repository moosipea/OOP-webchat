package common.networking.packets;

public class LoginResponsePacket extends AbstractPacket {
    private boolean success;

    public LoginResponsePacket(boolean success) {
        this.success = success;
    }

    public LoginResponsePacket() {
    }

    public boolean isSuccess() {
        return success;
    }
}
