package no.ntnu.imt3281.ludo.client;

import java.io.*;
import java.util.concurrent.ArrayBlockingQueue;

import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;
import no.ntnu.imt3281.ludo.gui.Mutation;
import no.ntnu.imt3281.ludo.gui.MutationConsumer;

public class ActionConsumer implements Runnable {

    private final ArrayBlockingQueue<Action> mIncommingActions = new ArrayBlockingQueue<Action>(100);
    private PrintWriter mRequestWriter;
    private MutationConsumer mMutationConsumer;
    private SocketManager mSocketManager;

    /**
     * Thread entry point
     */
    @Override
    public void run() {
        Logger.log(Level.INFO, "Hello from a ActionConsumer thread!");

        boolean running = true;
        while(running) {
            try {
                this.consume();
            } catch (InterruptedException e) {
                Logger.log(Level.ERROR, "InterruptedException when consuming action: " + e.toString());
            }
        }
        Logger.log(Level.INFO, "Byebye from a ActionConsumer thread!");
    }

    /**
     * Dispatch action to the ActionConsumer thread
     *
     * @param action action to consume
     * */
    public void dispatch(Action action) {
        try {
            this.mIncommingActions.put(action);
        } catch (InterruptedException e) {
            Logger.log(Level.ERROR, "InterruptedException when dispatching action to ActionConsumer");
        }
    }

    /**
     * login user with username and password
     *
     * @param username existing username
     * @param password existing password
     */
    public void login(String username, String password) {
        this.logAction("login");
        this.send(username + password);
        this.commit(MutationConsumer::loginPending);
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
    public void createUser() {
        this.logAction("createUser");
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
     * Consume first action in the incommingActions buffer
     *
     * @throws InterruptedException if incommingActions buffer is interrupted
     */
    private void consume() throws InterruptedException {
        Action action = this.mIncommingActions.take();
        action.run(this);
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
     * Commit mutation to mutation consumer
     *
     * @param mutation mutation to commit
     */
    private void commit(Mutation mutation) {
        mMutationConsumer.commit(mutation);
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
}