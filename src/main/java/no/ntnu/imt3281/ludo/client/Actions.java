package no.ntnu.imt3281.ludo.client;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import no.ntnu.imt3281.ludo.api.API;
import no.ntnu.imt3281.ludo.api.Request;
import no.ntnu.imt3281.ludo.api.RequestFactory;
import no.ntnu.imt3281.ludo.api.RequestType;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;
import no.ntnu.imt3281.ludo.gui.IGUIController;
import no.ntnu.imt3281.ludo.gui.Transitions;
import org.json.JSONObject;

import static no.ntnu.imt3281.ludo.api.RequestType.CreateUserRequest;
import static no.ntnu.imt3281.ludo.api.RequestType.LoginRequest;

public class Actions {

    private Transitions mTransitions;
    private API mAPI;
    private StateManager mStateManager;
    private State mState = new State();
    private final RequestFactory mRequestFactory = new RequestFactory();


    /**
     * Bind dependencies of Actions
     *
     * @param transitions to feed mutations
     * @param API to push requests
     */
    void bind(Transitions transitions, API API, StateManager stateManager) {
        mTransitions = transitions;
        mStateManager = stateManager;
        mAPI = API;
    }

    /**
     * login user with username and password
     *
     * @param email valid email
     * @param password valid password
     */
    public void login(String email, String password) {
        this.logAction("login");

        var item = new JSONObject();
        item.put("email", email);
        item.put("password", password);

        Request request = mRequestFactory.make(
                LoginRequest,
                item,
                (req, success) -> Logger.log(Level.INFO, "Action -> LoginSuccess: " + success.toString()),
                (req, error) -> Logger.log(Level.INFO, "Action -> LoginError: " + error.toString()));

        mAPI.send(request);
    }

    /**
     * login user with username and password
     *
     * @param email valid email
     * @param username valid username
     * @param password valid password
     */
    public void createUser(String email, String password, String username) {
        this.logAction("createUser");

        var item = new JSONObject();
        item.put("email", email);
        item.put("password", password);
        item.put("username", username);

        Request request = mRequestFactory.make(
                CreateUserRequest,
                item,
                (req, success) -> Logger.log(Level.INFO, "Action -> CreateUserSuccess"),
                (req, error) -> Logger.log(Level.INFO, "Action -> CreateUserError"));

        mAPI.send(request);
    }


    /**
     *
     */
    public void logout() {
        this.logAction("logout");
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