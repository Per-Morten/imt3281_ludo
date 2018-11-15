package no.ntnu.imt3281.ludo.gui;

import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;

import no.ntnu.imt3281.ludo.client.*;

public class SceneLiveController implements IController {

    private Actions mActions;
    private CacheManager mCache;

    /**
     * IController
     */
    public void bind(Actions a, CacheManager c) {

        mActions = a;
        mCache = c;
    }

    @FXML
    public Button mBtnUser;

    @FXML
    public Button mBtnGames;

    @FXML
    public Button mBtnFriends;

    @FXML
    public Button mBtnChats;

    @FXML
    public Button mBtnLogout;

    @FXML
    public Button mBtnNewGame;

    @FXML
    public TabPane mTabGame;

    @FXML
    public Button mBtnNewChat;

    @FXML
    public TabPane mTabChat;


    @FXML
    void onClickLogout(ActionEvent event) {
        mActions.logout();
    }

    @FXML
    void onClickSearch(ActionEvent event) {
        mActions.gotoSearch();
    }

    @FXML
    void onClickUser(ActionEvent event) {
        mActions.gotoUser();
    }

    @FXML
    void onNewChat(ActionEvent event) {
        mActions.newChat();
    }

    @FXML
    void onNewGame(ActionEvent event) {
        mActions.newGame();
    }

    @FXML
    void initialize() {
        assert mBtnUser != null : "fx:id=\"mBtnUser\" was not injected: check your FXML file 'SceneLive.fxml'.";
        assert mBtnGames != null : "fx:id=\"mBtnGames\" was not injected: check your FXML file 'SceneLive.fxml'.";
        assert mBtnFriends != null : "fx:id=\"mBtnFriends\" was not injected: check your FXML file 'SceneLive.fxml'.";
        assert mBtnChats != null : "fx:id=\"mBtnChats\" was not injected: check your FXML file 'SceneLive.fxml'.";
        assert mBtnLogout != null : "fx:id=\"mBtnLogout\" was not injected: check your FXML file 'SceneLive.fxml'.";
        assert mBtnNewGame != null : "fx:id=\"mBtnNewGame\" was not injected: check your FXML file 'SceneLive.fxml'.";
        assert mTabGame != null : "fx:id=\"mTabGame\" was not injected: check your FXML file 'SceneLive.fxml'.";
        assert mBtnNewChat != null : "fx:id=\"mBtnNewChat\" was not injected: check your FXML file 'SceneLive.fxml'.";
        assert mTabChat != null : "fx:id=\"mTabChat\" was not injected: check your FXML file 'SceneLive.fxml'.";
    }
}
