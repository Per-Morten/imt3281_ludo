package no.ntnu.imt3281.ludo.client;

import javafx.scene.image.Image;
import no.ntnu.imt3281.ludo.api.FieldNames;
import no.ntnu.imt3281.ludo.api.FriendStatus;
import no.ntnu.imt3281.ludo.api.RequestType;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;
import no.ntnu.imt3281.ludo.gui.Transitions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

import static no.ntnu.imt3281.ludo.api.RequestType.*;

public class Actions {

    private Transitions mTransitions;
    private API mAPI;
    private StateManager mStateManager;
    private final RequestFactory mRequests = new RequestFactory();

    /**
     * Bind dependencies of Actions
     *
     * @param transitions to feed mutations
     * @param API         to push requests
     */
    void bind(Transitions transitions, API API, StateManager stateManager) {
        mTransitions = transitions;
        mStateManager = stateManager;
        mAPI = API;
    }

    /**
     * login user with username and password
     *
     * @param email    valid email
     * @param password valid password
     */
    public void login(String email, String password) {
        var state = this.startAction("login");

        var payload = new JSONObject();
        payload.put(FieldNames.EMAIL, email);
        payload.put(FieldNames.PASSWORD, password);

        mAPI.send(mRequests.make(LOGIN_REQUEST, payload, state.authToken,
            success -> {
                Logger.log(Level.DEBUG, "Action -> LoginSuccess: " + success.toString());

                var payload2 = new JSONObject();
                payload.put(FieldNames.USER_ID, 1337);

                mAPI.send(mRequests.make(GET_USER_REQUEST, payload2, state.authToken,
                    success2 -> {

                        var user = new User();
                        var userJson = new JSONObject();
                        userJson.put(FieldNames.USERNAME, "Jonas");
                        userJson.put(FieldNames.EMAIL, "jonas@gmail.com");
                        userJson.put(FieldNames.PASSWORD, "1234");
                        userJson.put(FieldNames.AVATAR_URI, "https://pickaface.net/gallery/avatar/unr_test_161024_0535_9lih90.png");
                        user.json = userJson;
                        user.avatar = new Image("https://pickaface.net/gallery/avatar/unr_test_161024_0535_9lih90.png");

                        mStateManager.commit(gState -> {
                            gState.userlist.put(1337, user);
                            gState.authToken = "afaa254";
                            gState.userId = 1337;
                        });

                        mTransitions.renderUser();
                    },
                    error -> this.logError(GET_USER_REQUEST, error)));
            },
            error -> this.logError(LOGIN_REQUEST ,error)));
    }

    /**
     * login user with username and password
     *
     * @param email    valid email
     * @param username valid username
     * @param password valid password
     */
    public void createUser(String email, String password, String username) {
        var state = this.startAction("createUser");

        mTransitions.renderLogin();

        var item = new JSONObject();
        item.put(FieldNames.EMAIL, email);
        item.put(FieldNames.PASSWORD, password);
        item.put(FieldNames.USERNAME, username);

        mAPI.send(mRequests.make(CREATE_USER_REQUEST, item, state.authToken,
            success -> {
                Logger.log(Level.DEBUG, "Action -> CreateUserSuccess");
                mStateManager.commit(gState -> {
                    // @DUMMY
                    gState.userId = 2;
                    // @DUMMY
                    gState.email = "jonas.solsvik@gmail.com";
                    // @DUMMY
                    gState.username = "jonasjso";
                });

                mTransitions.renderLogin();
            },
            error -> this.logError(CREATE_USER_REQUEST, error)));

    }

    /**
     * Logout the user with user_id stored in state.
     */
    public void logout() {
        var state = this.startAction("logout");

        var item = new JSONObject();
        item.put(FieldNames.USER_ID, state.userId);

        mAPI.send(mRequests.make(LOGOUT_REQUEST, item, state.authToken,
            success -> {},
            error -> this.logError(LOGOUT_REQUEST, error)));

        mStateManager.commit(gState -> {
            gState.userId = -1;
            gState.email = "";
            gState.authToken = "";
            gState.username = "";
            gState.gamelist.clear();
            gState.chatlist.clear();
            gState.activeChats.clear();
            gState.activeGames.clear();
        });
        mTransitions.renderLogin();
    }

