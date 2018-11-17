package no.ntnu.imt3281.ludo.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import no.ntnu.imt3281.ludo.client.Actions;
import no.ntnu.imt3281.ludo.client.StateManager;

public class OverviewController implements IController {
    
    private Actions mActions;
    private StateManager mCache;

    /**
     * IController
     */
    public void bind(Actions a, StateManager c) {

        mActions = a;
        mCache = c;
    }

    @FXML
    public Button mBtnUser;
    @FXML
    public Button mBtnLive;
    @FXML
    public Button mBtnLogout;
    @FXML
    public Button mSendGameInvite;
    @FXML
    public Button mAcceptGameInvite;
    @FXML
    public Button mBtnSendChatInvite;
    @FXML
    public Button mAcceptChatInvite;
    @FXML
    public VBox mBoxGames2;
    @FXML
    public Button mAcceptFriendRequest;
    @FXML
    public Button mUnfriend;
    @FXML
    public Button mSendFriendRequest;
    @FXML
    public VBox mBoxGames;
    @FXML
    public VBox mBoxChats;
    @FXML
    public VBox mBoxFriends;
    @FXML
    public VBox mBoxUsers;
    
    @FXML
    void onClickLive(ActionEvent event) {
        mActions.gotoLive();
    }

    @FXML
    void onClickLogout(ActionEvent event) {
        mActions.logout( );
    }

    @FXML
    void onClickUser(ActionEvent event) {
        mActions.gotoUser();
    }

    @FXML
    void onAcceptChatInvite(ActionEvent event) {

    }

    @FXML
    void onAcceptFriendRequest(ActionEvent event) {

    }

    @FXML
    void onAcceptGameInvite(ActionEvent event) {

    }

    @FXML
    void onSendChatInvite(ActionEvent event) {

    }

    @FXML
    void onSendFriendRequest(ActionEvent event) {

    }

    @FXML
    void onSendGameInvite(ActionEvent event) {

    }

    @FXML
    void onUnfriend(ActionEvent event) {

    }

    @FXML
    void initialize() {
        assert mSendGameInvite != null : "fx:id=\"mSendGameInvite\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mAcceptGameInvite != null : "fx:id=\"mAcceptGameInvite\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mBoxGames != null : "fx:id=\"mBoxGames\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mBtnSendChatInvite != null : "fx:id=\"mBtnSendChatInvite\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mAcceptChatInvite != null : "fx:id=\"mAcceptChatInvite\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mBoxGames2 != null : "fx:id=\"mBoxGames2\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mAcceptFriendRequest != null : "fx:id=\"mAcceptFriendRequest\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mUnfriend != null : "fx:id=\"mUnfriend\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mBoxFriends != null : "fx:id=\"mBoxFriends\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mSendFriendRequest != null : "fx:id=\"mSendFriendRequest\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mBoxUsers != null : "fx:id=\"mBoxUsers\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mBtnUser != null : "fx:id=\"mBtnUser\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mBtnLive != null : "fx:id=\"mBtnLive\" was not injected: check your FXML file 'Overview.fxml'.";
        assert mBtnLogout != null : "fx:id=\"mBtnLogout\" was not injected: check your FXML file 'Overview.fxml'.";

    }
}
