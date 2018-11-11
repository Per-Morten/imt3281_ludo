package no.ntnu.imt3281.ludo.gui;

/**
 * Sample Skeleton for 'GameBoard.fxml' IGUIController Class
 */

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import no.ntnu.imt3281.ludo.client.ActionConsumer;

public class GameBoardController implements IGUIController {

    @FXML
    Label player1Name;

    @FXML
    ImageView player1Active;

    @FXML
    Label player2Name;

    @FXML
    ImageView player2Active;

    @FXML
    Label player3Name;
    
    @FXML
    ImageView player3Active;

    @FXML
    Label player4Name;

    @FXML
    ImageView player4Active;
    
    @FXML
    ImageView diceThrown;
    
    @FXML
    Button throwTheDice;

    @FXML
    TextArea chatArea;

    @FXML
    TextField textToSay;

    @FXML
    Button sendTextButton;

    private ActionConsumer mActionConsumer;

    public void bind(ActionConsumer a) {
        mActionConsumer = a;
    }
}