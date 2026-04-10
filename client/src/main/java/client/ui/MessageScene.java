package client.ui;

import client.ClientConnection;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stseen sõnumite vaateks. Saab vahetada kanaleid (TBD), kirjutada sõnumeid
 * ja lugeda sõnumeid.
 */
public class MessageScene extends Scene {
    // Kõik sõnumid UI komponentidena.
    private final Map<String, MessageList> channels;
    private String[] channelNames = {"general", "uudised"};
    private String selectedChannel = channelNames[0];
    private ScrollPane scrollPane;


    public MessageScene(ClientConnection conn, double w, double h) {
        // Midagi peame parentiks panema, paneme HBox
        super(new HBox(), w, h);

        channels = new HashMap<>();

        // Sõnumite vaade
        scrollPane = new ScrollPane();
        for (String channelName : channelNames){
            MessageList messages = new MessageList();
            messages.heightProperty().addListener((obs, oldValue, newValue) -> Platform.runLater(() -> scrollPane.setVvalue(1.0)));
            channels.put(channelName, messages);
        }
        scrollPane.setContent(channels.get(selectedChannel));
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Scrollib alla kui uus sõnum tuleb

        // Sõnumite kirjutamiseks
        TextField messageField = createMessageField(conn, scrollPane);

        // Ekraani parempoolne osa (sõnumid ja kast sõnumi kirjutamiseks)
        VBox messagesRoot = new VBox(scrollPane, messageField);
        HBox.setHgrow(messagesRoot, Priority.ALWAYS);

        // Vasakpoolne osa, kus on kanalid
        // TODO: see on ainult UI testimiseks, tuleks küsida kanalite nimekirja serverilt.
        VBox channelList = createChannelList(channelNames);

        // Lõpuks vahetame rooti välja
        // TODO: kindlasti seda saab kuidagi ilusamalt teha, see on hästi rõve.
        HBox root = new HBox(channelList, messagesRoot);
        setRoot(root);

        // Kui UI on loodud, kleebime sinna otsa ühenduse serveriga
        conn.setOnMessageReceived(this::addMessageToUI);
        Thread.ofVirtual().start(conn);
    }

    /**
     * Initsialiseerib tekstikastikese sõnumite kirjutamiseks.
     *
     * @param conn       viit ühendusele serveriga.
     * @param scrollPane viit ScrollPane'ile, mis sisaldab sõnumeid.
     * @return loodud tekstikastike koos oma event handler'iga.
     */
    private TextField createMessageField(ClientConnection conn, ScrollPane scrollPane) {
        TextField messageField = new TextField();

        // Kui vajutatakse enter, st tahetakse kirjutatut ära saata.
        messageField.setOnAction(e -> {
            // Tühi, edasi pole midagi teha.
            if (messageField.getText().isEmpty()) {
                return;
            }

            // Saadame ära.
            conn.queueMessage(messageField.getText(), selectedChannel);

            // Teeme tekstikastikese tühjaks.
            messageField.clear();

            // Skrollime alla.
            // TODO: miks on see vajalik, kui scrollPane peaks juba ise seda tegema (vt MessageScene konstruktorit).
            Platform.runLater(() -> scrollPane.setVvalue(1.0));
        });

        return messageField;
    }

    /**
     * Initsialiseerib kanalite nimekirja (iga kanal on nupp).
     *
     * @param channels kanalite nimed.
     * @return VBox, mis sisaldab kanaleid nuppudena.
     */
    // TODO: see oli kasulik UI testimiseks, aga kui kanalite nimekiri hakkab
    //  tulema serverilt, peab selle ümber tegema.
    private VBox createChannelList(String[] channels) {
        VBox channelList = new VBox();
        channelList.setFillWidth(true);

        for (String channelName : channels) {
            Button channelButton = new Button(channelName);
            channelButton.setMaxWidth(Double.MAX_VALUE);
            channelButton.setOnAction(e -> {
                selectedChannel = channelName;
                scrollPane.setContent(this.channels.get(selectedChannel));
            });;
            channelList.getChildren().add(channelButton);
        }

        return channelList;
    }

    /**
     * Lisab sõnumi sõnumite nimekirja. Seda meetodit võib välja kutsuda teisest
     * lõimest!
     *
     * @param content sõnumi sisu (praegu sõnena, peaks ümber tegema)
     */
    private void addMessageToUI(String payload) {
        // Platform.runLater() paneb selle lambda kuskile järjekorda ja see
        // täidetakse hiljem. Otse ei tohi me teisest lõimest JavaFX olekut
        // muuta, sest see teeks kõik katki.
        Platform.runLater(() -> {
            String[] pieces = payload.split("\t;");
            if (pieces.length == 3){
                String username = pieces[0];
                String channelName = pieces[1];
                String content = pieces[2].replace("\\t;", "\t;"); // unescapime
                MessageList channel =  channels.get(channelName);
                if (channel != null){
                    channel.addMessage(username, content);
                }
            }
        });
    }
}