    /**
     *
     */
    public void updateUser(String username, String email, String password, String avatarURI) {
        var state = this.startAction("updateUser");

        var payload = new JSONObject();
        payload.put(FieldNames.USERNAME, username);
        payload.put(FieldNames.EMAIL, email);
        payload.put(FieldNames.PASSWORD, password);
        payload.put(FieldNames.AVATAR_URI, avatarURI);

        mAPI.send(mRequests.make(UPDATE_USER_REQUEST, payload, state.authToken, success -> {

            mStateManager.commit(gState -> {
                gState.username = username;
                gState.email = email;
                gState.avatarURI = avatarURI;
            });

            mTransitions.renderUser();

        }, error -> this.logError(UPDATE_USER_REQUEST, error)));
    }

    /**
     *
     */
    public void deleteUser() {
        var state = this.startAction("deleteUser");

        var payload = new JSONObject();
        payload.put(FieldNames.USER_ID, state.userId);

        mAPI.send(mRequests.make(DELETE_USER_REQUEST, payload, state.authToken, success -> {

            this.logout();

        }, error -> this.logError(DELETE_USER_REQUEST, error)));
    }

    /**
     *
     */
    public void gotoUser() {
        mTransitions.renderUser();
    }

    /**
     *
     */
    public void gotoLive() {
        mTransitions.renderLive();
        mTransitions.renderGameTabs();
        mTransitions.renderChatTabs();
    }

    /**
     *
     */
    public void gotoOverview() {
        var state = this.startAction("gotoOverview");

        mTransitions.renderOverview();

        final var payload = new JSONObject();
        payload.put("page_index", 0);

        mAPI.send(mRequests.make(GET_GAME_RANGE_REQUEST, payload, state.authToken,
            success -> {
                Logger.log(Level.DEBUG, "Request -> GET_GAME_RANGE_REQUEST Success");

                // TODO
                var gamelist = new HashMap<Integer, JSONObject>();

                mStateManager.commit(gState -> {
                    gState.gamelist.putAll(gamelist);
                });

                mTransitions.renderGameList();
            },
            error -> this.logError(GET_GAME_RANGE_REQUEST, error)));


        final var payload3 = new JSONObject();
        payload3.put("page_index", 0);

        mAPI.send(mRequests.make(GET_CHAT_RANGE_REQUEST, payload3, state.authToken,
            success -> {
                Logger.log(Level.DEBUG, "Request -> GET_CHAT_RANGE_REQUEST Success");

                // TODO
                var chatlist = new HashMap<Integer,Chat>();

                mStateManager.commit(gState -> {
                    gState.chatlist.putAll(chatlist);
                });
                mTransitions.renderChatList();
            },
            error -> this.logError(GET_CHAT_RANGE_REQUEST , error)));


        final var payload4 = new JSONObject();
        payload4.put("page_index", 0);

        mAPI.send(mRequests.make(GET_FRIEND_RANGE_REQUEST, payload4, state.authToken,
            success -> {
                Logger.log(Level.DEBUG, "Request -> GET_FRIEND_RANGE_REQUEST Success");

                var friendlist = new HashMap<Integer, JSONObject>();

                // TODO
                var friend = new JSONObject();
                friend.put(FieldNames.USER_ID, 0);
                friend.put(FieldNames.USERNAME, "Patrick Patriot");
                friend.put(FieldNames.STATUS, FriendStatus.FRIENDED.toInt());
                friendlist.put(0,friend);

                var friend2 = new JSONObject();
                friend2.put(FieldNames.USER_ID, 1);
                friend2.put(FieldNames.USERNAME, "Senna Sanoi");
                friend2.put(FieldNames.STATUS, FriendStatus.PENDING.toInt());
                friendlist.put(1,friend2);

                var friend3 = new JSONObject();
                friend3.put(FieldNames.USER_ID, 1);
                friend3.put(FieldNames.USERNAME, "Ignore-me Ignored");
                friend3.put(FieldNames.STATUS, FriendStatus.IGNORED.toInt());
                friendlist.put(2,friend3);

                mStateManager.commit(gState -> {
                    gState.friendlist.putAll(friendlist);
                });
                mTransitions.renderFriendList();
            },
            error -> this.logError(GET_FRIEND_RANGE_REQUEST , error)));


        final var payload2 = new JSONObject();
        payload2.put("page_index", 0);

        mAPI.send(mRequests.make(GET_USER_RANGE_REQUEST, payload2, state.authToken,
            success -> {
                Logger.log(Level.DEBUG, "Request -> GET_USER_RANGE_REQUEST Success");

                // TODO
                var userlist = new HashMap<Integer, User>();

                // @DUMMY
                var userJson = new JSONObject();
                userJson.put(FieldNames.USER_ID, 0);
                userJson.put(FieldNames.USERNAME, "Nalle Bordvik");
                var user = new User();
                user.json = userJson;

                // @DUMMY
                var user2json = new JSONObject();
                user2json.put(FieldNames.USER_ID, 1);
                user2json.put(FieldNames.USERNAME, "Jonas Solsvik");
                var user2 = new User();
                user2.json = user2json;

                userlist.put(0,user);
                userlist.put(1,user2);

                mStateManager.commit(gState -> {
                    gState.userlist.putAll(userlist);
                });
                mTransitions.renderUserList();
            },
            error -> this.logError(GET_USER_RANGE_REQUEST, error)));
    }


