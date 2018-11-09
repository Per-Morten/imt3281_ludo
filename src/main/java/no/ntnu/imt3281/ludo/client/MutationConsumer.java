package no.ntnu.imt3281.ludo.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import no.ntnu.imt3281.ludo.gui.LudoController;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;
import javafx.stage.Stage;

class MutationConsumer implements Runnable {

    private final static Logger LOGGER = Logger.getLogger(ActionConsumer.class.getName());
    private final ArrayBlockingQueue<Runnable> incommingMutations = new ArrayBlockingQueue<Runnable>(100);
    private State mCurrentState;
    private ActionConsumer mActionConsumer;

    @Override
    public void run() {
        // LOGGER.setLevel(Level.ALL);
        System.out.println("Hello from a MutationConsumer thread!");

        boolean running = true;
        while(running) {
            try {
                var mutation = this.incommingMutations.take();
                mutation.run();

            } catch (InterruptedException ie) {
                running = false;
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Byebye from a MutationConsumer thread!");
    }

    void commit(Runnable mutation) {
        try {
            this.incommingMutations.put(mutation);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    void bind(ActionConsumer actionConsumer) {
        mActionConsumer = actionConsumer;
    }


    /**
     * Display network error, do nothing
     */
    public void networkError() {

    }

    /**
     * Display logging in...
     */
    public void loginPending() {

    }

    public void loginSuccess() {
        
    }

    public void loginError() {
        
    }

    void LogoutPending() {
        
    }

    void LogoutSuccess() {
        
    }

    void LogoutError() {
        
    }

    void GetUserPending() {
        
    }

    void GetUserSuccess() {
        
    }

    void GetUserError() {
        
    }

    void CreateUserPending() {
        
    }

    void CreateUserSuccess() {
        
    }

    void CreateUserError() {
        
    }

    void UpdateUserPending() {
        
    }

    void UpdateUserSuccess() {
        
    }

    void UpdateUserError() {
        
    }

    void DeleteUserPending() {
        
    }

    void DeleteUserSuccess() {
        
    }

    void DeleteUserError() {
        
    }

    void GetFriendPending() {
        
    }

    void GetFriendSuccess() {
        
    }

    void GetFriendError() {
        
    }

    void FriendPending() {
        
    }

    void FriendSuccess() {
        
    }

    void FriendError() {
        
    }

    void UnfriendPending() {
        
    }

    void UnfriendSuccess() {
        
    }

    void UnfriendError() {
        
    }

    void JoinChatPending() {
        
    }

    void JoinChatSuccess() {
        
    }

    void JoinChatError() {
        
    }

    void LeaveChatPending() {
        
    }

    void LeaveChatSuccess() {
        
    }

    void LeaveChatError() {
        
    }

    void GetChatPending() {
        
    }

    void GetChatSuccess() {
        
    }

    void GetChatError() {
        
    }

    void CreateChatPending() {
        
    }

    void CreateChatSuccess() {
        
    }

    void CreateChatError() {
        
    }

    void SendChatMessagePending() {
        
    }

    void SendChatMessageSuccess() {
        
    }

    void SendChatMessageError() {
        
    }

    void SendChatInvitePending() {
        
    }

    void SendChatInviteSuccess() {
        
    }

    void SendChatInviteError() {
        
    }

    void CreateGamePending() {
        
    }

    void CreateGameSuccess() {
        
    }

    void CreateGameError() {
        
    }

    void JoinGamePending() {
        
    }

    void JoinGameSuccess() {
        
    }

    void JoinGameError() {
        
    }

    void LeaveGamePending() {
        
    }

    void LeaveGameSuccess() {
        
    }

    void LeaveGameError() {
        
    }

    void SendGameInvitePending() {
        
    }

    void SendGameInviteSuccess() {
        
    }

    void SendGameInviteError() {
        
    }

    void DeclineGameInvitePending() {
        
    }

    void DeclineGameInviteSuccess() {
        
    }

    void DeclineGameInviteError() {
        
    }

    void StartGamePending() {
        
    }

    void StartGameSuccess() {
        
    }

    void StartGameError() {
        
    }

    void GetGameHeaderPending() {
        
    }

    void GetGameHeaderSuccess() {
        
    }

    void GetGameHeaderError() {
        
    }

    void GetGameStatePending() {
        
    }

    void GetGameStateSuccess() {
        
    }

    void GetGameStateError() {
        
    }

    void SendRollDicePending() {
        
    }

    void SendRollDiceSuccess() {
        
    }

    void SendRollDiceError() {
        
    }

    void MovePiecePending() {
        
    }

    void MovePieceSuccess() {
        
    }

    void MovePieceError() {
        
    }
   
    /**
     * Log action name
     */
    void logMutation() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        var element = stackTraceElements[1];
        LOGGER.info("Mutation : " + element.getMethodName());
    }
}
