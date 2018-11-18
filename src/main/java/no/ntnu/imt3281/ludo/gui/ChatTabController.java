package no.ntnu.imt3281.ludo.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ChatTabController extends BaseController {

    int mId;

    @FXML
    public VBox mMessageList;

    @FXML
    private TextField mTextSend;

    @FXML
    private Button mBtnSend;

    @FXML
    void onSend(ActionEvent event) {

        if (mTextSend.getText().equals("")) return;
        mActions.sendChatMessage(mId, mTextSend.getText());
    }

    @FXML
    void initialize() {
        assert mMessageList != null : "fx:id=\"mMessageList\" was not injected: check your FXML file 'ChatTab.fxml'.";
        assert mTextSend != null : "fx:id=\"mTextSend\" was not injected: check your FXML file 'ChatTab.fxml'.";
        assert mBtnSend != null : "fx:id=\"mBtnSend\" was not injected: check your FXML file 'ChatTab.fxml'.";
    }
}
