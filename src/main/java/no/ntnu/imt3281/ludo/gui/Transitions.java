package no.ntnu.imt3281.ludo.gui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.client.Actions;
import no.ntnu.imt3281.ludo.client.StateManager;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;

/**
 * Controls FXML Controllers and how they transition
 */
public class Transitions {

    private Stage mStage;
    private Actions mActions;
    private StateManager mStateManager;
    private Map<String, FXMLDocument> mDocuments = new HashMap<String, FXMLDocument>();

    public class FXMLDocument {
        public Parent root;
        public IController controller;
    }

    public void bind(Stage stage, Actions actions, StateManager sm) {
        mStage = stage;
        mActions = actions;
        mStateManager = sm;
    }

    public void renderLogin() {

        var login = this.loadFXML("SceneLogin.fxml");
        var root = new Scene(login.root);
        Platform.runLater(() -> {
            mStage.setScene(root);
            mStage.show();
        });
    }

    public void renderLive() {
        var live = this.loadFXML("SceneLive.fxml");

        Platform.runLater(()-> {
            mStage.setScene(new Scene(live.root));
            mStage.show();
        });
    }

    public void renderGameTabs() {

        var live = this.mDocuments.get("SceneLive.fxml");
        var liveController = (SceneLiveController)live.controller;
        var state = mStateManager.copy();

        Platform.runLater(()-> {
            liveController.mTabGames.getTabs().clear();

            state.activeGames.forEach((id, game) -> {
                Logger.log(Level.DEBUG, "New game " + id);

                var gameTab = this.loadFXML("TabGame.fxml");
                Tab tab = new Tab("Game" + id);
                tab.setContent(gameTab.root);
                liveController.mTabGames.getTabs().add(tab);
            });
        });
    }

    public void renderChatTabs() {

        var live = this.mDocuments.get("SceneLive.fxml");
        var liveController = (SceneLiveController)live.controller;
        var state = mStateManager.copy();

        Platform.runLater(()-> {
            liveController.mTabChats.getTabs().clear();

            state.activeChats.forEach((id, chat) -> {

                Logger.log(Level.DEBUG, "New Chat " + id);
                var chatTab = this.loadFXML("TabChat.fxml");
                Tab tab = new Tab("Chat" + id);
                tab.setContent(chatTab.root);
                liveController.mTabChats.getTabs().add(tab);
            });
        });
    }


    public void renderSearch() {
        var search = this.loadFXML("SceneSearch.fxml");
        var searchController = (SceneSearchController)search.controller;
        var state = mStateManager.copy();

        Platform.runLater(()-> {

            state.gameRange.forEach(game -> {
                Logger.log(Level.DEBUG, "" + game.toString());

                var item = this.loadFXML("ListItem.fxml");
                var itemController = (ListItemController)item.controller;
                var name = game.getString("name");
                var playerCount = game.getJSONArray("player_id").length();

                itemController.mText.setText(name + " [" + playerCount + "/4]");
                searchController.mBoxGames.getChildren().add(item.root);
            });

            state.chatRange.forEach(chat -> {
                var item = this.loadFXML("ListItem.fxml");
                var itemController = (ListItemController)item.controller;

                var name = chat.getString("name");
                var participantCount = chat.getJSONArray("participant_id").length();
                itemController.mText.setText(name + "[" + participantCount + " people]");
                searchController.mBoxChats.getChildren().add(item.root);
            });

            state.friendRange.forEach(friend -> {
                var item = this.loadFXML("ListItem.fxml");
                var itemController = (ListItemController)item.controller;

                var name = friend.getString("username");

                itemController.mText.setText(name);
                searchController.mBoxFriends.getChildren().add(item.root);
            });

            state.userRange.forEach(user -> {
                var item = this.loadFXML("ListItem.fxml");
                var itemController = (ListItemController)item.controller;

                var name = user.getString("username");

                itemController.mText.setText(name);
                searchController.mBoxUsers.getChildren().add(item.root);
            });

            mStage.setScene(new Scene(search.root));
            mStage.show();
        });
    }



    public void renderUser() {
        var user = this.loadFXML("SceneUser.fxml");
        var userController = (SceneSearchController)user.controller;
        var state = mStateManager.copy();

        Platform.runLater(()-> {
            mStage.setScene(new Scene(user.root));
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
            fxmlDocument.controller.bind(mActions, mStateManager);
            mDocuments.put(filename, fxmlDocument);
        } catch (IOException e) {
            Logger.log(Level.ERROR, "IOException loading .fxml file:" + e.toString());
        }

        return fxmlDocument;
    }
}
