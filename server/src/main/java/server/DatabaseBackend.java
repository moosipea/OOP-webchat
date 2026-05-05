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
import java.util.ArrayList;
import java.util.List;

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
                        """,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {
            st.setString(1, message.getContent());
            st.setLong(2, message.getTimestampMillis());
            st.setString(3, message.getUser());
            st.setString(4, message.getTargetChannel());
            st.executeUpdate();
            int affectedRows = st.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = st.getGeneratedKeys()) {
                    if (rs.next()) {
                        message.setId(rs.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Failed to save message: {}", e.getMessage());
        }
    }

    // TODO: mõelda, mida teha juhul, kui sama nimega kanal juba eksisteerib?
    //  Vahest peaks seda siiski raporteerima.
    @Override
    public void saveChannel(String channelName) {
        try (
                Connection db = dataSource.getConnection();
                PreparedStatement st = db.prepareStatement(
                        """
                            INSERT INTO channels (channel_name)
                            VALUES (
                                ?
                            )
                            ON CONFLICT (channel_name) DO NOTHING;
                        """
                )
        ) {
            st.setString(1, channelName);
            st.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to save channel: {}", e.getMessage());
        }
    }

    @Override
    public List<String> getChannels(String forWhom) {
        List<String> channels = new ArrayList<>();

        // TODO: teeme tabeli, mis seob kasutaja kanaliga. Siis saame teha
        //  sellise SQL päringu, mis tagastab ainult need kanalid, milles
        //  sellel kasutajal on lubatud rääkida.
        try (
                Connection db = dataSource.getConnection();
                PreparedStatement st = db.prepareStatement(
                        """
                            SELECT channel_name
                            FROM channels
                        """
                )
        ) {
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
                                SELECT messages.message_id,
                                    messages.content,
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
                long id = resultSet.getLong(1);
                String content = resultSet.getString(2);
                String author = resultSet.getString(3);
                String channel = resultSet.getString(4);
                System.out.println(content + " " + channel);
                long timestamp = resultSet.getLong(4);

                messages.add(new MessageToClientPacket(channel, author, content, timestamp, id));
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

    private void createDatabase() throws SQLException {
        // TODO: kas see kõik peaks olema transaction? korrektsuse mõttes
        //  vist küll, aga praktikas vahet pole
        // TODO: siia panna lätaki ressurssid
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
                        CONSTRAINT check_channel_name_length CHECK (length(channel_name) > 0)
                    )
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
