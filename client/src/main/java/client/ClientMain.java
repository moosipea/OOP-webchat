package client;

import javafx.application.Application;
import javafx.stage.Stage;

public class ClientMain extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        MessageScene scene = new MessageScene(640, 480);
        String styleSheet = String.valueOf(getClass().getResource("/style.css"));
        scene.getStylesheets().add(styleSheet);

        stage.setScene(scene);
        stage.setTitle("Hello, world");
        stage.show();
    }
}
