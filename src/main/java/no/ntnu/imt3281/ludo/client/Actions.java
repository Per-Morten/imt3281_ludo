package no.ntnu.imt3281.ludo.client;

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
    private CacheManager mCacheManager;
    private String mAuth;
    private Cache mLocalCache = new Cache();
    private final RequestFactory mRequestFactory = new RequestFactory();

    /**
     * Bind dependencies of Actions
     *
     * @param transitions to feed mutations
     * @param API         to push requests
     */
    void bind(Transitions transitions, API API, CacheManager cacheManager) {
        mTransitions = transitions;
        mCacheManager = cacheManager;
        mAPI = API;
    }

    /**
     * login user with username and password
     *
     * @param email    valid email
     * @param password valid password
     */
    public void login(String email, String password) {
        this.startAction("login");

        var item = new JSONObject();
        item.put("email", email);
        item.put("password", password);

        Request request = mRequestFactory.make(LOGIN_REQUEST, item, mAuth,
                (req, success) -> {
                    Logger.log(Level.DEBUG, "Action -> LoginSuccess: " + success.toString());
                },
                (req, error) -> Logger.log(Level.WARN, "Action -> LoginError: " + error.toString()));

        mAPI.send(request);

        mCacheManager.commit(cache -> {
            // @DUMMY
            cache.authToken = "afaa254";
            // @DUMMY
            cache.userId = 2;
            // @DUMMY
            cache.gameId.clear();
            cache.gameId.add(2);
            cache.gameId.add(3);
            // @DUMMY
            cache.clientId.clear();
            cache.clientId.add(2);
            cache.clientId.add(3);
            // @DUMMY
            cache.friendId.clear();
            cache.friendId.add(2);
            cache.friendId.add(3);

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
        this.startAction("createUser");

        var item = new JSONObject();
        item.put("email", email);
        item.put("password", password);
        item.put("username", username);

        Request request = mRequestFactory.make(CREATE_USER_REQUEST, item, mAuth,
                (req, success) -> {
                    Logger.log(Level.DEBUG, "Action -> CreateUserSuccess");

                },
                (req, error) -> Logger.log(Level.WARN, "Action -> CreateUserError"));

        mAPI.send(request);

        mCacheManager.commit(cache -> {
            // @DUMMY
            cache.userId = 2;
            // @DUMMY
            cache.email = "jonas.solsvik@gmail.com";
            // @DUMMY
            cache.username = "jonasjso";
        });
        mTransitions.renderLogin();
    }

    /**
     * Logout the user with user_id stored in cache.
     */
    public void logout() {
        this.startAction("logout");

        var item = new JSONObject();
        item.put("user_id", mLocalCache.userId);

        Request request = mRequestFactory.make(LOGOUT_REQUEST, item, mAuth,
                (req, success) -> {
                    Logger.log(Level.DEBUG, "Action -> LogoutSucces");

                },
                (req, error) -> Logger.log(Level.WARN, "Action -> LogoutError"));

        mAPI.send(request);
        mCacheManager.commit(cache -> {
            cache.userId = -1;
            cache.email = "";
            cache.authToken = "";
            cache.username = "";
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
        this.startAction("gotoSearch");

        final var item = new JSONObject();
        item.put("page_index", 0);
        Request requestGameRange = mRequestFactory.make(GET_GAME_RANGE_REQUEST, item, mAuth,
                (req, success) -> {
                    Logger.log(Level.DEBUG, "Request -> GET_GAME_RANGE_REQUEST Success");

                    var gameRange = new ArrayList<JSONObject>();

                    // @DUMMY
                    var game = new JSONObject();
                    game.put("game_id", 0);

                    game.put("player_id", new JSONArray(new int[]{0,1,2,3}));
                    game.put("name", "Ludder Fight");
                    gameRange.add(game);

                    // @DUMMY
                    var game2 = new JSONObject();
                    game2.put("game_id", 1);
                    game2.put("player_id", new JSONArray(new int[]{0}));
                    game2.put("name", "Ludo Capital");
                    gameRange.add(game2);

                    // @DUMMY
                    var game3 = new JSONObject();
                    game3.put("game_id", 2);
                    game3.put("player_id", new JSONArray(new int[]{}));
                    game3.put("name", "Showdown of ages");
                    gameRange.add(game3);


                    Logger.log(Level.DEBUG, "" + game.toString() + " " + game2.toString());

                    mCacheManager.commit(cache -> {
                        cache.gameRange.clear();
                        cache.gameRange.addAll(gameRange);
                    });

                    mTransitions.renderSearch();
                },
                (req, error) -> Logger.log(Level.WARN, "Request -> GET_GAME_RANGE_REQUEST failed"));


        final var item3 = new JSONObject();
        item.put("page_index", 0);
        Request requestChatRange = mRequestFactory.make(GET_CHAT_RANGE_REQUEST, item, mAuth,
                (req, success) -> {
                    Logger.log(Level.DEBUG, "Request -> GET_CHAT_RANGE_REQUEST Success");

                    var chatRange = new ArrayList<JSONObject>();

                    // @DUMMY
                    var chat = new JSONObject();
                    chat .put("chat_id", 0);
                    chat.put("name", "DarkRoom");
                    chat.put("participant_id", new JSONArray(new int[]{0,2}));
                    chatRange.add(chat);

                    // @DUMMY
                    var chat2 = new JSONObject();
                    chat2 .put("chat_id", 1);
                    chat2.put("name", "WastedWastes");
                    chat2.put("participant_id", new JSONArray(new int[]{0,2,4,5,6,7,8}));
                    chatRange.add(chat2);

                    mCacheManager.commit(cache -> {
                        cache.chatRange.clear();
                        cache.chatRange.addAll(chatRange);
                    });
                    mTransitions.renderSearch();
                },
                (req, error) -> Logger.log(Level.WARN, "Request -> GET_CHAT_RANGE_REQUEST failed"));


        final var item4 = new JSONObject();
        item.put("page_index", 0);
        Request requestFriendRange = mRequestFactory.make(GET_FRIEND_RANGE_REQUEST, item, mAuth,
                (req, success) -> {
                    Logger.log(Level.DEBUG, "Request -> GET_FRIEND_RANGE_REQUEST Success");

                    var friendRange = new ArrayList<JSONObject>();

                    // @DUMMY
                    var friend = new JSONObject();
                    friend.put("user_id", 0);
                    friend.put("username", "Patrick Patriot");
                    friendRange.add(friend);

                    // @DUMMY
                    var friend2 = new JSONObject();
                    friend2.put("user_id", 1);
                    friend2.put("username", "Senna Sanoi");
                    friendRange.add(friend2);

                    // @DUMMY
                    var friend3 = new JSONObject();
                    friend3.put("user_id", 2);
                    friend3.put("username", "Mah Man");
                    friendRange.add(friend3);

                    // @DUMMY
                    var friend4 = new JSONObject();
                    friend4.put("user_id", 3);
                    friend4.put("username", "Alla Saman");
                    friendRange.add(friend4);

                    mCacheManager.commit(cache -> {
                        cache.friendRange.clear();
                        cache.friendRange.addAll(friendRange);
                    });
                    mTransitions.renderSearch();
                },
                (req, error) -> Logger.log(Level.WARN, "Request -> GET_FRIEND_RANGE_REQUEST failed"));


        final var item2 = new JSONObject();
        item.put("page_index", 0);
        Request requestUserRange = mRequestFactory.make(GET_USER_RANGE_REQUEST, item, mAuth,
                (req, success) -> {
                    Logger.log(Level.DEBUG, "Request -> GET_USER_RANGE_REQUEST Success");

                    var userRange = new ArrayList<JSONObject>();

                    // @DUMMY
                    var user = new JSONObject();
                    user.put("user_id", 0);
                    user.put("username", "Nalle Bordvik");
                    userRange .add(user);

                    // @DUMMY
                    var user2 = new JSONObject();
                    user2.put("user_id", 1);
                    user2.put("username", "Jonas Solsvik");
                    userRange.add(user2);

                    mCacheManager.commit(cache -> {
                        cache.userRange.clear();
                        cache.userRange.addAll(userRange);
                    });
                    mTransitions.renderSearch();
                },
                (req, error) -> Logger.log(Level.WARN, "Request -> GET_USER_RANGE_REQUEST failed"));

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
    public void newGame() {
        this.startAction("newGame");
    }

    /**
     *
     */
    public void newChat() {
        this.startAction("newChat");
    }

    /**
     *
     */
    public void submitGame() {
        this.startAction("submitGame");
    }

    /**
     *
     */
    public void submitChat() {
        this.startAction("submitChat");
    }

    /**
     *
     */
    public void getUser() {
        this.startAction("getUser");
    }

    /**
     *
     */
    public void updateUser() {
        this.startAction("updateUser");
    }

    /**
     *
     */
    public void deleteUser() {
        this.startAction("deleteUser");
    }

    /**
     *
     */
    public void getFriend() {
        this.startAction("getFriend");
    }

    /**
     *
     */
    public void friend() {
        this.startAction("friend");
    }

    /**
     *
     */
    public void unfriend() {
        this.startAction("unfriend");
    }

    /**
     *
     */
    public void joinChat() {
        this.startAction("joinChat");
    }

    /**
     *
     */
    public void leaveChat() {
        this.startAction("leaveChat");
    }

    /**
     *
     */
    public void getChat() {
        this.startAction("login");
    }


    /**
     *
     */
    public void sendChatMessage() {
        this.startAction("sendChatMessage");
    }

    /**
     *
     */
    public void sendChatInvite() {
        this.startAction("sendChatInvite");
    }


    /**
     *
     */
    public void joinGame() {
        this.startAction("joinGame");
    }

    /**
     *
     */
    public void leaveGame() {
        this.startAction("leaveGame");
    }

    /**
     *
     */
    public void sendGameInvite() {
        this.startAction("sendGameInvite");
    }

    /**
     *
     */
    public void declineGameInvite() {
        this.startAction("declineGameInvite");
    }

    /**
     *
     */
    public void startGame() {
        this.startAction("startGame");
    }

    /**
     *
     */
    public void getGame() {
        this.startAction("getGame");
    }

    /**
     *
     */
    public void getGameState() {
        this.startAction("getGameState");
    }

    /**
     *
     */
    public void sendRollDice() {
        this.startAction("sendRollDice");
    }

    /**
     *
     */
    public void movePiece() {
        this.startAction("movePiece");
    }

    /**
     * Do prep-work and log action.
     *
     * @param methodName name of callee
     */
    private void startAction(String methodName) {
        Logger.log(Level.INFO, "Action -> " + methodName);
        mLocalCache = mCacheManager.copy();
        mAuth = mLocalCache.authToken;
    }
}