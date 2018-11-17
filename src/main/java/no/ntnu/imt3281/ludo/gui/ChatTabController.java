package no.ntnu.imt3281.ludo.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ChatTabController extends BaseController {

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
        assert mListMessages != null : "fx:id=\"mListMessages\" was not injected: check your FXML file 'ChatTab.fxml'.";
        assert mTextSend != null : "fx:id=\"mTextSend\" was not injected: check your FXML file 'ChatTab.fxml'.";
        assert mBtnSend != null : "fx:id=\"mBtnSend\" was not injected: check your FXML file 'ChatTab.fxml'.";

    }
}
