package no.ntnu.imt3281.ludo.client;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import no.ntnu.imt3281.ludo.api.RequestFactory;
import no.ntnu.imt3281.ludo.api.RequestType;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;
import no.ntnu.imt3281.ludo.gui.MutationConsumer;
import org.json.JSONObject;

public class ActionConsumer implements Runnable {

    private final ArrayBlockingQueue<Action> mIncommingActions = new ArrayBlockingQueue<Action>(100);
    private final RequestFactory mRequestFactory = new RequestFactory();
    private MutationConsumer mMutationConsumer;
    private SocketManager mSocketManager;
    private State mCurrentState = new State();
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
                mCurrentState = mMutationConsumer.getCurrentState();
                action.run(this);
            } catch (InterruptedException e) {
                Logger.log(Level.INFO, "InterruptedException when consuming action: " + e.toString());
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
            Logger.log(Level.ERROR, "InterruptedException when dispatching action to ActionConsumer");
        }
    }

    /**
     * login user with username and password
     *
     * @param email valid email
     * @param password valid password
     */
    public void login(String email, String password) {
        this.startAction("login");

        var payload = new ArrayList<JSONObject>();
        {
            var item = mRequestFactory.makeItem();
            item.put("email", email);
            item.put("password", password);
            payload.add(item);
        }

        var request = mRequestFactory.makeRequest(RequestType.LoginRequest, this.token(), payload);
        {
            this.send(request.toString());
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
        this.startAction("createUser");

        var payload = new ArrayList<JSONObject>();
        {
            var item = mRequestFactory.makeItem();
            item.put("email", email);
            item.put("password", password);
            item.put("username", username);
            payload.add(item);
        }

        var request = mRequestFactory.makeRequest(RequestType.LoginRequest, this.token(), payload);
        {
            this.send(request.toString());
        }
    }


    /**
     *
     */
    public void logout() {
        this.startAction("logout");
        mMutationConsumer.feed(MutationConsumer::logoutSuccess);
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
     * Bind dependencies of ActionConsumer
     *
     * @param mutationConsumer to commit mutations
     * @param socketManager to send messages
     */
    void bind(MutationConsumer mutationConsumer, SocketManager socketManager) {
        mMutationConsumer = mutationConsumer;
        mSocketManager = socketManager;
    }

    /**
     * Log action level info
     *
     * @param methodName name of callee
     */
    private void startAction(String methodName) {
        Logger.log(Level.INFO, "Action -> " + methodName);
    }


    /**
     * Send message to socket
     *
     * @param message message to send
     */
    private void send(String message) {
        try {
            mSocketManager.send(message);
        } catch (IOException e) {
            Logger.log(Level.ERROR, "IOException when trying to send message to socket:" +e.toString());
        }
    }



    private String token() {
        return mCurrentState.token;
    }
}