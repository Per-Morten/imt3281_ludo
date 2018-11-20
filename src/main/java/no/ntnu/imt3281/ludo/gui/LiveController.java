package no.ntnu.imt3281.ludo.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


public class LiveController extends BaseController {

    @FXML
    public Button mBtnUser;

    @FXML
    public Button mBtnOverview;

    @FXML
    public Button mBtnLogout;

    @FXML
    public TabPane mTabGames;

    @FXML
    public TabPane mTabChats;

    @FXML
    private TextField mChatName;

    @FXML
    private TextField mGameName;

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
    void onNewGame(KeyEvent event) {
        if(!event.getCode().equals(KeyCode.ENTER)) return;
        if(mGameName.getText().isEmpty()) return;

        mActions.createGame(mGameName.getText());
        Platform.runLater(() -> mGameName.setText("") );
    }

    @FXML
    void onNewChat(KeyEvent event) {
        if(!event.getCode().equals(KeyCode.ENTER)) return;
        if(mChatName.getText().isEmpty()) return;

        mActions.createChat(mChatName.getText());
        Platform.runLater(() ->mChatName.setText("") );
    }


    @FXML
    void initialize() {
        assert mBtnUser != null : "fx:id=\"mBtnUser\" was not injected: check your FXML file 'Live.fxml'.";
        assert mBtnOverview != null : "fx:id=\"mBtnSearch\" was not injected: check your FXML file 'Live.fxml'.";
        assert mBtnLogout != null : "fx:id=\"mBtnLogout\" was not injected: check your FXML file 'Live.fxml'.";
        assert mGameName != null : "fx:id=\"mGameName\" was not injected: check your FXML file 'Live.fxml'.";
        assert mTabGames != null : "fx:id=\"mTabGame\" was not injected: check your FXML file 'Live.fxml'.";
        assert mChatName != null : "fx:id=\"mChatName\" was not injected: check your FXML file 'Live.fxml'.";
        assert mTabChats != null : "fx:id=\"mTabChat\" was not injected: check your FXML file 'Live.fxml'.";
    }
}
