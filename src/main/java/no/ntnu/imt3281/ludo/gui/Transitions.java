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
import no.ntnu.imt3281.ludo.client.*;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

import static no.ntnu.imt3281.ludo.api.FieldNames.CHAT_ID;

/**
 * Controls FXML Controllers and how they transition
 */
public class Transitions {

    private Stage mStage;
    private Actions mActions;
    private Map<String, FXMLDocument> mDocuments = new HashMap<>();

    /**
     * Constructor
     */
    private class FXMLDocument {
        Parent root;
        BaseController controller;
    }

    /**
     * Bind dependencies
     *
     * @param stage fxml stage
     * @param actions used to dispatch input from user
     */
    public void bind(Stage stage, Actions actions) {
        mStage = stage;
        mActions = actions;
    }


    /**
     * Render login scene
     * Override without optional email parameter
     */
    public void renderLogin() {this.renderLogin("");}


    /**
     * Render login scene
     *
     * @param email for autocompletion
     */
    public void renderLogin(String email) {
        Platform.runLater(() -> {

            var login = this.loadFXML(Path.LOGIN);
            var loginController = (LoginController)login.controller;
            loginController.mLoginEmail.setText(email);

            var root = new Scene(login.root);
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
        Platform.runLater(() -> {

            var userDoc = this.loadFXML(Path.USER);
            var userController = (UserController) userDoc.controller;

            userController.mAvatarURL.setText(user.json.getString(FieldNames.AVATAR_URI));
            userController.mUsername.setText(user.json.getString(FieldNames.USERNAME));

            // TODO Email does not exist in get_user_request.
            // userController.mEmail.setText(user.json.getString(FieldNames.EMAIL));

            mStage.setScene(new Scene(userDoc.root));
            mStage.show();
        });
    }

    /**
     *
     */
    public void renderLive() {
        Platform.runLater(() -> {
            var live = this.loadFXML(Path.LIVE);

            mStage.setScene(new Scene(live.root));
            mStage.show();
        });
    }

    /**
     *
     */
    public void renderGameTabs(Map<Integer, JSONObject> activeGames) {
        Platform.runLater(() -> {

            var live = this.mDocuments.get(Path.LIVE);
            var liveController = (LiveController) live.controller;

            liveController.mTabGames.getTabs().clear();

            activeGames.forEach((id, game) -> {
                var gameTab = this.loadFXML(Path.GAME_TAB, id);
                Tab tab = new Tab(game.getString(FieldNames.NAME));
                tab.setContent(gameTab.root);
                liveController.mTabGames.getTabs().add(tab);
            });
        });
    }

    /**
     *
     */
    public void renderChatTabs(Map<Integer, Chat> activeChats) {
        Platform.runLater(() -> {

            var liveController = (LiveController) this.mDocuments.get(Path.LIVE).controller;

            liveController.mTabChats.getTabs().clear();

            activeChats.forEach((id, chat) -> {
                var chatTab = this.loadFXML(Path.CHAT_TAB, id);
                var chatTabController = (ChatTabController) chatTab.controller;
                Tab tab = new Tab(chat.json.getString(FieldNames.NAME));
                tab.setContent(chatTab.root);
                liveController.mTabChats.getTabs().add(tab);

                chatTabController.mId = id;

                chat.messages.forEach(messageJSON -> {
                    var message = messageJSON.getString(FieldNames.MESSAGE);
                    var username = messageJSON.getString(FieldNames.USERNAME);
                    var chatItem = this.loadFXML(Path.CHAT_ITEM);
                    var chatItemController = (ChatItemController) chatItem.controller;
                    chatItemController.mMessage.setText(username + ": " + message);
                    chatTabController.mMessageList.getChildren().add(chatItem.root);
                });
            });
        });
    }

    /**
     *
     */
    public void renderOverview(String searchGames, String searchChats, String searchFriends, String searchUsers) {
        Platform.runLater(() -> {

            var overview = this.loadFXML(Path.OVERVIEW);
            var overviewController = (OverviewController) overview.controller;

            overviewController.renderButtonTexts();

            overviewController.mSearchGames.setText(searchGames);
            overviewController.mSearchChats.setText(searchChats);
            overviewController.mSearchFriends.setText(searchFriends);
            overviewController.mSearchUsers.setText(searchUsers);

            mStage.setScene(new Scene(overview.root));
            mStage.show();
        });
    }

    /**
     *
     */
    public void renderGamesList(List<JSONObject> activeGames, List<JSONObject> gameInvites) {
        Platform.runLater(() -> {

            var overview = (OverviewController) this.getController(Path.OVERVIEW);


            overview.mListGames.getChildren().clear();

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
        Platform.runLater(() -> {

            var overview = (OverviewController) this.getController(Path.OVERVIEW);


            overview.mListChats.getChildren().clear();

            activeChats.forEach(chat -> {
                var id = chat.json.getInt(CHAT_ID);
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController) item.controller;

                var name = chat.json.getString(FieldNames.NAME);
                var participantCount = chat.json.getJSONArray(FieldNames.PARTICIPANT_ID).length();

                itemController.init(ListItemType.CHAT, id, overview, name + " [" + participantCount + " people]"); // TODO
                                                                                                                   // i18n
                overview.mListChats.getChildren().add(item.root);
            });

            chatInvites.forEach(chatInvite -> {

                var id = chatInvite.getInt(CHAT_ID);
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
        Platform.runLater(() -> {

            var overview = (OverviewController) this.getController(Path.OVERVIEW);

            overview.mListFriends.getChildren().clear();

            friendsList.forEach((friend) -> {
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController) item.controller;

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
                    itemController.init(ListItemType.FRIEND_REQUEST, userId, overview, name + " [pending]"); // TODO
                                                                                                             // i18n
                    overview.mListFriends.getChildren().add(item.root);
                    break;
                }
            });
        });
    }

    /**
     * Render a list of users in the overview scene
     */
    public void renderUserList(List<JSONObject> usersList, List<JSONObject> ignoredList) {
        Platform.runLater(() -> {

            var overview = (OverviewController) this.getController(Path.OVERVIEW);

            overview.mListUsers.getChildren().clear();

            usersList.forEach(user -> {
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController) item.controller;

                var userId = user.getInt(FieldNames.USER_ID);
                var name = user.getString(FieldNames.USERNAME);

                itemController.init(ListItemType.USER, userId, overview, name);
                overview.mListUsers.getChildren().add(item.root);
            });

            ignoredList.forEach(friend -> {
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController) item.controller;

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
     * New game tab, append to existing tabs
     */
    public void newGame(int id, JSONObject game) {
        Platform.runLater(() -> {
            var live = (LiveController) this.getController(Path.LIVE);

            var gameTab = this.loadFXML(Path.GAME_TAB, id);
            Tab tab = new Tab(game.getString(FieldNames.NAME));

            tab.setContent(gameTab.root);
            live.mTabGames.getTabs().add(tab);
        });
    }

    /**
     * New chat tab, append to existing tabs
     */
    public void newChat(Chat chat) {
        Platform.runLater(() -> {

            var id = chat.json.getInt(CHAT_ID);
            var live = (LiveController) this.getController(Path.LIVE);

            var chatTab = this.loadFXML(Path.CHAT_TAB, id);
            var chatTabController = (ChatTabController) chatTab.controller;
            Tab tab = new Tab(chat.json.getString(FieldNames.NAME));
            chatTabController.mId = id;

            tab.setContent(chatTab.root);
            live.mTabChats.getTabs().add(tab);
        });
    }

    /**
     * Add new chat message to existing chat
     *
     * @param chatId id of eixisting chat
     * @param username username of the person who sent the chat message
     * @param message message
     */
    public void newMessage(int chatId, String username, String message) {
        Platform.runLater(() -> {
            var chat = (ChatTabController) this.getController(Path.CHAT_TAB, chatId);
            var chatItem = this.loadFXML(Path.CHAT_ITEM);
            var chatItemController = (ChatItemController) chatItem.controller;
            chatItemController.mMessage.setText(username + ": " + message);
            chat.mMessageList.getChildren().add(chatItem.root);
        });
    }

    public void toastError(String message) {
        Platform.runLater(() -> {
            int toastMsgTime = 1500;
            int fadeInTime = 100;
            int fadeOutTime = 100;
            final var red = Color.color((float) 0xB0 / 0xff, (float) 0x00 / 0xff, (float) 0x20 / 0xff);
            Toast.makeText(mStage, message, toastMsgTime, fadeInTime, fadeOutTime, red);
        });
    }

    ////////////////////////////////////////////////////////////
    //
    // ----------------- PRIVATE FUNCTIONS -------------------
    //
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
            fxmlDocument.controller.bind(mActions);

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
