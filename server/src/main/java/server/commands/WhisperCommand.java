package server.commands;

import common.networking.packets.MessageToClientPacket;
import common.networking.packets.MessageToServerPacket;
import server.ConnectionHandler;
import server.ServerCommand;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WhisperCommand implements ServerCommand {
    // TODO: siin me lubame ainult ASCII kasutajanimesid, see pole vist
    //  andmebaasi ega kliendipoolse formatteriga kooskõlastatud.
    private static final Pattern pattern = Pattern.compile("^/whisper\\s+([a-zA-Z0-9]+)\\s+(.*)$");

    private final BiFunction<String, MessageToClientPacket, Boolean> sendWhisper;

    public WhisperCommand(BiFunction<String, MessageToClientPacket, Boolean> sendWhisper) {
        this.sendWhisper = sendWhisper;
    }

    private static MessageToClientPacket createWhisper(String targetChannel, String sender, String content, Timestamp timestamp) {
        return new MessageToClientPacket(
                targetChannel,
                null,
                String.format("***%s** whispers to you*: %s", sender, content),
                timestamp
        );
    }

    private static MessageToClientPacket createWhisperReply(String targetChannel, String target, String content, Timestamp timestamp) {
        return new MessageToClientPacket(
                targetChannel,
                null,
                String.format("*You whisper to **%s***: %s", target, content),
                timestamp
        );
    }

    @Override
    public boolean run(MessageToServerPacket msg, ConnectionHandler conn) {
        Matcher m = pattern.matcher(msg.getContent());

        if (m.find()) {
            String username = m.group(1);
            String content = m.group(2);

            MessageToClientPacket whisper = createWhisper(
                    msg.getTargetChannel(),
                    conn.getUsername(),
                    content,
                    Timestamp.from(Instant.now())
            );

            if (sendWhisper.apply(username, whisper)) {
                conn.addPacket(createWhisperReply(
                        msg.getTargetChannel(),
                        username,
                        content,
                        Timestamp.from(Instant.now()))
                );
            } else {
                conn.addPacket(new MessageToClientPacket(
                        msg.getTargetChannel(),
                        null,
                        "*That user doesn't seem to be online right now.*",
                        Timestamp.from(Instant.now())
                ));
            }

            return true;
        }

        return false;
    }

    @Override
    public String description() {
        return "Whisper something to another user privately.";
    }
}
