package no.ntnu.imt3281.ludo.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import no.ntnu.imt3281.ludo.client.ActionConsumer;

public class LoginController {

    private ActionConsumer mActionConsumer;

    @FXML
    private TextField inputUsername;

    @FXML
    private TextField inputPassword;

    @FXML
    private Button buttonLogin;

    @FXML
    void onClickLogin(ActionEvent event) {

        var username = inputUsername.getText();
        var password = inputPassword.getText();
        mActionConsumer.dispatch(() -> mActionConsumer.login(username, password));
    }

    @FXML
    void initialize() {
        assert inputUsername != null : "fx:id=\"inputUsername\" was not injected: check your FXML file 'Login.fxml'.";
        assert inputPassword != null : "fx:id=\"inputPassword\" was not injected: check your FXML file 'Login.fxml'.";
        assert buttonLogin != null : "fx:id=\"buttonLogin\" was not injected: check your FXML file 'Login.fxml'.";

    }

    public void bind(ActionConsumer actionConsumer) {
        mActionConsumer = actionConsumer;
    }
}
