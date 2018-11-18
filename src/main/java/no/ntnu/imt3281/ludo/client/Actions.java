package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.api.FieldNames;
import no.ntnu.imt3281.ludo.api.FriendStatus;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;
import no.ntnu.imt3281.ludo.gui.Transitions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
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

        var item = new JSONObject();
        item.put(FieldNames.EMAIL, email);
        item.put(FieldNames.PASSWORD, password);

        mAPI.send(mRequests.make(LOGIN_REQUEST, item, state.authToken,
                (req, success) -> {
                    Logger.log(Level.DEBUG, "Action -> LoginSuccess: " + success.toString());

                    mStateManager.commit(gState -> {
                        // @DUMMY
                        gState.authToken = "afaa254";
                        // @DUMMY
                        gState.userId = 2;
                    });

                    mTransitions.renderLive();
                },
                (req, error) -> Logger.log(Level.WARN, "Action -> LoginError: " + error.toString())));
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
                (req, success) -> {
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
                (req, error) -> Logger.log(Level.WARN, "Action -> CreateUserError")));

    }

    /**
     * Logout the user with user_id stored in state.
     */
    public void logout() {
        var state = this.startAction("logout");

        var item = new JSONObject();
        item.put(FieldNames.USER_ID, state.userId);

        mAPI.send(mRequests.make(LOGOUT_REQUEST, item, state.authToken,
                (req, success) -> {
                    Logger.log(Level.DEBUG, "Action -> LogoutSucces");
                    mStateManager.commit(gState -> {
                        gState.userId = -1;
                        gState.email = "";
                        gState.authToken = "";
                        gState.username = "";
                    });
                    mTransitions.renderLogin();
                },
                (req, error) -> Logger.log(Level.WARN, "Action -> LogoutError")));
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
                (req, success) -> {
                    Logger.log(Level.DEBUG, "Request -> GET_GAME_RANGE_REQUEST Success");

                    // TODO
                    var gamelist = new HashMap<Integer, JSONObject>();

                    mStateManager.commit(gState -> {
                        gState.gamelist.putAll(gamelist);
                    });

                    mTransitions.renderGameList();
                },
                (req, error) -> Logger.log(Level.WARN, "Request -> GET_GAME_RANGE_REQUEST failed")));


        final var payload3 = new JSONObject();
        payload3.put("page_index", 0);

        mAPI.send(mRequests.make(GET_CHAT_RANGE_REQUEST, payload3, state.authToken,
                (req, success) -> {
                    Logger.log(Level.DEBUG, "Request -> GET_CHAT_RANGE_REQUEST Success");

                    // TODO
                    var chatlist = new HashMap<Integer,JSONObject>();

                    mStateManager.commit(gState -> {
                        gState.chatlist.putAll(chatlist);
                    });
                    mTransitions.renderChatList();
                },
                (req, error) -> Logger.log(Level.WARN, "Request -> GET_CHAT_RANGE_REQUEST failed")));


        final var payload4 = new JSONObject();
        payload4.put("page_index", 0);

        mAPI.send(mRequests.make(GET_FRIEND_RANGE_REQUEST, payload4, state.authToken,
                (req, success) -> {
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
                (req, error) -> Logger.log(Level.WARN, "Request -> GET_FRIEND_RANGE_REQUEST failed")));


        final var payload2 = new JSONObject();
        payload2.put("page_index", 0);

        mAPI.send(mRequests.make(GET_USER_RANGE_REQUEST, payload2, state.authToken,
                (req, success) -> {
                    Logger.log(Level.DEBUG, "Request -> GET_USER_RANGE_REQUEST Success");

                    // TODO
                    var userlist = new HashMap<Integer, JSONObject>();

                    // @DUMMY
                    var user = new JSONObject();
                    user.put(FieldNames.USER_ID, 0);
                    user.put(FieldNames.USERNAME, "Nalle Bordvik");
                    userlist.put(0,user);

                    // @DUMMY
                    var user2 = new JSONObject();
                    user2.put(FieldNames.USER_ID, 1);
                    user2.put(FieldNames.USERNAME, "Jonas Solsvik");
                    userlist.put(1,user2);

                    mStateManager.commit(gState -> {
                        gState.userlist.putAll(userlist);
                    });
                    mTransitions.renderUserList();
                },
                (req, error) -> Logger.log(Level.WARN, "Request -> GET_USER_RANGE_REQUEST failed")));
    }

    /**
     *
     */
    public void gotoUser() {
        mTransitions.renderUser();
    }

    /**
     * Create game and add as active
     */
    public void createGame(/* TODO pass name*/) {
        var state = this.startAction("createGame");

        var newGameId = randomId();
        var payload = new JSONObject();
        payload.put(FieldNames.USER_ID, state.userId);
        payload.put(FieldNames.NAME, "Game "+newGameId);

        mAPI.send(mRequests.make(CREATE_GAME_REQUEST, payload, state.authToken,
                (req, success) -> {
                    // TODO
                    var game = new JSONObject();
                    game.put(FieldNames.NAME, "Game " + newGameId);
                    game.put(FieldNames.PLAYER_ID, new JSONArray(new int[]{}));
                    mStateManager.commit(gState -> {
                        gState.gamelist.put(newGameId, game);
                        gState.activeGames.add(newGameId);
                    });

                    mTransitions.newGame(newGameId);
                },
                (req, error) -> Logger.log(Level.WARN, "Request -> CREATE_GAME_REQUEST failed")));
    }


    /**
     * Create chat and add as active
     */
    public void createChat(/* TODO pass name*/) {
        var state = this.startAction("createChat");
        
        var newChatId = randomId();
        var payload = new JSONObject();
        payload.put(FieldNames.USER_ID, state.userId);
        payload.put(FieldNames.NAME, "Chat " + newChatId);

        mAPI.send(mRequests.make(CREATE_CHAT_REQUEST, payload, state.authToken,
                (req, success) -> {
                    // TODO
                    var chat = new JSONObject();
                    chat.put(FieldNames.NAME, "Chat "+newChatId);
                    chat.put(FieldNames.PARTICIPANT_ID, new JSONArray(new int[]{}));

                    mStateManager.commit(gState -> {
                        gState.chatlist.put(newChatId, chat);
                        gState.activeChats.add(newChatId);
                    });
                    mTransitions.newChat(newChatId);
                },
                (req, error) -> Logger.log(Level.WARN, "Request -> CREATE_CHAT_REQUEST failed")));
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
                (req, success) -> {

                },
                (req, error) -> Logger.log(Level.WARN, "Request -> FRIEND_REQUEST failed")));
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
                (req, success) -> {

                },
                (req, error) -> Logger.log(Level.WARN, "Request -> UNFRIEND_REQUEST failed")));
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
                (req, success) -> {

                },
                (req, error) -> Logger.log(Level.WARN, "Request -> IGNORE_REQUEST failed")));
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
                (req, success) -> {

                },
                (req, error) -> Logger.log(Level.WARN, "Request -> UNIGNORE_REQUEST failed")));
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
                (req, success) -> {

                },
                (req, error) -> Logger.log(Level.WARN, "Request -> JOIN_CHAT_REQUEST failed")));
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
                (req, success) -> {

                },
                (req, error) -> Logger.log(Level.WARN, "Request -> LEAVE_CHAT_REQUEST failed")));
    }

    /**
     *
     */
    public void sendChatMessage() {
        var state = this.startAction("sendChatMessage");
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
        (req, success) -> {

        },
        (req, error) -> Logger.log(Level.WARN, "Request -> CHAT_INVITE_REQUEST failed")));
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
        (req, success) -> {

        },
        (req, error) -> Logger.log(Level.WARN, "Request -> JOIN_GAME_REQUEST failed")));
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
        (req, success) -> {

        },
        (req, error) -> Logger.log(Level.WARN, "Request -> LEAVE_GAME failed")));
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
        (req, success) -> {

        },
        (req, error) -> Logger.log(Level.WARN, "Request -> GAME_INVITE_REQUEST failed")));
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
        (req, success) -> {

        },
        (req, error) -> Logger.log(Level.WARN, "Request -> DECLINE_GAME_REQUEST failed")));
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


    /**
     *
     */
    public void updateUser() {
        var state = this.startAction("updateUser");
    }

    /**
     *
     */
    public void deleteUser() {
        var state = this.startAction("deleteUser");
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
    public void getUser() {
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
     * Do prep-work and log action.
     *
     * @param methodName name of callee
     */
    private State startAction(String methodName) {
        Logger.log(Level.INFO, "Action -> " + methodName);
        return mStateManager.copy();
    }

    private static int randomId() {
        return ThreadLocalRandom.current().nextInt(100000, 10000000);
    }
}