    /**
     * Create game and add as active
     */
    public void createGame(/* TODO pass name*/) {
        var state = this.startAction("createGame");

        var newGameId = randomId();
        var payload = new JSONObject();
        payload.put(FieldNames.USER_ID, state.userId);
        payload.put(FieldNames.NAME, "Game " + newGameId);

        mAPI.send(mRequests.make(CREATE_GAME_REQUEST, payload, state.authToken,
                success -> {
                    // TODO
                    var game = makeGame();
                    mStateManager.commit(gState -> {
                        gState.gamelist.put(newGameId, game);
                        gState.activeGames.add(newGameId);
                    });

                    mTransitions.newGame(newGameId);
                },
                error -> this.logError(CREATE_GAME_REQUEST, error)));
    }


    /**
     * Create chat and add as active
     */
    public void createChat(/* TODO pass name*/) {
        var state = this.startAction("createChat");

        var payload = new JSONObject();
        var chat = makeChat();
        var chatId = chat.json.getInt(FieldNames.CHAT_ID);
        payload.put(FieldNames.USER_ID, state.userId);
        payload.put(FieldNames.NAME, "Chat " + chatId);

        mAPI.send(mRequests.make(CREATE_CHAT_REQUEST, payload, state.authToken,
            success -> {
                // TODO

                mStateManager.commit(gState -> {
                    gState.chatlist.put(chatId, chat);
                    gState.activeChats.add(chatId);
                });
                mTransitions.newChat(chatId);
            },
            error -> this.logError(CREATE_CHAT_REQUEST , error)));
    }


