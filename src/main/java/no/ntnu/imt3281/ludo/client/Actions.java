package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.api.FieldNames;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;
import no.ntnu.imt3281.ludo.gui.Transitions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static no.ntnu.imt3281.ludo.api.RequestType.*;

public class Actions {

    private Transitions mTransitions;
    private API mAPI;
    private StateManager mStateManager;
    private final RequestFactory mRequestFactory = new RequestFactory();

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

        Request request = mRequestFactory.make(LOGIN_REQUEST, item, state.authToken,
                (req, success) -> {
                    Logger.log(Level.DEBUG, "Action -> LoginSuccess: " + success.toString());
                },
                (req, error) -> Logger.log(Level.WARN, "Action -> LoginError: " + error.toString()));

        mAPI.send(request);

        mStateManager.commit(gState -> {
            // @DUMMY
            gState.authToken = "afaa254";
            // @DUMMY
            gState.userId = 2;
        });

        mTransitions.renderLive();
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

        var item = new JSONObject();
        item.put(FieldNames.EMAIL, email);
        item.put(FieldNames.PASSWORD, password);
        item.put(FieldNames.USERNAME, username);

        Request request = mRequestFactory.make(CREATE_USER_REQUEST, item, state.authToken,
                (req, success) -> {
                    Logger.log(Level.DEBUG, "Action -> CreateUserSuccess");

                },
                (req, error) -> Logger.log(Level.WARN, "Action -> CreateUserError"));

        mAPI.send(request);

