package no.ntnu.imt3281.ludo.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.shape.Rectangle;

import no.ntnu.imt3281.ludo.client.ActionConsumer;

public class LoginController {

    ActionConsumer mActionConsumer;

    @FXML
    TextField mInputUsername;

    @FXML
    TextField mInputPassword;

    @FXML
    Button mButtonLogin;

    @FXML
    Button mButtonRegister;

    @FXML
    Rectangle mRectangle;

    @FXML
    void onClickLogin(ActionEvent event) {
        var username = mInputUsername.getText();
        var password = mInputPassword.getText();
        mActionConsumer.feed(action -> action.login(username, password));
    }

    @FXML
    void onClickRegister(ActionEvent event) {
        var username = mInputUsername.getText();
        var password = mInputPassword.getText();
        mActionConsumer.feed(action -> action.createUser(username, password));
    }

    @FXML
    void initialize() {
        assert mInputUsername != null : "fx:id=\"inputUsername\" was not injected: check your FXML file 'Login.fxml'.";
        assert mInputPassword != null : "fx:id=\"inputPassword\" was not injected: check your FXML file 'Login.fxml'.";
        assert mButtonLogin != null : "fx:id=\"buttonLogin\" was not injected: check your FXML file 'Login.fxml'.";
        assert mRectangle != null : "fx:id=\"mTriangle\" was not injected: check your FXML file 'Login.fxml'.";
    }

    public void bind(ActionConsumer actionConsumer) {
        mActionConsumer = actionConsumer;
    }
}
