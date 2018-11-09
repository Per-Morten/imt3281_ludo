package no.ntnu.imt3281.ludo.client;

import javafx.application.Platform;

import java.io.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActionConsumer implements Runnable {

    private final static Logger LOGGER = Logger.getLogger(ActionConsumer.class.getName());
    private final ArrayBlockingQueue<Runnable> incommingActions = new ArrayBlockingQueue<Runnable>(100);
    private PrintWriter mRequestWriter;
    private MutationConsumer mMutationConsumer;

    @Override
    public void run() {
        LOGGER.setLevel(Level.ALL);
        System.out.println("Hello from a ActionConsumer thread!");

        boolean running = true;
        while(running) {
            try {
                Runnable action = this.incommingActions.take();
                action.run();
            } catch (InterruptedException ie) {
                running = false;
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Byebye from a ActionConsumer thread!");
    }

    void bind(MutationConsumer mutationConsumer, OutputStream requestStream) {
        mMutationConsumer = mutationConsumer;
        mRequestWriter = new PrintWriter(requestStream);
    }

    public void dispatch(Runnable action) {
        try {
            this.incommingActions.put(action);
            System.out.println("Action incomming");
        } catch (InterruptedException e) {
            e.printStackTrace();
            Platform.exit();
        }
    }
    
    public void login(String username, String password) {
        mRequestWriter.println(username + password);
        mRequestWriter.flush();
        mMutationConsumer.commit(() -> mMutationConsumer.loginPending());
    }

    void Logout() {

    }

    void GetUser() {

    }

    void CreateUser() {

    }

    void UpdateUser() {

    }

    void DeleteUser() {

    }

    void GetFriend() {

    }

    void Friend() {

    }

    void Unfriend() {

    }

    void JoinChat() {

    }

    void LeaveChat() {

    }

    void GetChat() {

    }

    void CreateChat() {

    }

    void SendChatMessage() {

    }

    void SendChatInvite() {

    }

    void CreateGame() {

    }

    void JoinGame() {

    }

    void LeaveGame() {

    }

    void SendGameInvite() {

    }

    void DeclineGameInvite() {

    }

    void StartGame() {

    }

    void GetGameHeader() {

    }

    void GetGameState() {

    }

    void SendRollDice() {

    }

    void MovePiece() {

    }
   

    /**
     * Log action name
     */
    void logAction() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        var element = stackTraceElements[1];
        LOGGER.info("Action : " + element.getMethodName());
    }
}