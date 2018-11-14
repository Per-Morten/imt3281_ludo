package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;
import no.ntnu.imt3281.ludo.gui.Transitions;
import org.json.JSONObject;

import static no.ntnu.imt3281.ludo.api.RequestType.CREATE_USER_REQUEST;
import static no.ntnu.imt3281.ludo.api.RequestType.LOGIN_REQUEST;
import static no.ntnu.imt3281.ludo.api.RequestType.LOGOUT_REQUEST;

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
        this.startAction("login");

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
                (req, error) -> Logger.log(Level.WARN, "Action -> LoginError: " + error.toString()));

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
        this.startAction("createUser");

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
                (req, error) -> Logger.log(Level.WARN, "Action -> CreateUserError"));

        mAPI.send(request);
    }

    /**
     * Logout the user with user_id stored in state.
     */
    public void logout() {
        this.startAction("logout");

        var item = new JSONObject();
        item.put("user_id", mLocalState.userId);

        Request request = mRequestFactory.make(LOGOUT_REQUEST, item, mLocalState.authToken,
                (req, success) -> {
                    Logger.log(Level.DEBUG, "Action -> LogoutSucces");

                    mStateManager.commit(state -> {
                        state.userId = -1;
                        state.email = "";
                        state.authToken = "";
                        state.username = "";
                    });
                    mTransitions.render("Login.fxml");
                },
                (req, error) -> Logger.log(Level.WARN, "Action -> LogoutError"));

        mAPI.send(request);
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
    public void createChat() {
        this.startAction("createChat");
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
    public void createGame() {
        this.startAction("createGame");
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
        mLocalState = mStateManager.copy();
    }
}