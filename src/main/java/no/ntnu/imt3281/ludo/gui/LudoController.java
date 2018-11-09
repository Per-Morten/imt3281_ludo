package no.ntnu.imt3281.ludo.gui;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

import no.ntnu.imt3281.ludo.client.*;

public class LudoController {

    private ActionConsumer mActionConsumer;

    public void bind(ActionConsumer actionConsumer) {
        mActionConsumer = actionConsumer;
    }

    @FXML
    private ResourceBundle resources;

    @FXML
    private MenuItem random;

    @FXML
    private TabPane tabbedPane;

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

    @FXML
    void initialize() {
        assert random != null : "fx:id=\"random\" was not injected: check your FXML file 'Ludo.fxml'.";
        assert tabbedPane != null : "fx:id=\"tabbedPane\" was not injected: check your FXML file 'Ludo.fxml'.";
    }

    @FXML
    void onRandomGame(ActionEvent e) {
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("GameBoard.fxml"));
    	loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));

		GameBoardController controller = loader.getController();
		// Use controller to set up communication for this game.
		// Note, a new game tab would be created due to some communication from the server
		// This is here purely to illustrate how a layout is loaded and added to a tab pane.
		
    	try {
    		AnchorPane gameBoard = loader.load();
        	Tab tab = new Tab("Game");
    		tab.setContent(gameBoard);
        	tabbedPane.getTabs().add(tab);
    	} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }


}
