package no.ntnu.imt3281.ludo.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;

import no.ntnu.imt3281.ludo.client.*;

public class LiveController implements IController {

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
    public Button mBtnUser;

    @FXML
    public Button mBtnSearch;

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
    void onClickSearch(ActionEvent event) {
        mActions.gotoSearch();
    }

    @FXML
    void onClickUser(ActionEvent event) {
        mActions.gotoUser();
    }

    @FXML
    void onNewChat(ActionEvent event) {
        mActions.createChat();
    }

    @FXML
    void onNewGame(ActionEvent event) {
        mActions.createGame();
    }

    @FXML
    void initialize() {
        assert mBtnUser != null : "fx:id=\"mBtnUser\" was not injected: check your FXML file 'Live.fxml'.";
        assert mBtnSearch != null : "fx:id=\"mBtnSearch\" was not injected: check your FXML file 'Live.fxml'.";
        assert mBtnLogout != null : "fx:id=\"mBtnLogout\" was not injected: check your FXML file 'Live.fxml'.";
        assert mBtnNewGame != null : "fx:id=\"mBtnNewGame\" was not injected: check your FXML file 'Live.fxml'.";
        assert mTabGames != null : "fx:id=\"mTabGame\" was not injected: check your FXML file 'Live.fxml'.";
        assert mBtnNewChat != null : "fx:id=\"mBtnNewChat\" was not injected: check your FXML file 'Live.fxml'.";
        assert mTabChats != null : "fx:id=\"mTabChat\" was not injected: check your FXML file 'Live.fxml'.";
    }
}
