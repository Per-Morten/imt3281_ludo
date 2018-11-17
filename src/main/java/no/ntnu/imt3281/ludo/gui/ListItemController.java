package no.ntnu.imt3281.ludo.gui;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import no.ntnu.imt3281.ludo.client.Actions;
import no.ntnu.imt3281.ludo.client.StateManager;

public class ListItemController implements IController {
    private Actions mActions;
    private StateManager mCache;
    /**
     * IController
     */
    public void bind(Actions a, StateManager c) {

        mActions = a;
        mCache = c;
    }

    public Integer index;
    public ListItemType type;

    @FXML
    public Text mText;

    @FXML
    void onClick(MouseEvent event) {

    }

    @FXML
    void initialize() {
        assert mText != null : "fx:id=\"mText\" was not injected: check your FXML file 'ListItem.fxml'.";
    }
}
