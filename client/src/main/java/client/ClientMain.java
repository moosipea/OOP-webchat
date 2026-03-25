package client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.Objects;

public class ClientMain extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        MessageList messages = new MessageList();

        messages.addMessage("moosipea", "omg *sõnumite* **renderimine**");
        messages.addMessage("moosipea", "teisi kasutajaid saab pingida: @kasutaja");
        messages.addMessage("kasutaja", "ja kasutaja värv määratakse nime alusel");

        Scene scene = new Scene(messages);
        String styleSheet = String.valueOf(getClass().getResource("/style.css"));
        scene.getStylesheets().add(styleSheet);

        stage.setScene(scene);
        stage.setTitle("Hello, world");
        stage.show();
    }
}
