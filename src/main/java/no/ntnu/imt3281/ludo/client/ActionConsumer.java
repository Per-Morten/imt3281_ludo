package no.ntnu.imt3281.ludo.client;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActionListener implements Runnable {

    private final static Logger LOGGER = Logger.getLogger(ActionListener.class.getName());
    private final ArrayBlockingQueue<Runnable> incommingActions = new ArrayBlockingQueue<Runnable>(100);

    @Override
    public void run() {
        LOGGER.setLevel(Level.ALL);
        System.out.println("Hello from a ActionListener thread!");

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
        System.out.println("Byebye from a ActionListener thread!");
    }

    void dispatch(Runnable action) throws InterruptedException {
        this.incommingActions.put(action);
    }
    
    void Login(String username, String password) {
        
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