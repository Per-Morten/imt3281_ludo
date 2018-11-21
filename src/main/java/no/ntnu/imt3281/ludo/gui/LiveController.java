package no.ntnu.imt3281.ludo.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;


public class LiveController extends BaseController {

    @FXML
    public Button mBtnUser;

    @FXML
    public Button mBtnOverview;

    @FXML
    public Button mBtnLogout;

    @FXML
    public Button mBtnNewGame;

    @FXML
    public Button mBtnNewChat;

    @FXML
    public TabPane mTabGames;

    @FXML
    public TabPane mTabChats;


    @FXML
    void onClickLogout(ActionEvent event) {
        mActions.logout();
    }

    @FXML
    void onClickOverview(ActionEvent event) {
        mActions.gotoOverview();
    }

    @FXML
    void onClickUser(ActionEvent event) {
        mActions.gotoUser();
    }

    @FXML
    void onNewGame(ActionEvent event) {
        mActions.createGame();
    }

    @FXML
    void onNewChat(ActionEvent event) {
        mActions.createChat();
    }

    @FXML
    void initialize() {
        assert mBtnUser != null : "fx:id=\"mBtnUser\" was not injected: check your FXML file 'Live.fxml'.";
        assert mBtnOverview != null : "fx:id=\"mBtnSearch\" was not injected: check your FXML file 'Live.fxml'.";
        assert mBtnLogout != null : "fx:id=\"mBtnLogout\" was not injected: check your FXML file 'Live.fxml'.";
        assert mBtnNewGame != null : "fx:id=\"mBtnNewGame\" was not injected: check your FXML file 'Live.fxml'.";
        assert mTabGames != null : "fx:id=\"mTabGame\" was not injected: check your FXML file 'Live.fxml'.";
        assert mBtnNewChat != null : "fx:id=\"mBtnNewChat\" was not injected: check your FXML file 'Live.fxml'.";
        assert mTabChats != null : "fx:id=\"mTabChat\" was not injected: check your FXML file 'Live.fxml'.";
    }
}
