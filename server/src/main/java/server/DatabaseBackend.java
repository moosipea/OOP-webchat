package server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import common.networking.packets.LoginRequestPacket;
import common.networking.packets.MessageToClientPacket;
import common.networking.packets.RegisterRequestPacket;
import common.networking.packets.RequestHistoryPacket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public class DatabaseBackend implements ChatDataStore, AutoCloseable {
    private static final Logger log = LogManager.getLogger(DatabaseBackend.class);
    private final HikariDataSource dataSource;

    public DatabaseBackend() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:chat.db");
        config.setMaximumPoolSize(16);
        dataSource = new HikariDataSource(config);
        createDatabase();
    }

    @Override
    public void saveMessage(MessageToClientPacket message) {
        try (
                Connection db = dataSource.getConnection();
                PreparedStatement st = db.prepareStatement(
                        """
                            INSERT INTO messages (content, message_timestamp, author, channel)
                            VALUES (
                                ?,
                                ?,
                                (SELECT users.user_id
                                 FROM users
                                 WHERE users.username = ?),
                                (SELECT channels.channel_id
                                 FROM channels
                                 WHERE channels.channel_name = ?)
                            )
                        """
                )
        ) {
            st.setString(1, message.getContent());
            st.setLong(2, message.getTimestamp().getTime());
            st.setString(3, message.getUser());
            st.setString(4, message.getTargetChannel());
            st.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to save message: {}", e.getMessage());
        }
    }

    // TODO: mõelda, mida teha juhul, kui sama nimega kanal juba eksisteerib?
    //  Vahest peaks seda siiski raporteerima.
    @Override
    public void saveChannel(String channelName, boolean publicChannel) {
        try (
                Connection db = dataSource.getConnection();
                PreparedStatement st = db.prepareStatement(
                        """
                            INSERT INTO channels (channel_name, is_private)
                            VALUES (
                                ?,
                                ?
                            )
                            ON CONFLICT (channel_name) DO NOTHING;
                        """
                )
        ) {
            st.setString(1, channelName);
            st.setBoolean(2, !publicChannel);
            st.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to save channel: {}", e.getMessage());
        }
    }

    @Override
    public boolean addUserToChannel(String username, String channel, boolean hasPerms) {
        try (
                Connection db = dataSource.getConnection();
                PreparedStatement st = db.prepareStatement(
                        """
                        INSERT INTO users_channels (channel, theuser, has_perms)
                        VALUES (
                            (SELECT channel_id FROM channels WHERE channel_name = ?),
                            (SELECT user_id FROM users WHERE username = ?),
                            ?
                        )
                        """
                )
        ) {
            st.setString(1, channel);
            st.setString(2, username);
            st.setBoolean(3, hasPerms);
            st.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to add user '{}' to channel '{}' (hasPerms={}): {}", username, channel, hasPerms, e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public List<String> getChannels(String forWhom) {
        List<String> channels = new ArrayList<>();

        try (
                Connection db = dataSource.getConnection();
                PreparedStatement st = db.prepareStatement(
                        """
                            SELECT c.channel_name
                            FROM users_channels uc
                                JOIN users u on uc.theuser = u.user_id
                                JOIN channels c ON uc.channel = c.channel_id
                            WHERE u.username = ?
                        """
                )
        ) {
            st.setString(1, forWhom);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                String channelName = rs.getString(1);
                channels.add(channelName);
            }
        } catch (SQLException e) {
            log.error("Failed to get channel list: {}", e.getMessage());
        }

        // TODO: tühja listi asemel võiks exceptioni teha
        return channels;
    }

    @Override
    public List<MessageToClientPacket> retrieveMessages(RequestHistoryPacket packet) {
        List<MessageToClientPacket> messages = new ArrayList<>();

        try (
                Connection db = dataSource.getConnection();
                PreparedStatement st = db.prepareStatement(
                        """
                            SELECT *
                            FROM (
                                SELECT messages.content,
                                    users.username,
                                    channels.channel_name,
                                    messages.message_timestamp
                                FROM messages
                                JOIN users ON users.user_id = messages.author
                                JOIN channels ON channels.channel_id = messages.channel
                                WHERE channels.channel_name = ?
                                    AND messages.message_timestamp > ?
                                    AND messages.message_timestamp <= ?
                                ORDER BY messages.message_timestamp DESC
                                LIMIT 100
                            ) AS recent_messages
                            ORDER BY message_timestamp ASC;
                        """
                )
        ) {

            st.setString(1, packet.getChannel());
            long notBefore = 0;
            if (packet.getNotBefore() != null) {
                notBefore = packet.getNotBefore().toEpochMilli();
            }
            st.setLong(2, notBefore);
            long before = Long.MAX_VALUE;
            if (packet.getNotBefore() != null) {
                before = packet.getBefore().toEpochMilli();
            }
            st.setLong(3, before);

            ResultSet resultSet = st.executeQuery();
            while (resultSet.next()) {
                String content = resultSet.getString(1);
                String author = resultSet.getString(2);
                String channel = resultSet.getString(3);
                Timestamp timestamp = Timestamp.from(Instant.ofEpochSecond(resultSet.getLong(4)));
                messages.add(new MessageToClientPacket(channel, author, content, timestamp));
            }

        } catch (SQLException e) {
            log.error("Failed to query messages: {}", e.getMessage());
        }

        return messages;
    }

    @Override
    public boolean attemptToRegisterUser(RegisterRequestPacket registerPacket) {
        try (
                Connection db = dataSource.getConnection();
                PreparedStatement st = db.prepareStatement(
                        """
                          INSERT INTO users (username, password_hash)
                          VALUES (
                            ?,
                            ?
                          )
                        """
                )
        ) {
            st.setString(1, registerPacket.getUsername());
            st.setBlob(2, new ByteArrayInputStream(registerPacket.getPasswordHash()));
            st.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to register user: {}", e.getMessage());
            return false;
        }

        log.info("Registered user {}", registerPacket.getUsername());
        return true;
    }

    @Override
    public boolean attemptToLogInUser(LoginRequestPacket loginPacket) {
        try (
                Connection db = dataSource.getConnection();
                PreparedStatement st = db.prepareStatement(
                        """
                        SELECT count(*)
                        FROM users
                        WHERE username = ? AND password_hash = ?
                        """
                )
        ) {
            st.setString(1, loginPacket.getUsername());
            st.setBlob(2, new ByteArrayInputStream(loginPacket.getPasswordHash()));
            ResultSet rs = st.executeQuery();

            if (rs.getInt(1) == 1) {
                return true;
            }

        } catch (SQLException e) {
            log.error("Failed to log in user: {}", e.getMessage());
        }

        return false;
    }

    @Override
    public Set<String> getChannelUsers(String channel) {
        Set<String> allowedUsers = new HashSet<>();

        try (
                Connection db = dataSource.getConnection();
                PreparedStatement st = db.prepareStatement(
                        """
                        SELECT u.username
                        FROM users_channels uc
                            JOIN users u ON uc.theuser = u.user_id
                            JOIN channels c ON uc.channel = c.channel_id
                        WHERE c.channel_name = ?
                        """
                )
        ) {
            st.setString(1, channel);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                allowedUsers.add(rs.getString(1));
            }

            return allowedUsers;
        } catch (SQLException e) {
            log.error("Failed to get channel users: {}", e.getMessage());
        }

        return allowedUsers;
    }

    private void createDatabase() throws SQLException {
        try (
                Connection db = dataSource.getConnection();
                Statement st = db.createStatement()
        ) {
            st.addBatch(
                    """
                    CREATE TABLE IF NOT EXISTS messages (
                        message_id INTEGER PRIMARY KEY NOT NULL UNIQUE,
                        content TEXT NOT NULL,
                        message_timestamp INTEGER NOT NULL,
                        author INTEGER NOT NULL,
                        channel INTEGER NOT NULL,
                        FOREIGN KEY (author) REFERENCES users(user_id),
                        FOREIGN KEY (channel) REFERENCES channels(channel_id)
                    )
                    """
            );

            st.addBatch(
                    """
                    CREATE TABLE IF NOT EXISTS users (
                        user_id INTEGER PRIMARY KEY NOT NULL UNIQUE,
                        username TEXT NOT NULL UNIQUE,
                        password_hash BLOB NOT NULL,
                        is_admin INTEGER DEFAULT 0 NOT NULL,
                        CONSTRAINT check_username_length CHECK (length(username) > 0),
                        CONSTRAINT check_hash_length CHECK (length(password_hash) = 32)
                    );
                    """
            );

            st.addBatch(
                    """
                    CREATE TABLE IF NOT EXISTS channels (
                        channel_id INTEGER PRIMARY KEY NOT NULL UNIQUE,
                        channel_name TEXT NOT NULL UNIQUE,
                        is_private INTEGER DEFAULT 0 NOT NULL,
                        CONSTRAINT check_channel_name_length CHECK (length(channel_name) > 0)
                    )
                    """
            );

            st.addBatch(
                    """
                    CREATE TABLE IF NOT EXISTS users_channels (
                        channel INTEGER NOT NULL,
                        theuser INTEGER NOT NULL,
                        has_perms INTEGER DEFAULT 0 NOT NULL,
                        FOREIGN KEY (channel) REFERENCES channels(channel_id),
                        FOREIGN KEY (theuser) REFERENCES users(user_id),
                        CONSTRAINT unique_channel_user UNIQUE (channel, theuser)
                    )
                    """
            );

            // Andmebaasi triger, mis lisab registreerunud kasutaja
            // kõikidesse avalikesse kanalitesse.
            st.addBatch(
                    """
                    CREATE TRIGGER IF NOT EXISTS tg_add_to_public
                    AFTER INSERT
                    ON users
                    FOR EACH ROW
                    BEGIN
                        INSERT INTO users_channels (channel, theuser)
                        SELECT channel_id, NEW.user_id
                        FROM channels
                        WHERE NOT is_private;
                    END;
                    """
            );

            // Andmebaasi triger, mis lisab kõik kasutajad äsja loodud
            // kanalisse, kui see on avalik.
            // Siin peab natuke süntaksiga nihverdama, kuna SQLite ei luba
            // kõiki asju trigerite sees.
            st.addBatch(
                    """
                    CREATE TRIGGER IF NOT EXISTS tg_add_to_new_public
                    AFTER INSERT
                    ON channels
                    FOR EACH ROW
                    WHEN NOT NEW.is_private
                    BEGIN
                        INSERT INTO users_channels (channel, theuser)
                        SELECT NEW.channel_id, user_id
                        FROM users
                        WHERE NOT EXISTS (
                            SELECT 1
                            FROM users_channels
                            WHERE channel = NEW.channel_id
                              AND theuser = users.user_id
                        );
                    END;
                    """
            );

            // TODO: võiks errorit kontrollida, aga eriti vahet pole tabeli loomisel
            st.executeBatch();
        }
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
