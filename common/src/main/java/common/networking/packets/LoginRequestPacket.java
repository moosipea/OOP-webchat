package common.networking.packets;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginRequestPacket extends AbstractPacket {
    private String username;
    private byte[] passwordHash;

    public LoginRequestPacket(String username, String password) throws NoSuchAlgorithmException {
        this.username = username;

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String combo = username + password;

        this.passwordHash = digest.digest(combo.getBytes(StandardCharsets.UTF_8));
    }

    public LoginRequestPacket() {
    }

    public String getUsername() {
        return username;
    }

    public byte[] getPasswordHash() {
        return passwordHash;
    }
}
