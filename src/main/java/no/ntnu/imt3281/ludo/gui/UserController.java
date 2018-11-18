package no.ntnu.imt3281.ludo.gui;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class UserController extends BaseController {

    @FXML
    ImageView mAvatar;

    @FXML
    TextField mUsername;

    @FXML
    TextField mAvatarURL;

    @FXML
    TextField mEmail;

    @FXML
    PasswordField mPassword;

    @FXML
    private Button mBtnPlay;

    @FXML
    private Button mBtnUpdate;

    @FXML
    private Button mBtnLogout;

    @FXML
    private Button mBtnDelete;

    @FXML
    void onUpdate(ActionEvent event) {
        mActions.updateUser(mUsername.getText(), mEmail.getText(), mPassword.getText(), mAvatarURL.getText());
    }

    @FXML
    void onDelete(ActionEvent event) {
        mActions.deleteUser();
    }

    @FXML
    void onLogout(ActionEvent event) {
        mActions.logout();
    }

    @FXML
    void onPlay(ActionEvent event) {
        mActions.gotoLive();
    }

    @FXML
    void initialize() {
        assert mAvatar != null : "fx:id=\"mAvatar\" was not injected: check your FXML file 'User.fxml'.";
        assert mUsername != null : "fx:id=\"mUsername\" was not injected: check your FXML file 'User.fxml'.";
        assert mAvatarURL != null : "fx:id=\"mAvatarURL\" was not injected: check your FXML file 'User.fxml'.";
        assert mEmail != null : "fx:id=\"mEmail\" was not injected: check your FXML file 'User.fxml'.";
        assert mPassword != null : "fx:id=\"mPassword\" was not injected: check your FXML file 'User.fxml'.";
        assert mBtnPlay != null : "fx:id=\"mBtnPlay\" was not injected: check your FXML file 'User.fxml'.";
        assert mBtnUpdate != null : "fx:id=\"mBtnUpdate\" was not injected: check your FXML file 'User.fxml'.";
        assert mBtnLogout != null : "fx:id=\"mBtnLogout\" was not injected: check your FXML file 'User.fxml'.";

    }
}
