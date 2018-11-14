package no.ntnu.imt3281.ludo.gui;

import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;

import no.ntnu.imt3281.ludo.client.*;

public class LudoController implements IController {

    Actions mActions;

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

    /*
     * @FXML void onRandomGame(ActionEvent e) { FXMLLoader loader = new
     * FXMLLoader(getClass().getResource("GameBoard.fxml"));
     * loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));
     * 
     * GameBoardController controller = loader.getController(); // Use controller to
     * set up communication for this game. // Note, a new game tab would be created
     * due to some communication from the server // This is here purely to
     * illustrate how a layout is loaded and added to a tab pane.
     * 
     * try { AnchorPane gameBoard = loader.load(); Tab tab = new Tab("Game");
     * tab.setContent(gameBoard); mTabbedPane.getTabs().add(tab); } catch
     * (IOException e1) { // TODO Auto-generated catch block e1.printStackTrace(); }
     * }
     */

}
