package no.ntnu.imt3281.ludo.gui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.api.FieldNames;
import no.ntnu.imt3281.ludo.api.FriendStatus;
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

    private class FXMLDocument {
        Parent root;
        BaseController controller;
    }

    /**
     *
     */
    public void bind(Stage stage, Actions actions, StateManager sm) {
        mStage = stage;
        mActions = actions;
        mStateManager = sm;
    }

    /**
     *
     */
    public void renderLogin() {

        var login = this.loadFXML(Path.LOGIN);
        var root = new Scene(login.root);
        Platform.runLater(() -> {
            mStage.setScene(root);
            mStage.show();
        });
    }


    /**
     *
     */
    public void renderUser() {
        var user = this.loadFXML(Path.USER);
        var userController = (UserController)user.controller;
        var state = mStateManager.copy();
        var userJson = state.userlist.get(state.userId).json;

        Platform.runLater(()-> {
            userController.mAvatar.setImage(new Image(userJson.getString(FieldNames.AVATAR_URI)));
            userController.mAvatarURL.setText(userJson.getString(FieldNames.AVATAR_URI));
            userController.mUsername.setText(userJson.getString(FieldNames.USERNAME));
            userController.mEmail.setText(userJson.getString(FieldNames.EMAIL));

            mStage.setScene(new Scene(user.root));
            mStage.show();
        });
    }

    /**
     *
     */
    public void renderLive() {
        var live = this.loadFXML(Path.LIVE);

        Platform.runLater(()-> {
            mStage.setScene(new Scene(live.root));
            mStage.show();
        });
    }

    /**
     *
     */
    public void renderGameTabs() {

        var live = this.mDocuments.get(Path.LIVE);
        var liveController = (LiveController)live.controller;
        var state = mStateManager.copy();

        Platform.runLater(()-> {
            liveController.mTabGames.getTabs().clear();

            state.activeGames.forEach(id -> {
                var gameTab = this.loadFXML(Path.GAME_TAB, id);
                Tab tab = new Tab("Game" + id);
                tab.setContent(gameTab.root);
                liveController.mTabGames.getTabs().add(tab);
            });
        });
    }

    /**
     *
     */
    public void renderChatTabs() {

        var liveController = (LiveController)this.mDocuments.get(Path.LIVE).controller;
        var state = mStateManager.copy();

        Platform.runLater(()-> {
            liveController.mTabChats.getTabs().clear();

            state.activeChats.forEach(id -> {
                var chatTab = this.loadFXML(Path.CHAT_TAB, id);
                Tab tab = new Tab("Chat" + id);
                tab.setContent(chatTab.root);
                liveController.mTabChats.getTabs().add(tab);
            });
        });
    }


    /**
     *
     */
    public void renderOverview() {
        var overview = this.loadFXML(Path.OVERVIEW);
        var overviewController = (OverviewController)overview.controller;

        Platform.runLater(()-> {
            overviewController.renderButtonTexts();
            mStage.setScene(new Scene(overview .root));
            mStage.show();
        });
    }




    /**
     *
     */
    public void renderGameList() {

        var overview = (OverviewController)this.getController(Path.OVERVIEW);
        var state = mStateManager.copy();

        Platform.runLater(()-> {

            state.activeGames.forEach(id -> {
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController) item.controller;
                var game = state.gamelist.get(id);
                var name = game.getString(FieldNames.NAME);
                var playerCount = game.getJSONArray(FieldNames.PLAYER_ID).length();

                itemController.mType = ListItemType.GAME;
                itemController.mOverview = overview;
                itemController.init(ListItemType.GAME, id, overview, name + " [" + playerCount + "/4]");
                overview.mBoxGames.getChildren().add(item.root);
            });

            state.gameInvites.forEach(id -> {
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController) item.controller;
                var game = state.gamelist.get(id);
                var name = game.getString(FieldNames.NAME);

                itemController.init(ListItemType.GAME_INVITE, id, overview, name + " invite"); // TODO i18n
                overview.mBoxGames.getChildren().add(item.root);
            });
        });
    }

    /**
     *
     */
    public void renderChatList() {
        var overview = (OverviewController)this.getController(Path.OVERVIEW);
        var state = mStateManager.copy();

        Platform.runLater(()-> {

            state.activeChats.forEach(id -> {
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController) item.controller;

                var chat = state.chatlist.get(id);
                var name = chat.json.getString(FieldNames.NAME);
                var participantCount = chat.json.getJSONArray(FieldNames.PARTICIPANT_ID).length();

                itemController.init(ListItemType.CHAT, id, overview, name + " ["+ participantCount+" people]"); // TODO i18n
                overview.mBoxChats.getChildren().add(item.root);
            });

            state.chatInvites.forEach(id -> {
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController) item.controller;

                var chat = state.chatlist.get(id);
                var name = chat.json.getString(FieldNames.NAME);

                itemController.init(ListItemType.CHAT_INVITE, id, overview, name + " invite");// TODO i18n
                overview.mBoxChats.getChildren().add(item.root);
            });
        });
    }

    /**
     *
     */
    public void renderFriendList() {
        var overview = (OverviewController)this.getController(Path.OVERVIEW);
        var state = mStateManager.copy();

        Platform.runLater(()-> {
            state.friendlist.forEach((id, friend) -> {
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController)item.controller;
                var name = friend.getString(FieldNames.USERNAME);
                var status = FriendStatus.fromInt(friend.getInt(FieldNames.STATUS));
                if (status == null) {
                    Logger.log(Level.WARN, "status == null");
                    return;
                }

                switch (status) {
                    case FRIENDED:
                        itemController.init(ListItemType.FRIEND, id, overview, name);
                        overview.mBoxFriends.getChildren().add(item.root);
                        break;

                    case PENDING:
                        itemController.init(ListItemType.FRIEND_REQUEST, id, overview, name + " [pending]"); // TODO i18n
                        overview.mBoxFriends.getChildren().add(item.root);
                        break;
                }
            });
        });
    }

    /**
     *
     */
    public void renderUserList() {
        var overview = (OverviewController)this.getController(Path.OVERVIEW);
        var state = mStateManager.copy();

        Platform.runLater(()-> {

            state.userlist.forEach((id, user) -> {
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController)item.controller;
                var name = user.json.getString(FieldNames.USERNAME);

                itemController.init(ListItemType.USER, id, overview, name);
                overview.mBoxUsers.getChildren().add(item.root);
            });

            state.friendlist.forEach((id, friend) -> {
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController)item.controller;
                var name = friend.getString(FieldNames.USERNAME);
                var status = FriendStatus.fromInt(friend.getInt(FieldNames.STATUS));
                if (status == null) {
                    Logger.log(Level.WARN, "status == null");
                    return;
                }

                switch (status) {
                    case IGNORED:
                        itemController.init(ListItemType.USER_IGNORED, id, overview, name + " [ignored]"); // TODO i18n
                        overview.mBoxUsers.getChildren().add(item.root);
                        break;
                }
            });
        });
    }

    /**
     *
     */
    public void newGame(int id) {
        var live = (LiveController)this.getController(Path.LIVE);

        Platform.runLater(() -> {
            var gameTab = this.loadFXML(Path.GAME_TAB, id);
            Tab tab = new Tab("Game " + id);
            tab.setContent(gameTab.root);
            live.mTabGames.getTabs().add(tab);
        });
    }

    public void newChat(int id) {
        var live = (LiveController)this.getController(Path.LIVE);
        var chatTab = this.loadFXML(Path.CHAT_TAB, id);
        var chatTabController = (ChatTabController)chatTab.controller;
        chatTabController.mId = id;

        Platform.runLater(() -> {
            Tab tab = new Tab("Chat " + id);
            tab.setContent(chatTab.root);
            live.mTabChats.getTabs().add(tab);
        });
    }

    /**
     *
     */
    public void newMessage(int chatId, int userId, String message) {
        var chat = (ChatTabController)this.getController(Path.CHAT_TAB, chatId);
        var state = mStateManager.copy();

        Platform.runLater(() -> {
            var user = state.userlist.get(userId);
            var username = user.json.getString(FieldNames.USERNAME);
            var avatarURI = user.json.getString(FieldNames.AVATAR_URI);

            var chatItem = this.loadFXML(Path.CHAT_ITEM);
            var chatItemController = (ChatItemController)chatItem.controller;
            chatItemController.mMessage.setText(username + ": " + message);
            chat.mMessageList.getChildren().add(chatItem.root);
            chatItemController.mAvatar.setImage(user.avatar);
        });
    }


    /**
     *
     */
    public void renderGame(int id) {

    }


    public void toastError(String message) {
        int toastMsgTime = 1500;
        int fadeInTime = 100;
        int fadeOutTime = 100;
        final var red =
                Color.color((float) 0xB0 / 0xff, (float) 0x00 / 0xff, (float) 0x20 / 0xff);
        Toast.makeText(mStage, message, toastMsgTime, fadeInTime, fadeOutTime, red);
    }

    private FXMLDocument loadFXML(String filename) {
        return loadFXML(filename, -1);
    }

    private FXMLDocument loadFXML(String filename, int id) {
        var fxmlDocument = new FXMLDocument();

        var fxmlLoader = new FXMLLoader(getClass().getResource(filename));
        fxmlLoader.setResources(ResourceBundle.getBundle(Path.RESOURCE_BUNDLE));

        try {
            fxmlDocument.root = fxmlLoader.load();
            fxmlDocument.controller = fxmlLoader.getController();
            fxmlDocument.controller.bind(mActions, mStateManager);

            if (id != -1) {
                mDocuments.put(filename + String.valueOf(id), fxmlDocument);
            } else {
                mDocuments.put(filename, fxmlDocument);
            }

        } catch (IOException e) {
            Logger.log(Level.ERROR, "IOException loading .fxml file:" + e.toString());
        }

        return fxmlDocument;
    }


    /**
     * Shortcut function for getting a controller
     *
     * @param path file path of fxml file
     *
     * @return BaseController interface
     */
    private BaseController getController(String path) {
        return this.mDocuments.get(path).controller;
    }

    private BaseController getController(String path, int id) {
        return this.mDocuments.get(path + String.valueOf(id)).controller;
    }
}
