package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ClientMain extends Application {
    private final MessageList messages = new MessageList();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        messages.addMessage("moosipea", "omg *sõnumite* **renderimine**");
        messages.addMessage("moosipea", "teisi kasutajaid saab pingida: @kasutaja");
        messages.addMessage("kasutaja", "ja kasutaja värv määratakse nime alusel");

        ScrollPane scrollPane = new ScrollPane(messages);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Scrollib alla kui uus sõnum tuleb
        messages.heightProperty().addListener((obs, oldValue, newValue) -> Platform.runLater(() -> scrollPane.setVvalue(1.0)));

        TextField messageField = new TextField();
        messageField.setOnAction(e -> {
            sendMessage(messageField.getText());
            messageField.clear();
            Platform.runLater(() -> scrollPane.setVvalue(1.0));
        });

        VBox root = new VBox();
        root.getChildren().addAll(scrollPane, messageField);

        Scene scene = new Scene(root, 640, 480);
        String styleSheet = String.valueOf(getClass().getResource("/style.css"));
        scene.getStylesheets().add(styleSheet);

        stage.setScene(scene);
        stage.setTitle("Hello, world");
        stage.show();
    }

    // Dummy meetod sõnumi saatmiseks kliendilt serverile
    private void sendMessage(String content) {
        // Hardcoded username praeguseks
        // See on ainult visuaali jaoks, mõistagi ei lase me lihtsalt kasutajal
        // kellegi teise nime alt sõnumeid saata
        String username = "kasutaja";
        messages.addMessage(username, content);
    }
}
