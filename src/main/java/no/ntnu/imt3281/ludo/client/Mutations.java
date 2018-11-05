package no.ntnu.imt3281.ludo.client;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;
import no.ntnu.imt3281.buffers.BlockingBuffer;

public class Mutations implements Runnable {

    final static Logger LOGGER = Logger.getLogger(Actions.class.getName());
    final ArrayBlockingQueue<Mutation> incommingMutations = new ArrayBlockingQueue<Mutation>(100);

    @Override
    public void run() {
        while(true) {
            System.out.println("Hello from a Mutations thread!");
            try {
                Mutation mutation = this.incommingMutations.take();
                mutation.consumer.consume(mutation);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void commit(Mutation mutation) throws InterruptedException {
        this.incommingMutations.put(mutation);
    }
    
    public void LoginPending(Mutation mutation) {
        
    }

    public void LoginSuccess(Mutation mutation) {
        
    }

    public void LoginError(Mutation mutation) {
        
    }

    public void LogoutPending(Mutation mutation) {
        
    }

    public void LogoutSuccess(Mutation mutation) {
        
    }

    public void LogoutError(Mutation mutation) {
        
    }

    public void GetUserPending(Mutation mutation) {
        
    }

    public void GetUserSuccess(Mutation mutation) {
        
    }

    public void GetUserError(Mutation mutation) {
        
    }

    public void CreateUserPending(Mutation mutation) {
        
    }

    public void CreateUserSuccess(Mutation mutation) {
        
    }

    public void CreateUserError(Mutation mutation) {
        
    }

    public void UpdateUserPending(Mutation mutation) {
        
    }

    public void UpdateUserSuccess(Mutation mutation) {
        
    }

    public void UpdateUserError(Mutation mutation) {
        
    }

    public void DeleteUserPending(Mutation mutation) {
        
    }

    public void DeleteUserSuccess(Mutation mutation) {
        
    }

    public void DeleteUserError(Mutation mutation) {
        
    }

    public void GetFriendPending(Mutation mutation) {
        
    }

    public void GetFriendSuccess(Mutation mutation) {
        
    }

    public void GetFriendError(Mutation mutation) {
        
    }

    public void FriendPending(Mutation mutation) {
        
    }

    public void FriendSuccess(Mutation mutation) {
        
    }

    public void FriendError(Mutation mutation) {
        
    }

    public void UnfriendPending(Mutation mutation) {
        
    }

    public void UnfriendSuccess(Mutation mutation) {
        
    }

    public void UnfriendError(Mutation mutation) {
        
    }

    public void JoinChatPending(Mutation mutation) {
        
    }

    public void JoinChatSuccess(Mutation mutation) {
        
    }

    public void JoinChatError(Mutation mutation) {
        
    }

    public void LeaveChatPending(Mutation mutation) {
        
    }

    public void LeaveChatSuccess(Mutation mutation) {
        
    }

    public void LeaveChatError(Mutation mutation) {
        
    }

    public void GetChatPending(Mutation mutation) {
        
    }

    public void GetChatSuccess(Mutation mutation) {
        
    }

    public void GetChatError(Mutation mutation) {
        
    }

    public void CreateChatPending(Mutation mutation) {
        
    }

    public void CreateChatSuccess(Mutation mutation) {
        
    }

    public void CreateChatError(Mutation mutation) {
        
    }

    public void SendChatMessagePending(Mutation mutation) {
        
    }

    public void SendChatMessageSuccess(Mutation mutation) {
        
    }

    public void SendChatMessageError(Mutation mutation) {
        
    }

    public void SendChatInvitePending(Mutation mutation) {
        
    }

    public void SendChatInviteSuccess(Mutation mutation) {
        
    }

    public void SendChatInviteError(Mutation mutation) {
        
    }

    public void CreateGamePending(Mutation mutation) {
        
    }

    public void CreateGameSuccess(Mutation mutation) {
        
    }

    public void CreateGameError(Mutation mutation) {
        
    }

    public void JoinGamePending(Mutation mutation) {
        
    }

    public void JoinGameSuccess(Mutation mutation) {
        
    }

    public void JoinGameError(Mutation mutation) {
        
    }

    public void LeaveGamePending(Mutation mutation) {
        
    }

    public void LeaveGameSuccess(Mutation mutation) {
        
    }

    public void LeaveGameError(Mutation mutation) {
        
    }

    public void SendGameInvitePending(Mutation mutation) {
        
    }

    public void SendGameInviteSuccess(Mutation mutation) {
        
    }

    public void SendGameInviteError(Mutation mutation) {
        
    }

    public void DeclineGameInvitePending(Mutation mutation) {
        
    }

    public void DeclineGameInviteSuccess(Mutation mutation) {
        
    }

    public void DeclineGameInviteError(Mutation mutation) {
        
    }

    public void StartGamePending(Mutation mutation) {
        
    }

    public void StartGameSuccess(Mutation mutation) {
        
    }

    public void StartGameError(Mutation mutation) {
        
    }

    public void GetGameHeaderPending(Mutation mutation) {
        
    }

    public void GetGameHeaderSuccess(Mutation mutation) {
        
    }

    public void GetGameHeaderError(Mutation mutation) {
        
    }

    public void GetGameStatePending(Mutation mutation) {
        
    }

    public void GetGameStateSuccess(Mutation mutation) {
        
    }

    public void GetGameStateError(Mutation mutation) {
        
    }

    public void SendRollDicePending(Mutation mutation) {
        
    }

    public void SendRollDiceSuccess(Mutation mutation) {
        
    }

    public void SendRollDiceError(Mutation mutation) {
        
    }

    public void MovePiecePending(Mutation mutation) {
        
    }

    public void MovePieceSuccess(Mutation mutation) {
        
    }

    public void MovePieceError(Mutation mutation) {
        
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
