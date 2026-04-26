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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.UnknownHostException;

/**
 * Stseen sserveriga ühendamiseks. Kasutaja saab sisaldaad serveri andmed
 * (IP, port) ja oma kasutajanime ning salasõna (TBD) ning seejärel ühendada.
 */
public class LoginScene extends Scene {
    public LoginScene(String stylesheet, Stage stage, double w, double h) {
        // Jõle kahtlane, aga see töötab
        super(new VBox(), w, h);
        getStylesheets().add(stylesheet);

        TextField ipField = new TextField();
        TextField portField = new TextField();

        Button connectButton = new Button("Connect");

        connectButton.setOnAction(e -> {
            try {
                // Loome ühenduse
                ClientConnection conn = new ClientConnection(ipField.getText(), portField.getText());

                // TODO: kui nüüd teise lõime sees error, siis tuleks midagi siin teha ka
                Thread.ofVirtual().start(conn);

                AuthDialog authDialog = new AuthDialog(conn);
                authDialog.show();

                conn.setOnLoginResponse(response -> {
                    if (response.isSuccess()) {
                        Platform.runLater(() -> {
                            stage.setScene(new MessageScene(stylesheet, conn, w, h));
                            authDialog.close();
                        });
                    } else {
                        // TODO: report login error (popup)
                        // TODO: üldse võiks olla mingi staatiline abimeetod popupide tegemiseks
                    }
                });

                conn.setOnRegisterResponse(response -> {
                    if (response.isSuccess()) {
                        conn.loginWithCredentials(authDialog.getEnteredUsername(), authDialog.getEnteredPassword());
                    } else {
                        // TODO: report login error (popup)
                    }
                });

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

    private static class AuthDialog extends Stage {
        private String enteredUsername;
        private String enteredPassword;

        public AuthDialog(ClientConnection conn) {
            initModality(Modality.WINDOW_MODAL);

            TextField usernameField = new TextField();
            PasswordField passwordField = new PasswordField();
            Button loginButton = new Button("Login");
            Button registerButton = new Button("Register");

            loginButton.setOnAction(e -> {
                enteredUsername = usernameField.getText();
                enteredPassword = passwordField.getText();
                conn.loginWithCredentials(enteredUsername, enteredPassword);
            });

            registerButton.setOnAction(e -> {
                enteredUsername = usernameField.getText();
                enteredPassword = passwordField.getText();
                conn.registerWithCredentials(enteredUsername, enteredPassword);
            });

            Scene dialogScene = new Scene(new VBox(
                    usernameField,
                    passwordField,
                    new HBox(loginButton, registerButton)
            ));

            setScene(dialogScene);
        }

        public String getEnteredUsername() {
            return enteredUsername;
        }

        public String getEnteredPassword() {
            return enteredPassword;
        }
    }
}