    /**
     *
     */
    public void friend(HashSet<Integer> friendsId) {
        var state = this.startAction("friend");

        var payload = new ArrayList<JSONObject>();
        friendsId.forEach(friendId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, state.userId);
            item.put(FieldNames.OTHER_ID, friendId);
            payload.add(item);
        });

        mAPI.send(mRequests.make(FRIEND_REQUEST, payload, state.authToken,
            success -> {

            },
            error -> this.logError(FRIEND_REQUEST, error)));
    }

    /**
     *
     */
    public void unfriend(HashSet<Integer> friendsId) {
        var state = this.startAction("unfriend");

        var payload = new ArrayList<JSONObject>();
        friendsId.forEach(friendId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, state.userId);
            item.put(FieldNames.OTHER_ID, friendId);
            payload.add(item);
        });

        mAPI.send(mRequests.make(UNFRIEND_REQUEST, payload, state.authToken,
            success -> {

            },
            error -> this.logError(UNFRIEND_REQUEST , error)));
    }

    /**
     *
     */
    public void ignore(HashSet<Integer> usersId) {
        var state = this.startAction("ignore");

        var payload = new ArrayList<JSONObject>();
        usersId.forEach(userId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, state.userId);
            item.put(FieldNames.OTHER_ID, userId);
            payload.add(item);
        });

        mAPI.send(mRequests.make(IGNORE_REQUEST, payload, state.authToken,
            success -> {

            },
            error -> this.logError(IGNORE_REQUEST , error)));
    }

    /**
     *
     */
    public void unignore(HashSet<Integer> usersId) {
        var state = this.startAction("unignore");

        var payload = new ArrayList<JSONObject>();
        usersId.forEach(userId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, state.userId);
            item.put(FieldNames.OTHER_ID, userId);
            payload.add(item);
        });

        mAPI.send(mRequests.make(UNIGNORE_REQUEST, payload, state.authToken,
            success -> {

            },
            error -> this.logError(UNIGNORE_REQUEST , error)));
    }

    /**
     *
     */
    public void joinChat(HashSet<Integer> chatsId) {
        var state = this.startAction("joinChat");

        var payload = new ArrayList<JSONObject>();
        chatsId.forEach(chatId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, state.userId);
            item.put(FieldNames.CHAT_ID, chatId);
            payload.add(item);
        });

        mAPI.send(mRequests.make(JOIN_CHAT_REQUEST, payload, state.authToken,
            success -> {

            },
            error -> this.logError(JOIN_CHAT_REQUEST , error)));
    }

    /**
     *
     */
    public void leaveChat(HashSet<Integer> chatsId) {
        var state = this.startAction("leaveChat");

        var payload = new ArrayList<JSONObject>();
        chatsId.forEach(chatId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, state.userId);
            item.put(FieldNames.CHAT_ID, chatId);
            payload.add(item);
        });

        mAPI.send(mRequests.make(LEAVE_CHAT_REQUEST, payload, state.authToken,
            success -> {

            },
            error -> this.logError(LEAVE_CHAT_REQUEST , error)));
    }

    /**
     *
     */
    public void sendChatMessage(int chatId, String message) {
        var state = this.startAction("sendChatMessage");

        var payload = new JSONObject();
        payload.put(FieldNames.USER_ID, state.userId);
        payload.put(FieldNames.CHAT_ID, chatId);
        payload.put(FieldNames.MESSAGE, message);

        mAPI.send(mRequests.make(SEND_CHAT_MESSAGE_REQUEST, payload, state.authToken,
            success -> {
                var messageObj = new JSONObject();
                messageObj.put(FieldNames.USER_ID, state.userId);
                messageObj.put(FieldNames.MESSAGE, message);
                mTransitions.newMessage(chatId, state.userId, message);
            },
            error -> this.logError(SEND_CHAT_MESSAGE_REQUEST, error)));
    }

    /**
     *
     */
    public void sendChatInvite(HashSet<Integer> chatsId, HashSet<Integer> friendsId) {
        var state = this.startAction("sendChatInvite");

        var payload = new ArrayList<JSONObject>();
        chatsId.forEach(chatId -> {
            friendsId.forEach(friendId -> {
                var item = new JSONObject();
                item.put(FieldNames.USER_ID, state.userId);
                item.put(FieldNames.OTHER_ID, friendId);
                item.put(FieldNames.CHAT_ID, chatId);
                payload.add(item);
            });
        });

        mAPI.send(mRequests.make(SEND_CHAT_INVITE_REQUEST, payload, state.authToken,
        success -> {

        },
        error -> this.logError(SEND_CHAT_INVITE_REQUEST , error)));
    }

    /**
     *
     */
    public void joinGame(HashSet<Integer> gamesId) {
        var state = this.startAction("joinGame");

        var payload = new ArrayList<JSONObject>();
        gamesId.forEach(gameId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, state.userId);
            item.put(FieldNames.GAME_ID, gameId);
            payload.add(item);
        });

        mAPI.send(mRequests.make(JOIN_GAME_REQUEST, payload, state.authToken,
        success -> {

        },
        error -> this.logError(JOIN_GAME_REQUEST , error)));
    }

    /**
     *
     */
    public void leaveGame(HashSet<Integer> gamesId) {
        var state = this.startAction("leaveGame");

        var payload = new ArrayList<JSONObject>();
        gamesId.forEach(gameId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, state.userId);
            item.put(FieldNames.GAME_ID, gameId);
            payload.add(item);
        });

        mAPI.send(mRequests.make(LEAVE_GAME_REQUEST, payload, state.authToken,
        success -> {

        },
        error -> this.logError(LEAVE_GAME_REQUEST, error)));
    }

    /**
     *
     */
    public void sendGameInvite(HashSet<Integer> gamesId, HashSet<Integer> friendsId) {
        var state = this.startAction("sendGameInvite");

        var payload = new ArrayList<JSONObject>();
        gamesId.forEach(gameId -> {
            friendsId.forEach(friendId -> {
                var item = new JSONObject();
                item.put(FieldNames.USER_ID, state.userId);
                item.put(FieldNames.OTHER_ID, friendId);
                item.put(FieldNames.GAME_ID, gameId);
                payload.add(item);
            });
        });

        mAPI.send(mRequests.make(SEND_GAME_INVITE_REQUEST, payload, state.authToken,
        success -> {

        },
        error -> this.logError(SEND_GAME_INVITE_REQUEST, error)));
    }

    /**
     *
     */
    public void declineGameInvite(HashSet<Integer> gamesId) {
        var state = this.startAction("declineGameInvite");

        var payload = new ArrayList<JSONObject>();
        gamesId.forEach(gameId -> {
            var item = new JSONObject();
            item.put(FieldNames.GAME_ID, gameId);
            payload.add(item);
        });

        mAPI.send(mRequests.make(DECLINE_GAME_INVITE_REQUEST, payload, state.authToken,
        success -> {

        },
        error -> this.logError(DECLINE_GAME_INVITE_REQUEST, error)));
    }

    /**
     *
     */
    public void startGame() {
        var state = this.startAction("startGame");
    }

    /**
     *
     */
    public void sendRollDice() {
        var state = this.startAction("sendRollDice");
    }

    /**
     *
     */
    public void movePiece() {
        var state = this.startAction("movePiece");
    }





    // ------------------- GET REQUESTS -------------------

    /**
     *
     */
    public void getChat() {
        var state = this.startAction("login");
    }


    /**
     *
     */
    public void getUser(Collection<Integer> userIds) {

        var state = this.startAction("getUser");

    }
    /**
     *
     */
    public void getFriend() {
        var state = this.startAction("getFriend");
    }

    /**
     *
     */
    public void getGame() {
        var state = this.startAction("getGame");
    }

    /**
     *
     */
    public void getGameState() {
        var state = this.startAction("getGameState");
    }


    /**
     * Do prep-work for each action.
     *
     * @param methodName name of callee
     */
    private State startAction(String methodName) {
        Logger.log(Level.INFO, "Action -> " + methodName);
        return mStateManager.copy();
    }

    private void logError(RequestType type, JSONObject error) {

        // TODO Get error codes strings

        var errorString = "Response error -> " + RequestType.toLowerCaseString(type);
        Logger.log(Level.WARN, errorString);
        // TODO Enable in production
        //mTransitions.toastError(errorString.toString());
    }

    private static int randomId() {
        return ThreadLocalRandom.current().nextInt(1000000, 9999999);
    }

    private static JSONObject makeGame() {
        var gameId = randomId();
        var game = new JSONObject();
        game.put(FieldNames.GAME_ID, gameId);
        game.put(FieldNames.NAME, "Game " + gameId);
        game.put(FieldNames.PLAYER_ID, new JSONArray(new int[]{}));
        return game;
    }

    private static Chat makeChat() {
        var chatId = randomId();
        var chat = new Chat();
        chat.json.put(FieldNames.CHAT_ID, chatId);
        chat.json.put(FieldNames.NAME, "Chat " + chatId);
        chat.json.put(FieldNames.PARTICIPANT_ID, new JSONArray(new int[]{}));
        return chat;
    }
}