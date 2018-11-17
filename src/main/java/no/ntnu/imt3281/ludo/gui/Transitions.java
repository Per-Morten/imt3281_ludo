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
import no.ntnu.imt3281.ludo.api.FieldNames;
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
    private Map<String, FXMLDocument> mDocuments = new HashMap<>();

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

        var login = this.loadFXML(Path.LOGIN);
        var root = new Scene(login.root);
        Platform.runLater(() -> {
            mStage.setScene(root);
            mStage.show();
        });
    }

    public void renderLive() {
        var live = this.loadFXML(Path.LIVE);

        Platform.runLater(()-> {
            mStage.setScene(new Scene(live.root));
            mStage.show();
        });
    }

    public void renderGameTabs() {

        var live = this.mDocuments.get(Path.LIVE);
        var liveController = (LiveController)live.controller;
        var state = mStateManager.copy();

        Platform.runLater(()-> {
            liveController.mTabGames.getTabs().clear();

            state.activeGames.forEach((id, game) -> {
                Logger.log(Level.DEBUG, "New game " + id);

                var gameTab = this.loadFXML(Path.GAME_TAB);
                Tab tab = new Tab("Game" + id);
                tab.setContent(gameTab.root);
                liveController.mTabGames.getTabs().add(tab);
            });
        });
    }

    public void renderChatTabs() {

        var liveController = (LiveController)this.mDocuments.get(Path.LIVE).controller;
        var state = mStateManager.copy();

        Platform.runLater(()-> {
            liveController.mTabChats.getTabs().clear();

            state.activeChats.forEach((id, chat) -> {
                var chatTab = this.loadFXML(Path.CHAT_TAB);
                Tab tab = new Tab("Chat" + id);
                tab.setContent(chatTab.root);
                liveController.mTabChats.getTabs().add(tab);
            });
        });
    }


    public void renderOverview() {
        var search = this.loadFXML(Path.OVERVIEW);
        var overviewController = (OverviewController)search.controller;
        var state = mStateManager.copy();

        Platform.runLater(()-> {

            mStage.setScene(new Scene(search.root));
            mStage.show();
        });
    }


    public void renderGameList() {

        var overview = (OverviewController)this.getController(Path.OVERVIEW);
        var state = mStateManager.copy();

        Platform.runLater(()-> {
            state.gameRange.forEach(game -> {
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController) item.controller;
                var name = game.getString(FieldNames.NAME);
                var playerCount = game.getJSONArray(FieldNames.PLAYER_ID).length();

                itemController.mText.setText(name + " [" + playerCount + "/4]");
                overview.mBoxGames.getChildren().add(item.root);
            });
        });
    }

    public void renderChatList() {
        var overview = (OverviewController)this.getController(Path.OVERVIEW);
        var state = mStateManager.copy();

        Platform.runLater(()-> {
            state.chatRange.forEach(chat -> {
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController)item.controller;
                var name = chat.getString(FieldNames.NAME);
                var participantCount = chat.getJSONArray(FieldNames.PARTICIPANT_ID).length();
                itemController.mText.setText(name + "[" + participantCount + " people]");
                overview.mBoxChats.getChildren().add(item.root);
            });
        });
    }

    public void renderFriendList() {
        var overview = (OverviewController)this.getController(Path.OVERVIEW);
        var state = mStateManager.copy();

        Platform.runLater(()-> {
            state.friendRange.forEach(friend -> {
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController)item.controller;
                var name = friend.getString(FieldNames.USERNAME);
                itemController.mText.setText(name);
                overview.mBoxFriends.getChildren().add(item.root);
            });
        });
    }

    public void renderUserList() {
        var overview = (OverviewController)this.getController(Path.OVERVIEW);
        var state = mStateManager.copy();

        Platform.runLater(()-> {
            state.userRange.forEach(user -> {
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController)item.controller;
                var name = user.getString(FieldNames.USERNAME);
                itemController.mText.setText(name);
                overview.mBoxUsers.getChildren().add(item.root);
            });
        });
    }

    public void renderUser() {
        var user = this.loadFXML(Path.USER);
        var userController = (OverviewController)user.controller;
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
        fxmlLoader.setResources(ResourceBundle.getBundle(Path.RESOURCE_BUNDLE));

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

    /**
     * Shortcut function for getting controller
     *
     * @param path file path of fxml file
     *
     * @return IController interface
     */
    private IController getController(String path) {
        return this.mDocuments.get(path).controller;
    }
}
