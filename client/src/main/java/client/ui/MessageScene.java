package client.ui;

import java.util.HashMap;
import java.util.Map;

import client.ClientConnection;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Stseen sõnumite vaateks. Saab vahetada kanaleid (TBD), kirjutada sõnumeid
 * ja lugeda sõnumeid.
 */
public class MessageScene extends Scene {
    // Kõik sõnumid UI komponentidena.
    private final Map<String, MessageList> channels = new HashMap<>();
    private final ChannelList channelList;
    private ScrollPane scrollPane;
    private ClientConnection conn;

    public MessageScene(String stylesheet, ClientConnection conn, double w, double h) {
        // Midagi peame parentiks panema, paneme HBox
        super(new HBox(), w, h);
        this.conn = conn;
        getStylesheets().add(stylesheet);

        // Ekraani vasakpoolne osa, kus on kanalid
        channelList = new ChannelList();
        channelList.setOnChannelChange(newChannel -> {
            scrollPane.setContent(channels.get(newChannel));
        });

        // Sõnumite vaade
        scrollPane = new ScrollPane(channels.get("#general"));
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        scrollPane.vvalueProperty();

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(2), event -> {
                checkScroll();
            })
        );
        timeline.setCycleCount(Timeline.INDEFINITE); // Jääbki korduma
        timeline.play();

        // Ekraani parempoolne osa (sõnumid ja kast sõnumi kirjutamiseks)
        VBox messagesRoot = createMessagesSide(conn, channelList, scrollPane);

        // Lõpuks vahetame rooti välja
        // TODO: kindlasti seda saab kuidagi ilusamalt teha, see on hästi rõve.
        HBox root = new HBox(channelList, messagesRoot);
        setRoot(root);

        // Kui UI on loodud, kleebime sinna otsa ühenduse serveriga
        conn.setOnMessageReceived((msg) -> Platform.runLater(() -> {
            MessageList channel = channels.get(msg.getTargetChannel());
            if (channel != null) {
                channel.addMessage(msg.getUser(), msg.getContent(), msg.getTimestampMillis(), msg.getId());
            }
        }));

        conn.setOnChannelAdded((channelPacket) -> {
            Platform.runLater(() -> {
                addChannel(channelPacket.getChannelName());
            });
        });

        conn.requestChannelList();
    }

    public void checkScroll(){
        double value = scrollPane.vvalueProperty().doubleValue();
        var child = scrollPane.getContent();
        if (child == null) return;
        if (child instanceof MessageList messages){
            double sisuKorgus = messages.getHeight();
            double aknaKorgus = scrollPane.getViewportBounds().getHeight();

            if (sisuKorgus <= aknaKorgus) { // ei saa veel kerida
                messages.requestHistoryAfter();
                messages.requestHistoryBefore();
            }
            else{
                // on kerinud ülesse
                if (value < 0.2){
                    messages.requestHistoryBefore();
                }
                // on kerinud alla
                if (value > 0.8){
                    messages.requestHistoryAfter();
                }
                // ei ole kerinud enam nii alla
                if (value < 0.7){
                    messages.getChildren().removeLast();
                }
                // ei ole kerinud enam nii ülesse
                if (value > 0.3){
                    messages.getChildren().removeFirst();
                }
            }
        }

    }
    /**
     * Loob ekraani parempoolse osa, mis sisaldab sõnumeid ja kirjutus välja nende
     * kirjutamiseks.
     *
     * @param conn        ühendus serveriga
     * @param channelList kanalite ui komponent
     * @param scrollPane  ScrollPane, mille sees sõnumid istuvad
     * @return ekraani parempoolne osa
     */
    private static VBox createMessagesSide(ClientConnection conn, ChannelList channelList, ScrollPane scrollPane) {
        TextField messageField = new TextField();
        messageField.setOnAction(e -> {
            if (messageField.getText().isEmpty()) {
                return;
            }
            conn.sendMessage(channelList.getActiveChannel(), messageField.getText());
            messageField.clear();
            Platform.runLater(() -> scrollPane.setVvalue(1.0));
        });

        VBox messagesRoot = new VBox(scrollPane, messageField);
        HBox.setHgrow(messagesRoot, Priority.ALWAYS);
        return messagesRoot;
    }

    private void addChannel(String channelName) {
        if (channels.containsKey(channelName)) {
            return;
        }

        MessageList channel = new MessageList();
        channel.setRequestHistory((before, notBefore) -> {
            conn.requestHistory(channelName, before, notBefore);
        });
        // Teeb, et scrollib alla kui uus sõnum tuleb
        //channel.heightProperty().addListener((obs, oldValue, newValue) -> Platform.runLater(() -> scrollPane.setVvalue(1.0)));
        channels.put(channelName, channel);
        channelList.addChannel(channelName);
    }
}
