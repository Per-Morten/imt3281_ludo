package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;
import no.ntnu.imt3281.ludo.gui.Transitions;
import org.json.JSONObject;

import static no.ntnu.imt3281.ludo.api.RequestType.CREATE_USER_REQUEST;
import static no.ntnu.imt3281.ludo.api.RequestType.LOGIN_REQUEST;

public class Actions {

    private Transitions mTransitions;
    private API mAPI;
    private StateManager mStateManager;
    private State mLocalState = new State();
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
        this.logAction("login");

        var item = new JSONObject();
        item.put("email", email);
        item.put("password", password);

        Request request = mRequestFactory.make(LOGIN_REQUEST, item, mLocalState.authToken,
                (req, success) -> {
                    Logger.log(Level.DEBUG, "Action -> LoginSuccess: " + success.toString());

                    mStateManager.commit(state -> {
                        // @DUMMY
                        state.authToken = "afaa254";
                        // @DUMMY
                        state.userId = 2;
                    });
                    mTransitions.render("Ludo.fxml");
                },
                (req, error) -> Logger.log(Level.DEBUG, "Action -> LoginError: " + error.toString()));

        mAPI.send(request);
    }

    /**
     * login user with username and password
     *
     * @param email    valid email
     * @param username valid username
     * @param password valid password
     */
    public void createUser(String email, String password, String username) {
        this.logAction("createUser");

        var item = new JSONObject();
        item.put("email", email);
        item.put("password", password);
        item.put("username", username);

        Request request = mRequestFactory.make(CREATE_USER_REQUEST, item, mLocalState.authToken,
                (req, success) -> {
                    Logger.log(Level.DEBUG, "Action -> CreateUserSuccess");

                    mStateManager.commit(state -> {
                        // @DUMMY
                        state.userId = 2;
                        // @DUMMY
                        state.email = "jonas.solsvik@gmail.com";
                        // @DUMMY
                        state.username = "jonasjso";
                    });
                    mTransitions.render("Login.fxml");
                },
                (req, error) -> Logger.log(Level.DEBUG, "Action -> CreateUserError"));

        mAPI.send(request);
    }

    /**
     *
     */
    public void logout() {
        this.startAction("logout");

        var item = new JSONObject();
        item.put("user_id", mLocalState.userId);

        Request request = mRequestFactory.make(LOGOUT_REQUEST, item, mLocalState.authToken,
                (req, success) -> Logger.log(Level.DEBUG, "Action -> logout"),
                (req, error) -> Logger.log(Level.DEBUG, "Action -> logout"));

        mAPI.send(request);

        mStateManager.commit(state -> {
            state.userId = -1;
            state.email = "";
            state.authToken = "";
            state.username = "";
        });
        mTransitions.render("Login.fxml");
    }

    /**
     *
     */
    public void getUser() {
        this.logAction("getUser");
    }

    /**
     *
     */
    public void updateUser() {
        this.logAction("updateUser");
    }

    /**
     *
     */
    public void deleteUser() {
        this.logAction("deleteUser");
    }

    /**
     *
     */
    public void getFriend() {
        this.logAction("getFriend");
    }

    /**
     *
     */
    public void friend() {
        this.logAction("friend");
    }

    /**
     *
     */
    public void unfriend() {
        this.logAction("unfriend");
    }

    /**
     *
     */
    public void joinChat() {
        this.logAction("joinChat");
    }

    /**
     *
     */
    public void leaveChat() {
        this.logAction("leaveChat");
    }

    /**
     *
     */
    public void getChat() {
        this.logAction("login");
    }

    /**
     *
     */
    public void createChat() {
        this.logAction("createChat");
    }

    /**
     *
     */
    public void sendChatMessage() {
        this.logAction("sendChatMessage");
    }

    /**
     *
     */
    public void sendChatInvite() {
        this.logAction("sendChatInvite");
    }

    /**
     *
     */
    public void createGame() {
        this.logAction("createGame");
    }

    /**
     *
     */
    public void joinGame() {
        this.logAction("joinGame");
    }

    /**
     *
     */
    public void leaveGame() {
        this.logAction("leaveGame");
    }

    /**
     *
     */
    public void sendGameInvite() {
        this.logAction("sendGameInvite");
    }

    /**
     *
     */
    public void declineGameInvite() {
        this.logAction("declineGameInvite");
    }

    /**
     *
     */
    public void startGame() {
        this.logAction("startGame");
    }

    /**
     *
     */
    public void getGame() {
        this.logAction("getGame");
    }

    /**
     *
     */
    public void getGameState() {
        this.logAction("getGameState");
    }

    /**
     *
     */
    public void sendRollDice() {
        this.logAction("sendRollDice");
    }

    /**
     *
     */
    public void movePiece() {
        this.logAction("movePiece");
    }

    /**
     * Log action level info
     *
     * @param methodName name of callee
     */
    private void logAction(String methodName) {
        Logger.log(Level.INFO, "Action -> " + methodName);
    }
}