        mStateManager.commit(gState -> {
            // @DUMMY
            gState.userId = 2;
            // @DUMMY
            gState.email = "jonas.solsvik@gmail.com";
            // @DUMMY
            gState.username = "jonasjso";
        });
        mTransitions.renderLogin();
    }

    /**
     * Logout the user with user_id stored in state.
     */
    public void logout() {
        var state = this.startAction("logout");

        var item = new JSONObject();
        item.put(FieldNames.USER_ID, state.userId);

        Request request = mRequestFactory.make(LOGOUT_REQUEST, item, state.authToken,
                (req, success) -> {
                    Logger.log(Level.DEBUG, "Action -> LogoutSucces");

                },
                (req, error) -> Logger.log(Level.WARN, "Action -> LogoutError"));

        mAPI.send(request);
        mStateManager.commit(gState -> {
            gState.userId = -1;
            gState.email = "";
            gState.authToken = "";
            gState.username = "";
        });
        mTransitions.renderLogin();
    }

    /**
     *
     */
    public void gotoLive() {
        mTransitions.renderLive();
    }

    /**
     *
     */
    public void gotoSearch() {
        var state = this.startAction("gotoSearch");

        final var item = new JSONObject();
        item.put("page_index", 0);
        Request requestGameRange = mRequestFactory.make(GET_GAME_RANGE_REQUEST, item, state.authToken,
                (req, success) -> {
                    Logger.log(Level.DEBUG, "Request -> GET_GAME_RANGE_REQUEST Success");

                    var gameRange = new ArrayList<JSONObject>();

                    // @DUMMY
                    var game = new JSONObject();
                    game.put("game_id", 0);

                    game.put("player_id", new JSONArray(new int[]{0,1,2,3}));
                    game.put(FieldNames.NAME, "Ludder Fight");
                    gameRange.add(game);

                    // @DUMMY
                    var game2 = new JSONObject();
                    game2.put("game_id", 1);
                    game2.put("player_id", new JSONArray(new int[]{0}));
                    game2.put(FieldNames.NAME, "Ludo Capital");
                    gameRange.add(game2);

                    // @DUMMY
                    var game3 = new JSONObject();
                    game3.put("game_id", 2);
                    game3.put("player_id", new JSONArray(new int[]{}));
                    game3.put(FieldNames.NAME, "Showdown of ages");
                    gameRange.add(game3);


                    Logger.log(Level.DEBUG, "" + game.toString() + " " + game2.toString());

                    mStateManager.commit(gState -> {
                        gState.gameRange.clear();
                        gState.gameRange.addAll(gameRange);
                    });

                    mTransitions.renderGameList();
                },
                (req, error) -> Logger.log(Level.WARN, "Request -> GET_GAME_RANGE_REQUEST failed"));


        final var item3 = new JSONObject();
        item.put("page_index", 0);
        Request requestChatRange = mRequestFactory.make(GET_CHAT_RANGE_REQUEST, item, state.authToken,
                (req, success) -> {
                    Logger.log(Level.DEBUG, "Request -> GET_CHAT_RANGE_REQUEST Success");

                    var chatRange = new ArrayList<JSONObject>();

                    // @DUMMY
                    var chat = new JSONObject();
                    chat .put(FieldNames.CHAT_ID, 0);
                    chat.put(FieldNames.NAME, "DarkRoom");
                    chat.put(FieldNames.PARTICIPANT_ID, new JSONArray(new int[]{0,2}));
                    chatRange.add(chat);

                    // @DUMMY
                    var chat2 = new JSONObject();
                    chat2 .put(FieldNames.CHAT_ID, 1);
                    chat2.put(FieldNames.NAME, "WastedWastes");
                    chat2.put(FieldNames.PARTICIPANT_ID, new JSONArray(new int[]{0,2,4,5,6,7,8}));
                    chatRange.add(chat2);

                    mStateManager.commit(gState -> {
                        gState.chatRange.clear();
                        gState.chatRange.addAll(chatRange);
                    });
                    mTransitions.renderChatList();
                },
                (req, error) -> Logger.log(Level.WARN, "Request -> GET_CHAT_RANGE_REQUEST failed"));


        final var item4 = new JSONObject();
        item.put("page_index", 0);
        Request requestFriendRange = mRequestFactory.make(GET_FRIEND_RANGE_REQUEST, item, state.authToken,
                (req, success) -> {
                    Logger.log(Level.DEBUG, "Request -> GET_FRIEND_RANGE_REQUEST Success");

                    var friendRange = new ArrayList<JSONObject>();

                    // @DUMMY
                    var friend = new JSONObject();
                    friend.put(FieldNames.USER_ID, 0);
                    friend.put(FieldNames.USERNAME, "Patrick Patriot");
                    friendRange.add(friend);

                    // @DUMMY
                    var friend2 = new JSONObject();
                    friend2.put(FieldNames.USER_ID, 1);
                    friend2.put(FieldNames.USERNAME, "Senna Sanoi");
                    friendRange.add(friend2);

                    // @DUMMY
                    var friend3 = new JSONObject();
                    friend3.put(FieldNames.USER_ID, 2);
                    friend3.put(FieldNames.USERNAME, "Mah Man");
                    friendRange.add(friend3);

                    // @DUMMY
                    var friend4 = new JSONObject();
                    friend4.put(FieldNames.USER_ID, 3);
                    friend4.put(FieldNames.USERNAME, "Alla Saman");
                    friendRange.add(friend4);

                    mStateManager.commit(gState -> {
                        gState.friendRange.clear();
                        gState.friendRange.addAll(friendRange);
                    });
                    mTransitions.renderFriendList();
                },
                (req, error) -> Logger.log(Level.WARN, "Request -> GET_FRIEND_RANGE_REQUEST failed"));


        final var item2 = new JSONObject();
        item.put("page_index", 0);
        Request requestUserRange = mRequestFactory.make(GET_USER_RANGE_REQUEST, item, state.authToken,
                (req, success) -> {
                    Logger.log(Level.DEBUG, "Request -> GET_USER_RANGE_REQUEST Success");

                    var userRange = new ArrayList<JSONObject>();

                    // @DUMMY
                    var user = new JSONObject();
                    user.put(FieldNames.USER_ID, 0);
                    user.put(FieldNames.USERNAME, "Nalle Bordvik");
                    userRange .add(user);

                    // @DUMMY
                    var user2 = new JSONObject();
                    user2.put(FieldNames.USER_ID, 1);
                    user2.put(FieldNames.USERNAME, "Jonas Solsvik");
                    userRange.add(user2);

                    mStateManager.commit(gState -> {
                        gState.userRange.clear();
                        gState.userRange.addAll(userRange);
                    });
                    mTransitions.renderUserList();
                },
                (req, error) -> Logger.log(Level.WARN, "Request -> GET_USER_RANGE_REQUEST failed"));

        mTransitions.renderOverview();

        mAPI.send(requestGameRange);
        mAPI.send(requestChatRange);
        mAPI.send(requestUserRange);
        mAPI.send(requestFriendRange);
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
    public void createGame() {
        var state = this.startAction("createGame");

        mStateManager.commit(gState -> {
            var game = new JSONObject();
            gState.activeGames.put(state.activeGames.size(),game);
        });

        mTransitions.renderGameTabs();
    }

    /**
     *
     */
    public void createChat() {
        var state = this.startAction("createChat");

        mStateManager.commit(gState -> {
            var chat = new JSONObject();
            gState.activeChats.put(state.activeChats.size(), chat);
        });
        mTransitions.renderChatTabs();
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
    public void updateUser() {
        var state = this.startAction("updateUser");
    }

    /**
     *
     */
    public void deleteUser() {
        var state = this.startAction("deleteUser");
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
    public void friend() {
        var state = this.startAction("friend");
    }

    /**
     *
     */
    public void unfriend() {
        var state = this.startAction("unfriend");
    }

    /**
     *
     */
    public void joinChat() {
        var state = this.startAction("joinChat");
    }

    /**
     *
     */
    public void leaveChat() {
        var state = this.startAction("leaveChat");
    }

    /**
     *
     */
    public void getChat() {
        var state = this.startAction("login");
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
    public void sendChatInvite() {
        var state = this.startAction("sendChatInvite");
    }


    /**
     *
     */
    public void joinGame() {
        var state = this.startAction("joinGame");
    }

    /**
     *
     */
    public void leaveGame() {
        var state = this.startAction("leaveGame");
    }

    /**
     *
     */
    public void sendGameInvite() {
        var state = this.startAction("sendGameInvite");
    }

    /**
     *
     */
    public void declineGameInvite() {
        var state = this.startAction("declineGameInvite");
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
     * Do prep-work and log action.
     *
     * @param methodName name of callee
     */
    private State startAction(String methodName) {
        Logger.log(Level.INFO, "Action -> " + methodName);
        return mStateManager.copy();
    }
}