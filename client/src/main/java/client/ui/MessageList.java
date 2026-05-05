package client.ui;

import java.time.Instant;

import javafx.scene.layout.VBox;

/**
 * Sisuliselt lihtsalt wrapper VBox'i ümber.
 * TODO: See peaks dünaamiliselt fetchima kuskilt andmebaasist.
 *  Aga ma ei viitsi praegu.
 */
public class MessageList extends VBox {
    /**
     * Lisab uue sõnumi UIsse.
     *
     * @param author  autor
     * @param content sõnum
     */
    public void addMessage(String author, String content, Instant time, long id) {
        getChildren().add(new MessageComponent(author, content, time, id));
    }
}
