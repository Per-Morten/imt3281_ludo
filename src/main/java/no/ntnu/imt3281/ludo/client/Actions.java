package no.ntnu.imt3281.ludo.client;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Actions implements Runnable {

    final static Logger LOGGER = Logger.getLogger(Actions.class.getName());
    final ArrayBlockingQueue<Action> incommingActions = new ArrayBlockingQueue<Action>(100);

    @Override
    public void run() {
        
        LOGGER.setLevel(Level.ALL);

        while(true) {
            System.out.println("Hello from a Actions thread!");
            try {
                Action action = this.incommingActions.take();
                action.consumer.consume(action);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void dispatch(Action action) throws InterruptedException {
        this.incommingActions.put(action);
    }
    
    public void Login(Action action) {

    }

    public void Logout(Action action) {

    }

    public void GetUser(Action action) {

    }

    public void CreateUser(Action action) {

    }

    public void UpdateUser(Action action) {

    }

    public void DeleteUser(Action action) {

    }

    public void GetFriend(Action action) {

    }

    public void Friend(Action action) {

    }

    public void Unfriend(Action action) {

    }

    public void JoinChat(Action action) {

    }

    public void LeaveChat(Action action) {

    }

    public void GetChat(Action action) {

    }

    public void CreateChat(Action action) {

    }

    public void SendChatMessage(Action action) {

    }

    public void SendChatInvite(Action action) {

    }

    public void CreateGame(Action action) {

    }

    public void JoinGame(Action action) {

    }

    public void LeaveGame(Action action) {

    }

    public void SendGameInvite(Action action) {

    }

    public void DeclineGameInvite(Action action) {

    }

    public void StartGame(Action action) {

    }

    public void GetGameHeader(Action action) {

    }

    public void GetGameState(Action action) {

    }

    public void SendRollDice(Action action) {

    }

    public void MovePiece(Action action) {

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