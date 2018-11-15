package no.ntnu.imt3281.ludo.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import no.ntnu.imt3281.ludo.client.Actions;
import no.ntnu.imt3281.ludo.client.CacheManager;

public class SceneSearchController implements IController {
    
    private Actions mActions;
    private CacheManager mCache;

    /**
     * IController
     */
    public void bind(Actions a, CacheManager c) {

        mActions = a;
        mCache = c;
    }

    @FXML
    private Button mBtnUser;

    @FXML
    private Button mBtnLive;

    @FXML
    private Button mBtnLogout;

    @FXML
    void onClickLive(ActionEvent event) {
        mActions.gotoLive();
    }

    @FXML
    void onClickLogout(ActionEvent event) {
        mActions.logout();
    }

    @FXML
    void onClickUser(ActionEvent event) {
        mActions.gotoUser();
    }

    @FXML
    void initialize() {
        assert mBtnUser != null : "fx:id=\"mBtnUser\" was not injected: check your FXML file 'Search.fxml'.";
        assert mBtnLive != null : "fx:id=\"mBtnLive\" was not injected: check your FXML file 'Search.fxml'.";
        assert mBtnLogout != null : "fx:id=\"mBtnLogout\" was not injected: check your FXML file 'Search.fxml'.";

    }
}
