package client;

import client.ui.LoginScene;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Klient alustab siit JavaFX programmina.
 */
public class ClientMain extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Programmi tegelik töö hakkab peale siit.
     */
    @Override
    public void start(Stage stage) {
        String stylesheet = String.valueOf(getClass().getResource("/style.css"));

        // Esialgne stseen on serveriga ühendamiseks mõeldud LoginScene.
        LoginScene scene = new LoginScene(stylesheet, stage, 640, 480);
        stage.setScene(scene);
        stage.show();
    }
}
