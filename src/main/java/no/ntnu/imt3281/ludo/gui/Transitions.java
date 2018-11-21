package no.ntnu.imt3281.ludo.gui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.api.FieldNames;
import no.ntnu.imt3281.ludo.api.FriendStatus;
import no.ntnu.imt3281.ludo.client.Actions;
import no.ntnu.imt3281.ludo.client.Chat;
import no.ntnu.imt3281.ludo.client.StateManager;
import no.ntnu.imt3281.ludo.client.User;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

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
     * Loads and renders the user scene
     *
     * @param user logged in user
     */
    public void renderUser(User user) {
        Logger.log(Level.INFO, "Transition -> Render User");

        var userDoc = this.loadFXML(Path.USER);
        var userController = (UserController)userDoc.controller;

        Platform.runLater(()-> {
            userController.mAvatarURL.setText(user.json.getString(FieldNames.AVATAR_URI));
            userController.mUsername.setText(user.json.getString(FieldNames.USERNAME));

            // TODO Email does not exist in get_user_request.
            //userController.mEmail.setText(user.json.getString(FieldNames.EMAIL));

            mStage.setScene(new Scene(userDoc.root));
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
    public void renderGameTabs(Map<Integer, JSONObject> activeGames) {

        var live = this.mDocuments.get(Path.LIVE);
        var liveController = (LiveController)live.controller;

        Platform.runLater(()-> {
            liveController.mTabGames.getTabs().clear();

            activeGames.forEach((id, game) -> {
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
    public void renderChatTabs(Map<Integer, Chat> activeChats) {

        var liveController = (LiveController)this.mDocuments.get(Path.LIVE).controller;

        Platform.runLater(()-> {
            liveController.mTabChats.getTabs().clear();

            activeChats.forEach((id, chat) -> {
                var chatTab = this.loadFXML(Path.CHAT_TAB, id);
                var chatTabController = (ChatTabController)chatTab.controller;
                Tab tab = new Tab("Chat" + id);
                tab.setContent(chatTab.root);
                liveController.mTabChats.getTabs().add(tab);

                chatTabController.mId = id;

                chat.messages.forEach(messageJSON -> {
                    var message = messageJSON.getString(FieldNames.MESSAGE);
                    var username = messageJSON.getString(FieldNames.USERNAME);
                    var chatItem = this.loadFXML(Path.CHAT_ITEM);
                    var chatItemController = (ChatItemController)chatItem.controller;
                    chatItemController.mMessage.setText(username + ": " + message);
                    chatTabController.mMessageList.getChildren().add(chatItem.root);
                });
            });
        });
    }

    /**
     *
     */
    public void renderOverview() {
        var overview = this.loadFXML(Path.OVERVIEW);
        var state = mStateManager.copy();
        var overviewController = (OverviewController)overview.controller;

        Platform.runLater(()-> {
            overviewController.renderButtonTexts();

            overviewController.mSearchGames.setText(state.searchGames);
            overviewController.mSearchChats.setText(state.searchChats);
            overviewController.mSearchFriends.setText(state.searchFriends);
            overviewController.mSearchUsers.setText(state.searchUsers);

            mStage.setScene(new Scene(overview .root));
            mStage.show();
        });
    }

    /**
     *
     */
    public void renderGamesList(List<JSONObject> activeGames, List<JSONObject>  gameInvites) {

        var overview = (OverviewController)this.getController(Path.OVERVIEW);

        Platform.runLater(()-> {

            overview.mListGames.getChildren().clear();

            Logger.log(Level.DEBUG, "activeGames.size(): " + activeGames.size());

            activeGames.forEach(game -> {
                var id = game.getInt(FieldNames.GAME_ID);
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController) item.controller;
                var name = game.getString(FieldNames.NAME);
                var playerCount = game.getJSONArray(FieldNames.PLAYER_ID).length();

                itemController.mType = ListItemType.GAME;
                itemController.mOverview = overview;
                itemController.init(ListItemType.GAME, id, overview, name + " [" + playerCount + "/4]");
                overview.mListGames.getChildren().add(item.root);
            });

            gameInvites.forEach(gameInvite -> {

                var id = gameInvite.getInt(FieldNames.GAME_ID);

                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController) item.controller;
                var name = gameInvite.getString(FieldNames.NAME);

                itemController.init(ListItemType.GAME_INVITE, id, overview, name + " invite"); // TODO i18n
                overview.mListGames.getChildren().add(item.root);
            });
        });
    }

    /**
     *
     */
    public void renderChatsList(List<Chat> activeChats, List<JSONObject> chatInvites) {
        var overview = (OverviewController)this.getController(Path.OVERVIEW);

        Platform.runLater(()-> {

            overview.mListChats.getChildren().clear();

            Logger.log(Level.DEBUG, "activeChats.size(): " + activeChats.size());

            activeChats.forEach(chat -> {
                var id = chat.json.getInt(FieldNames.CHAT_ID);
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController) item.controller;

                var name = chat.json.getString(FieldNames.NAME);
                var participantCount = chat.json.getJSONArray(FieldNames.PARTICIPANT_ID).length();

                itemController.init(ListItemType.CHAT, id, overview, name + " ["+ participantCount+" people]"); // TODO i18n
                overview.mListChats.getChildren().add(item.root);
            });

            chatInvites.forEach(chatInvite -> {

                var id = chatInvite.getInt(FieldNames.CHAT_ID);
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController) item.controller;

                var name = chatInvite.getString(FieldNames.NAME);

                itemController.init(ListItemType.CHAT_INVITE, id, overview, name + " invite");// TODO i18n
                overview.mListChats.getChildren().add(item.root);
            });
        });
    }

    /**
     *
     */
    public void renderFriendList(List<JSONObject> friendsList) {
        var overview = (OverviewController)this.getController(Path.OVERVIEW);

        Platform.runLater(()-> {
            overview.mListFriends.getChildren().clear();

            friendsList.forEach((friend) -> {
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController)item.controller;

                var userId = friend.getInt(FieldNames.USER_ID);
                var name = friend.getString(FieldNames.USERNAME);
                var status = FriendStatus.fromInt(friend.getInt(FieldNames.STATUS));
                if (status == null) {
                    Logger.log(Level.WARN, "status == null");
                    return;
                }

                switch (status) {
                    case FRIENDED:
                        itemController.init(ListItemType.FRIEND, userId, overview, name);
                        overview.mListFriends.getChildren().add(item.root);
                        break;

                    case PENDING:
                        itemController.init(ListItemType.FRIEND_REQUEST, userId, overview, name + " [pending]"); // TODO i18n
                        overview.mListFriends.getChildren().add(item.root);
                        break;
                }
            });
        });
    }

    /**
     *
     */
    public void renderUserList(List<JSONObject> usersList, List<JSONObject> ignoredList) {
        var overview = (OverviewController)this.getController(Path.OVERVIEW);

        Platform.runLater(()-> {
            overview.mListUsers.getChildren().clear();

            usersList.forEach(user -> {
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController)item.controller;

                var userId = user.getInt(FieldNames.USER_ID);
                var name = user.getString(FieldNames.USERNAME);

                itemController.init(ListItemType.USER, userId, overview, name);
                overview.mListUsers.getChildren().add(item.root);
            });

            ignoredList.forEach(friend -> {
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController)item.controller;

                var friendId = friend.getInt(FieldNames.USER_ID);
                var name = friend.getString(FieldNames.USERNAME);
                var status = FriendStatus.fromInt(friend.getInt(FieldNames.STATUS));
                if (status == null) {
                    Logger.log(Level.WARN, "status == null");
                    return;
                }

                itemController.init(ListItemType.USER_IGNORED, friendId, overview, name + " [ignored]"); // TODO i18n
                overview.mListUsers.getChildren().add(item.root);
            });
        });
    }

    /**
     *
     */
    public void newGame(int id, JSONObject game) {
        var live = (LiveController)this.getController(Path.LIVE);

        Platform.runLater(() -> {
            var gameTab = this.loadFXML(Path.GAME_TAB, id);
            Tab tab = new Tab("Game " + id);
            tab.setContent(gameTab.root);
            live.mTabGames.getTabs().add(tab);
        });
    }

    public void newChat(int id, JSONObject chat) {
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
    public void newMessage(int chatId, String username, String message) {
        var chat = (ChatTabController)this.getController(Path.CHAT_TAB, chatId);

        Platform.runLater(() -> {

            var chatItem = this.loadFXML(Path.CHAT_ITEM);
            var chatItemController = (ChatItemController)chatItem.controller;
            chatItemController.mMessage.setText(username + ": " + message);
            chat.mMessageList.getChildren().add(chatItem.root);
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
