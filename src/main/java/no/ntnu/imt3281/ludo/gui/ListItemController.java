package no.ntnu.imt3281.ludo.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class ListItemController extends BaseController {

    int mId;
    ListItemType mType;
    OverviewController mOverview;

    @FXML
    CheckBox mCheckbox;

    @FXML
    Text mText;

    void init(ListItemType type, int id, OverviewController overview, String text) {
        mId = id;
        mType = type;
        mOverview = overview;
        mText.setText(text);
    }

    @FXML
    void onClick(MouseEvent event) {

    }

    @FXML
    void onCheck(ActionEvent event) {
        mOverview.onItemSelectChange(mType, mId, mCheckbox.isSelected());
    }

    @FXML
    void initialize() {
        assert mText != null : "fx:id=\"mText\" was not injected: check your FXML file 'ListItem.fxml'.";
    }
}
