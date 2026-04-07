package client;

import client.ui.LoginScene;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Klient alustab siit JavaFX programmina.
 */
public class ClientMain extends Application {
    private Stage stage = null;
    private String stylesheet = null;

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Programmi tegelik töö hakkab peale siit.
     */
    @Override
    public void start(Stage stage) {
        // See värk on ilgelt kahtlane
        this.stage = stage;

        // Esialgne stseen on serveriga ühendamiseks mõeldud LoginScene.
        LoginScene scene = new LoginScene(this::switchScene, 640, 480);
        switchScene(scene);

        stage.show();
    }

    /**
     * Laeb stylesheet'i (css fail) ressurssidest.
     *
     * @return stylesheet sõnena.
     */
    private String getStyleSheet() {
        // Loeme ressurssidest ainult siis, kui on esimene kord.
        if (stylesheet == null) {
            stylesheet = String.valueOf(getClass().getResource("/style.css"));
        }
        return stylesheet;
    }

    /**
     * Vahetab ära aktivse stseeni ja lisab sellele stylesheet'i.
     *
     * @param scene uus stseen.
     */
    // TODO: mõelda midagi targemat välja
    private void switchScene(Scene scene) {
        scene.getStylesheets().add(getStyleSheet());
        stage.setScene(scene);
    }
}
