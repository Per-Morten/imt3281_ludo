package no.ntnu.imt3281.ludo.gui;

import com.notification.NotificationFactory;
import com.notification.NotificationManager;
import com.notification.manager.SimpleManager;
import com.notification.types.TextNotification;
import com.theme.ThemePackagePresets;
import com.utils.Time;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.api.FieldNames;
import no.ntnu.imt3281.ludo.api.FriendStatus;
import no.ntnu.imt3281.ludo.client.*;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * Controls FXML Controllers and how they transition
 */
public class Transitions {

    private Stage mStage;
    private Actions mActions;
    private Map<String, FXMLDocument> mDocuments = new HashMap<>();
    private NotificationFactory mFactory = new NotificationFactory(ThemePackagePresets.cleanLight());
    private NotificationManager mPlain = new SimpleManager(NotificationFactory.Location.NORTHEAST);

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

            mStage.setScene(new Scene(login.root));
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

            userController.mAvatarURL.setText(user.avatarURL);
            userController.mUsername.setText(user.username);

            // TODO Email does not exist in get_user_request.
            // userController.mEmail.setText(user.json.getString(FieldNames.EMAIL));

            mStage.setScene(new Scene(userDoc.root));
            mStage.show();
        });
    }

    /**
     * Render live controller
     */
    public void renderLive() {
        Platform.runLater(() -> {
            var live = this.loadFXML(Path.LIVE);

            mStage.setScene(new Scene(live.root));
            mStage.show();
        });
    }

    /**
     * Clear all game tabs and create them again with provded game data (excluding game state)
     */
    public void renderGameTabs(Map<Integer, Game> activeGames, Map<Integer, Chat> activeChats, int userId) {
        Platform.runLater(() -> {

            var live = this.mDocuments.get(Path.LIVE);
            var liveController = (LiveController) live.controller;

            liveController.mTabGames.getTabs().clear();

            activeGames.forEach((id, game) -> {
                var gameTab = this.loadFXML(Path.GAME_TAB, id);
                Tab tab = new Tab(game.name);
                tab.setContent(gameTab.root);

                var initial = LudoBoard.getInitialPositions();
                var gameTabController = (GameTabController)gameTab.controller;
                gameTabController.mId = game.id;
                gameTabController.mChatId = game.chatId;
                gameTabController.setPiecePositions(initial);
               // gameTabController.setPlayerLabels(game.playerNames);

                // Compute color
                int i = 0;
                for (var pid: game.playerId) {
                    if (pid.equals(userId)) {
                        gameTabController.mColor = i;
                        break;
                    }
                    i++;
                };
                if (i == 4) Logger.log(Level.WARN, "Logged in user not found in active games player id's");


                // Hide start button if not game master
                if (game.ownerId != userId) {
                    gameTabController.mBtnStartGame.setVisible(false);
                    gameTabController.mCheckboxAllowRandoms.setVisible(false);
                } else {
                    gameTabController.mCheckboxAllowRandoms.setSelected(game.allowRandoms);
                }

                var gamechat = activeChats.get(game.chatId);
                gamechat.messages.forEach(message -> {
                    gameTabController.chatArea.appendText(message.username + ": " + message.message + "\n");
                });

                liveController.mTabGames.getTabs().add(tab);
            });
        });
    }

    /**
     * Update existing gameTabs with game state
     */
    public void updateGameTabs(Map<Integer, GameState> gameStates) {
        Platform.runLater(() -> {
            gameStates.forEach((id, gameState) -> {
                var gameTabController = (GameTabController)this.getController(Path.GAME_TAB, id);
                gameTabController.setPlayerLabels(gameState.playerNames);
                gameTabController.setPiecePositions(gameState.piecePositions);
                gameTabController.setRolledDiceImage(gameState.previousDiceThrow);
            });
        });
    }

    /**
     * Clear all chat tabs, and render them again
     */
    public void renderChatTabs(Map<Integer, Chat> activeChats) {
        Platform.runLater(() -> {

            var liveController = (LiveController) this.mDocuments.get(Path.LIVE).controller;

            liveController.mTabChats.getTabs().clear();

            activeChats.forEach((id, chat) -> {
                var chatTab = this.loadFXML(Path.CHAT_TAB, id);
                var chatTabController = (ChatTabController) chatTab.controller;
                Tab tab = new Tab(chat.name);
                tab.setContent(chatTab.root);
                liveController.mTabChats.getTabs().add(tab);

                chatTabController.mId = id;

                chat.messages.forEach(message -> {
                    var chatItem = this.loadFXML(Path.CHAT_ITEM);
                    var chatItemController = (ChatItemController) chatItem.controller;
                    chatItemController.mMessage.setText(message.username + ": " + message.message);
                    chatTabController.mMessageList.getChildren().add(chatItem.root);
                });
            });
        });
    }

    /**
     * Render overview scene with empty lists, but with filled search fields.
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
     * Render the overview scene's games list
     */
    public void renderGamesList(List<Game> activeGames, List<GameInvite> gameInvites) {
        Platform.runLater(() -> {

            var overview = (OverviewController) this.getController(Path.OVERVIEW);
            overview.mListGames.getChildren().clear();

            activeGames.forEach(game -> {
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController) item.controller;
                var playerCount = game.playerId.size();

                itemController.mType = ListItemType.GAME;
                itemController.mOverview = overview;
                itemController.init(ListItemType.GAME, game.id, overview, game.name + " [" + playerCount + "/4]");
                overview.mListGames.getChildren().add(item.root);
            });

            gameInvites.forEach(gameInvite -> {
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController) item.controller;

                itemController.init(ListItemType.GAME_INVITE, gameInvite.gameId, overview, gameInvite.gameName + " [invite]"); // TODO i18n
                overview.mListGames.getChildren().add(item.root);
            });
        });
    }

    /**
     * Render the overview scene's chats list
     */
    public void renderChatsList(List<Chat> activeChats, List<ChatInvite> chatInvites) {
        Platform.runLater(() -> {

            var overview = (OverviewController) this.getController(Path.OVERVIEW);

            overview.mListChats.getChildren().clear();

            activeChats.forEach(chat -> {
                var id = chat.id;
                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController) item.controller;

                var name = chat.name;
                var participantCount = chat.participants.size();

                itemController.init(ListItemType.CHAT, id, overview, name + " [" + participantCount + " people]"); // TODO
                                                                                                                   // i18n
                overview.mListChats.getChildren().add(item.root);
            });

            chatInvites.forEach(chatInvite -> {

                var item = this.loadFXML(Path.LIST_ITEM);
                var itemController = (ListItemController) item.controller;

                var name = chatInvite.chatName;

                itemController.init(ListItemType.CHAT_INVITE, chatInvite.chatId, overview, name + " [invite]");// TODO i18n
                overview.mListChats.getChildren().add(item.root);
            });
        });
    }

    /**
     * Render the overview scene's friends list
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
                    overview.mListFriends.getChildren().add(item.root);
                    break;
                }
            });
        });
    }

    /**
     * Render the overview scene's users list
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

    public void toastInfo(String message) {
        Platform.runLater(() -> {
            TextNotification notification = mFactory.buildTextNotification("Info",message); // TODO i18n
            notification.setCloseOnClick(true);
            mPlain.addNotification(notification, Time.seconds(2));
        });
    }

    public void toastError(String message) {
        Platform.runLater(() -> {
            TextNotification notification = mFactory.buildTextNotification("Error", message); // TODO i18n
            notification.setCloseOnClick(true);
            mPlain.addNotification(notification, Time.seconds(2));
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
