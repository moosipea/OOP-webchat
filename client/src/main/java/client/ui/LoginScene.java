package client.ui;

import client.ClientConnection;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.UnknownHostException;

/**
 * Stseen sserveriga ühendamiseks. Kasutaja saab sisaldaad serveri andmed
 * (IP, port) ja oma kasutajanime ning salasõna (TBD) ning seejärel ühendada.
 */
public class LoginScene extends Scene {
    private static final Logger log = LogManager.getLogger(LoginScene.class);

    public LoginScene(String stylesheet, Stage stage, double w, double h) {
        // Jõle kahtlane, aga see töötab
        super(new VBox(), w, h);
        getStylesheets().add(stylesheet);

        TextField ipField = new TextField();
        TextField portField = new TextField();
        Label errorField = new Label();

        Button connectButton = new Button("Connect");

        connectButton.setOnAction(e -> {
            try {
                // Loome ühenduse
                ClientConnection conn = new ClientConnection(ipField.getText(), portField.getText());

                // TODO: kui nüüd teise lõime sees error, siis tuleks midagi siin teha ka
                Thread.ofVirtual().start(conn);

                TextField usernameField = new TextField();
                PasswordField passwordField = new PasswordField();
                Button loginButton = new Button("Login");
                Button registerButton = new Button("Register");
                errorField.setText(""); // on siin, kuna ei saa uuesti errorField-ile midagi määrata

                loginButton.setOnAction(e2 -> {
                    String enteredUsername = usernameField.getText();
                    String enteredPassword = passwordField.getText();
                    conn.loginWithCredentials(enteredUsername, enteredPassword);
                });

                registerButton.setOnAction(e2 -> {
                    String enteredUsername = usernameField.getText();
                    String enteredPassword = passwordField.getText();
                    conn.setOnRegisterResponse(response -> {
                        if (response.isSuccess()) {
                            conn.loginWithCredentials(enteredUsername, enteredPassword);
                        } else {
                            log.error("Registering failed!");
                            Platform.runLater(() -> errorField.setText("Registering failed!"));
                        }
                    });
                    conn.registerWithCredentials(enteredUsername, enteredPassword);
                });

                conn.setOnLoginResponse(response -> {
                    if (response.isSuccess()) {
                        Platform.runLater(() -> stage.setScene(new MessageScene(stylesheet, conn, w, h)));
                    } else {
                        log.error("Login failed!");
                        Platform.runLater(() -> errorField.setText("Login failed!"));
                    }
                });

                setRoot(new VBox(
                        new HBox(new Label("username: "), usernameField),
                        new HBox(new Label("password: "), passwordField),
                        new HBox(loginButton, registerButton),
                        errorField
                ));

            } catch (UnknownHostException ex) {
                errorField.setText("Can't connect to server.");
            }
        });

        setRoot(new VBox(
                new HBox(new Label("IP: "), ipField),
                new HBox(new Label("port: "), portField),
                connectButton,
                errorField
        ));
    }
}
