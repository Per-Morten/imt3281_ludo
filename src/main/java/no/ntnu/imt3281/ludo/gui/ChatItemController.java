package no.ntnu.imt3281.ludo.gui;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class ChatItemController extends BaseController {

    @FXML
    Text mMessage;

    @FXML
    ImageView mAvatar;


    @FXML
    void initialize() {
        assert mMessage != null : "fx:id=\"mMessage\" was not injected: check your FXML file 'ChatItem.fxml'.";
    }
}
