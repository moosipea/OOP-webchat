package client.ui;

import java.util.function.BiConsumer;

import javafx.scene.layout.VBox;

/**
 * Sisuliselt lihtsalt wrapper VBox'i ümber.
 * TODO: See peaks dünaamiliselt fetchima kuskilt andmebaasist.
 *  Aga ma ei viitsi praegu.
 */
public class MessageList extends VBox {
    private BiConsumer<Long, Long> requestHistory = null;

    /**
     * lambda, mis võtab 2 väärtust: before ja notBefore
     * mõlemad on milisekundites
     * @param requestHistory
     */
    public void setRequestHistory(BiConsumer<Long, Long> requestHistory){
        this.requestHistory = requestHistory;
    }

    public void requestHistoryAfter(){
        if (requestHistory != null){
            if (getChildren().isEmpty()){
                return;
            }
            var lastMessage = getChildren().getLast();
            if (lastMessage instanceof MessageComponent messageComponent){
                requestHistory.accept(Long.MAX_VALUE, messageComponent.time);
            }
        }
    }

    public void requestHistoryBefore(){
        if (requestHistory != null){
            if (getChildren().isEmpty()){
                return;
            }
            var firstMessage = getChildren().getFirst();
            if (firstMessage instanceof MessageComponent messageComponent){
                requestHistory.accept(messageComponent.time, Long.MIN_VALUE);
            }
        }
    }
    /**
     * Lisab uue sõnumi UIsse.
     *
     */
    public void addMessage(String author, String content, long time, long id) {
        var children = getChildren();

        for (var node : children) {
            if (node instanceof MessageComponent m && m.id == id) {
                return; // Sõnum on juba olemas, katkestame töö
            }
        }

        // Leia õige positsioon
        int position = children.size(); // Vaikimisi lisame lõppu
        
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) instanceof MessageComponent m) {
                if (time < m.time) {
                    position = i;
                    break;
                }
            }
        }

        MessageComponent message = new MessageComponent(author, content, time, id);
        children.add(position, message);
    }
}
