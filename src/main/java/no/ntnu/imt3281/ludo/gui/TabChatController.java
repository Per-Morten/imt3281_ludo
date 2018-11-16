package no.ntnu.imt3281.ludo.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import no.ntnu.imt3281.ludo.client.Actions;
import no.ntnu.imt3281.ludo.client.StateManager;

public class TabChatController implements IController {
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
    private VBox mListMessages;

    @FXML
    private TextField mTextSend;

    @FXML
    private Button mBtnSend;

    @FXML
    void onSend(ActionEvent event) {

    }

    @FXML
    void initialize() {
        assert mListMessages != null : "fx:id=\"mListMessages\" was not injected: check your FXML file 'TabChat.fxml'.";
        assert mTextSend != null : "fx:id=\"mTextSend\" was not injected: check your FXML file 'TabChat.fxml'.";
        assert mBtnSend != null : "fx:id=\"mBtnSend\" was not injected: check your FXML file 'TabChat.fxml'.";

    }
}
