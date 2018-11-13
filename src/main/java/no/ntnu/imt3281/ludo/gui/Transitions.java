package no.ntnu.imt3281.ludo.gui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.HashMap;

import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.client.Actions;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;


/**
 * Controls FXML Controllers and how they transition
 */
public class Transitions  {

    private HashMap<String, IController> mControllers = new HashMap<>();
    private Stage mStage;
    private Actions mActions;

    public void bind(Stage stage, Actions actions) {
        mStage = stage;
        mActions = actions;
    }

    public void redirect(String filename) {

        var rootPane = this.loadFXML(filename);
        var root = new Scene(rootPane);
        Platform.runLater(() -> {
            mStage.setScene(root);
            mStage.show();
        });
    }

    private void toastPending(String message) {
        int toastMsgTime = 1500;
        int fadeInTime = 100;
        int fadeOutTime= 100;
        Toast.makeText(mStage, message, toastMsgTime, fadeInTime, fadeOutTime, Color.color((float)0x62/0xff, (float)0x00/0xff, (float)0xEE/0xff));
    }

    private void toastError(String message) {
        int toastMsgTime = 1500;
        int fadeInTime = 100;
        int fadeOutTime= 100;
        Toast.makeText(mStage, message, toastMsgTime, fadeInTime, fadeOutTime, Color.color((float)0xB0 / 0xff, (float)0x00/0xff, (float)0x20/0xff));
    }



    private AnchorPane loadFXML(String filename) {
        var root = new AnchorPane();
        var fxmlLoader = new FXMLLoader(getClass().getResource(filename));
        try {
            root = fxmlLoader.load();
            IController guiController = fxmlLoader.getController();
            guiController.bind(mActions);
            mControllers.put(filename, guiController);
        } catch (IOException e) {
            Logger.log(Level.ERROR, "IOException loading .fxml file:" + e.toString());
        }
        return root;
    }
}
