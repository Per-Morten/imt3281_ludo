package no.ntnu.imt3281.ludo.gui;

import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;

import no.ntnu.imt3281.ludo.client.*;

public class LudoController implements IController {

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
    void onClickChats(ActionEvent event) {

    }

    @FXML
    void onClickFriends(ActionEvent event) {

    }

    @FXML
    void onClickGames(ActionEvent event) {

    }

    @FXML
    void onClickLogout(ActionEvent event) {

    }

    @FXML
    void onClickUser(ActionEvent event) {

    }

    @FXML
    void onNewChat(ActionEvent event) {

    }

    @FXML
    void onNewGame(ActionEvent event) {

    }

    @FXML
    void initialize() {
        assert mBtnUser != null : "fx:id=\"mBtnUser\" was not injected: check your FXML file 'Ludo.fxml'.";
        assert mBtnGames != null : "fx:id=\"mBtnGames\" was not injected: check your FXML file 'Ludo.fxml'.";
        assert mBtnFriends != null : "fx:id=\"mBtnFriends\" was not injected: check your FXML file 'Ludo.fxml'.";
        assert mBtnChats != null : "fx:id=\"mBtnChats\" was not injected: check your FXML file 'Ludo.fxml'.";
        assert mBtnLogout != null : "fx:id=\"mBtnLogout\" was not injected: check your FXML file 'Ludo.fxml'.";
        assert mBtnNewGame != null : "fx:id=\"mBtnNewGame\" was not injected: check your FXML file 'Ludo.fxml'.";
        assert mTabGame != null : "fx:id=\"mTabGame\" was not injected: check your FXML file 'Ludo.fxml'.";
        assert mBtnNewChat != null : "fx:id=\"mBtnNewChat\" was not injected: check your FXML file 'Ludo.fxml'.";
        assert mTabChat != null : "fx:id=\"mTabChat\" was not injected: check your FXML file 'Ludo.fxml'.";
    }
}
