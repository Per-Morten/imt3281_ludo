package no.ntnu.imt3281.ludo.gui;

import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;

import no.ntnu.imt3281.ludo.client.*;

public class LudoController implements IController {

    private Actions mActions;
    private StateManager mState;

    /**
     * IController
     */
    public void bind(Actions a, StateManager s) {

        mActions = a;
        mState = s;
    }

    @FXML
    ResourceBundle resources;

    @FXML
    TabPane mTabbedPane;

    @FXML
    Button mBtnLogout;

    @FXML
    void onChallangePlayers(ActionEvent event) {

    }

    @FXML
    void onChatInviteList(ActionEvent event) {

    }

    @FXML
    void onRoomList(ActionEvent event) {

    }

    @FXML
    void onUserFriendList(ActionEvent event) {

    }

    @FXML
    void onUserProfile(ActionEvent event) {

    }

    public void bind(Actions actions) {
        mActions = actions;
    }

    @FXML
    void initialize() {
        assert mTabbedPane != null : "fx:id=\"mTabbedPane\" was not injected: check your FXML file 'Ludo.fxml'.";
        assert mBtnLogout != null : "fx:id=\"mBtnLogout\" was not injected: check your FXML file 'Ludo.fxml'.";
    }

    @FXML
    void onLogout(ActionEvent e) {
        mActions.logout();
    }

}
