package no.ntnu.imt3281.ludo.client;

import javafx.application.Platform;

import java.io.*;
import java.util.concurrent.ArrayBlockingQueue;

import no.ntnu.imt3281.ludo.gui.MutationConsumer;

public class ActionConsumer implements Runnable {

    private final ArrayBlockingQueue<Action> mIncommingActions = new ArrayBlockingQueue<Action>(100);
    private PrintWriter mRequestWriter;
    private MutationConsumer mMutationConsumer;

    void bind(MutationConsumer mutationConsumer, OutputStream requestStream) {
        mMutationConsumer = mutationConsumer;
        mRequestWriter = new PrintWriter(requestStream);
    }

    public void dispatch(Action action) {
        try {
            this.mIncommingActions.put(action);
            System.out.println("Action incomming");
            // TODO LOG INFO
        } catch (InterruptedException e) {
            e.printStackTrace();
            Platform.exit();
            // TODO LOG ERROR
        }
    }

    @Override
    public void run() {
        System.out.println("Hello from a ActionConsumer thread!");

        boolean running = true;
        while(running) {
            try {
                Action action = this.mIncommingActions.take();
                action.run(this);
            } catch (InterruptedException ie) {
                running = false;
                Thread.currentThread().interrupt();
                // TODO LOG ERROR
            }
        }
        System.out.println("Byebye from a ActionConsumer thread!");
    }

    public void login(String username, String password) {
        mRequestWriter.println(username + password);
        mRequestWriter.flush();
        mMutationConsumer.commit(MutationConsumer::loginPending);
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
    }
}