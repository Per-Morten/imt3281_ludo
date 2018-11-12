package no.ntnu.imt3281.ludo.client;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

import no.ntnu.imt3281.ludo.api.RequestFactory;
import no.ntnu.imt3281.ludo.api.RequestType;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;
import no.ntnu.imt3281.ludo.gui.IGUIController;
import no.ntnu.imt3281.ludo.gui.Transitions;
import org.json.JSONObject;

public class ActionConsumer implements Runnable {

    private final ArrayBlockingQueue<Action> mIncommingActions = new ArrayBlockingQueue<Action>(100);
    private final RequestFactory mRequestFactory = new RequestFactory();
    private Transitions mTransitions;
    private ResponseConsumer mResponseConsumer;
    private SocketManager mSocketManager;
    private State mCurrentState = new State();

    private HashMap<String, IGUIController> mControllers = new HashMap<>();

    /**
     * Bind dependencies of ActionConsumer
     *
     * @param transitions to feed mutations
     * @param responseConsumer to push requests
     * @param socketManager to send messages
     */
    void bind(Transitions transitions, ResponseConsumer responseConsumer, SocketManager socketManager) {
        mTransitions = transitions;
        mSocketManager = socketManager;
        mResponseConsumer = responseConsumer;
    }

    /**
     * Thread entry point
     */
    @Override
    public void run() {
        Logger.log(Level.INFO, "Hello from a ActionConsumer thread!");

        boolean running = true;
        while(running) {
            try {
                Action action = this.mIncommingActions.take();
                mCurrentState = mTransitions.getCurrentState();
                action.run(this);
            } catch (InterruptedException e) {
                Logger.log(Level.INFO, "InterruptedException when consuming action");
                running = false;
            }
        }
        Logger.log(Level.INFO, "Byebye from a ActionConsumer thread!");
    }

    /**
     * Feed action from other thread
     *
     * @param action action to consume
     * */
    public void feed(Action action) {
        try {
            this.mIncommingActions.put(action);
        } catch (InterruptedException e) {
            Logger.log(Level.INFO, "InterruptedException when feeding action");
        }
    }

    /**
     * login user with username and password
     *
     * @param email valid email
     * @param password valid password
     */
    public void login(String email, String password) {
        this.logAction("login");

        var payload = new ArrayList<JSONObject>();
        {
            var item = mRequestFactory.makeItem();
            item.put("email", email);
            item.put("password", password);
            payload.add(item);
        }

        var request = mRequestFactory.makeRequest(RequestType.LoginRequest, this.token(), payload);
        {
            this.send(request);
        }
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

        var payload = new ArrayList<JSONObject>();
        {
            var item = mRequestFactory.makeItem();
            item.put("email", email);
            item.put("password", password);
            item.put("username", username);
            payload.add(item);
        }

        var request = mRequestFactory.makeRequest(RequestType.CreateUserRequest, this.token(), payload);
        {
            this.send(request);
        }
    }


    /**
     *
     */
    public void logout() {
        this.logAction("logout");
        mTransitions.feed(Transitions::logoutSuccess);
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


    /**
     * Send message to socket
     *
     * @param request to be sent
     */
    private void send(JSONObject request) {

        mResponseConsumer.feedRequest(request);

        try {
            mSocketManager.send(request.toString());
        } catch (IOException e) {
            Logger.log(Level.ERROR, "IOException when trying to send message to socket:" +e.toString());
        }

    }



    private String token() {
        return mCurrentState.token;
    }
}