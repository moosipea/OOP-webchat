package client.ui;

import client.ClientConnection;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.function.Consumer;

/**
 * Stseen sserveriga ühendamiseks. Kasutaja saab sisaldaad serveri andmed
 * (IP, port) ja oma kasutajanime ning salasõna (TBD) ning seejärel ühendada.
 */
public class LoginScene extends Scene {
    // TODO: switchScene Consumer'ina tundub nagu halb
    public LoginScene(Consumer<Scene> switchScene, double w, double h) {
        // Jõle kahtlane, aga see töötab
        super(new VBox(), w, h);

        TextField ipField = new TextField();
        TextField portField = new TextField();

        Button connectButton = new Button("Connect");

        connectButton.setOnAction(e -> {
            try {
                // Loome ühenduse
                ClientConnection conn = new ClientConnection(ipField.getText(), portField.getText());

                // TODO: login/register popup
                // TODO: kui register õnnestub, siis uuesti login popup
            } catch (UnknownHostException ex) {
                // TODO: error popup
                throw new RuntimeException(ex);
            }
        });

        setRoot(new VBox(
                new HBox(new Label("IP: "), ipField),
                new HBox(new Label("port: "), portField),
                connectButton
        ));
    }
}
