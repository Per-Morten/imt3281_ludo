package no.ntnu.imt3281.ludo.gui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.ResourceBundle;

import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.client.Actions;
import no.ntnu.imt3281.ludo.client.CacheManager;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;

/**
 * Controls FXML Controllers and how they transition
 */
public class Transitions {

    private Stage mStage;
    private Actions mActions;
    private CacheManager mCacheManager;

    public class FXMLDocument {
        public AnchorPane root;
        public IController controller;
    }

    public void bind(Stage stage, Actions actions, CacheManager sm) {
        mStage = stage;
        mActions = actions;
        mCacheManager = sm;
    }

    public void renderLogin() {

        var login = this.loadFXML("Login.fxml");
        var root = new Scene(login.root);
        Platform.runLater(() -> {
            mStage.setScene(root);
            mStage.show();
        });
    }

    public void renderLudo() {
        var ludo = this.loadFXML("Ludo.fxml");
        var ludoController = (LudoController)ludo.controller;
        var state = mCacheManager.copy();

        Platform.runLater(()-> {
            state.gameId.forEach(id -> {
                var gameBoard = this.loadFXML("GameBoard.fxml");
                Tab tab = new Tab("Game" + id);
                tab.setContent(gameBoard.root);
                ludoController.mTabGame.getTabs().add(tab);
            });
            mStage.setScene(new Scene(ludo.root));
            mStage.show();
        });
    }


    private void toastPending(String message) {
        int toastMsgTime = 1500;
        int fadeInTime = 100;
        int fadeOutTime = 100;
        Toast.makeText(mStage, message, toastMsgTime, fadeInTime, fadeOutTime,
                Color.color((float) 0x62 / 0xff, (float) 0x00 / 0xff, (float) 0xEE / 0xff));
    }

    private void toastError(String message) {
        int toastMsgTime = 1500;
        int fadeInTime = 100;
        int fadeOutTime = 100;
        Toast.makeText(mStage, message, toastMsgTime, fadeInTime, fadeOutTime,
                Color.color((float) 0xB0 / 0xff, (float) 0x00 / 0xff, (float) 0x20 / 0xff));
    }

    private FXMLDocument loadFXML(String filename) {
        var fxmlDocument = new FXMLDocument();

        var fxmlLoader = new FXMLLoader(getClass().getResource(filename));
        fxmlLoader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));

        try {
            fxmlDocument.root = fxmlLoader.load();
            fxmlDocument.controller = fxmlLoader.getController();
            fxmlDocument.controller.bind(mActions, mCacheManager);
        } catch (IOException e) {
            Logger.log(Level.ERROR, "IOException loading .fxml file:" + e.toString());
        }

        return fxmlDocument;
    }
}
