package no.ntnu.imt3281.ludo.client;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

class MutationListener implements Runnable {

    private final static Logger LOGGER = Logger.getLogger(ActionConsumer.class.getName());
    private final ArrayBlockingQueue<Runnable> incommingMutations = new ArrayBlockingQueue<Runnable>(100);

    private State mCurrentState;

    @Override
    public void run() {
        // LOGGER.setLevel(Level.ALL);
        System.out.println("Hello from a MutationListener thread!");

        mCurrentState = new State();

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
        System.out.println("Byebye from a MutationListener thread!");
    }

    void commit(Runnable mutation) throws InterruptedException {
        this.incommingMutations.put(mutation);
    }
    
    void LoginPending() {
        
    }

    void LoginSuccess() {
        
    }

    void LoginError() {
        
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
