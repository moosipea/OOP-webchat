package common.networking.packets;

public class RegisterResponsePacket extends AbstractPacket {
    private boolean success;

    public RegisterResponsePacket(boolean success) {
        this.success = success;
    }

    public RegisterResponsePacket() {
    }

    public boolean isSuccess() {
        return success;
    }
}
