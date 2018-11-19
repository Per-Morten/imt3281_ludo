package no.ntnu.imt3281.ludo.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

import no.ntnu.imt3281.ludo.client.State;

public class LoginController extends BaseController {

    @FXML
    private TextField mLoginEmail;

    @FXML
    private PasswordField mLoginPassword;

    @FXML
    private Button mButtonLogin;

    @FXML
    private TextField mCreateEmail;

    @FXML
    private PasswordField mCreatePassword;

    @FXML
    private TextField mCreateUsername;

    @FXML
    private Button mButtonCreate;

    @FXML
    void onClickLogin(ActionEvent event) {
        var email = mLoginEmail.getText();
        var password = mLoginPassword.getText();
        mActions.login(email, password);
    }

    @FXML
    void onClickCreate(ActionEvent event) {
        var email = mCreateEmail.getText();
        var password = mCreatePassword.getText();
        var username = mCreateUsername.getText();
        mActions.createUser(email, password, username);
    }

    @FXML
    void initialize() {
        assert mLoginEmail != null : "fx:id=\"mLoginEmail\" was not injected: check your FXML file 'Login.fxml'.";
        assert mLoginPassword != null : "fx:id=\"mLoginPassword\" was not injected: check your FXML file 'Login.fxml'.";
        assert mButtonLogin != null : "fx:id=\"mButtonLogin\" was not injected: check your FXML file 'Login.fxml'.";
        assert mCreateEmail != null : "fx:id=\"mCreateEmail\" was not injected: check your FXML file 'Login.fxml'.";
        assert mCreatePassword != null : "fx:id=\"mCreatePassword\" was not injected: check your FXML file 'Login.fxml'.";
        assert mCreateUsername != null : "fx:id=\"mCreateUsername\" was not injected: check your FXML file 'Login.fxml'.";
        assert mButtonCreate != null : "fx:id=\"mButtonCreate\" was not injected: check your FXML file 'Login.fxml'.";

        Platform.runLater(() -> {
            State localState = mStateManager.copy();
            mLoginEmail.setText(localState.email);
        });
    }
}
