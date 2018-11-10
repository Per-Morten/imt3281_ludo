package no.ntnu.imt3281.ludo.gui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import no.ntnu.imt3281.ludo.client.ActionConsumer;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import no.ntnu.imt3281.ludo.client.State;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;


/**
 * Controls FXML Controllers
 */
public class MutationConsumer {
    private final ArrayBlockingQueue<Mutation> mIncommingMutations = new ArrayBlockingQueue<Mutation>(100);
    private ActionConsumer mActionConsumer;
    private FXMLLoader mLoginFile;
    private FXMLLoader mLudoFile;
    private FXMLLoader mGameBoardFile;
    private ExecutorService mCommitListener = Executors.newSingleThreadExecutor();
    private State mState;
    /**
     * Setup initial state, then listen for mutations
     * @param primaryStage
     */
    public void run(Stage primaryStage, State state) {
        mState = state;

        mLoginFile = new FXMLLoader(getClass().getResource("../gui/Login.fxml"));
        mLudoFile = new FXMLLoader(getClass().getResource("../gui/Ludo.fxml"));
        mGameBoardFile = new FXMLLoader(getClass().getResource("../gui/GameBoard.fxml"));

        // Handle close window
        primaryStage.setOnCloseRequest((WindowEvent e) -> {
            Platform.exit();
        });

        // Set initial scene root
        AnchorPane loginPane = new AnchorPane();
        try {
            loginPane = mLoginFile.load();
        } catch (IOException e) {
            Logger.log(Level.ERROR, "IOException loading .fxml file:" + e.getCause());
        }
        var root = new Scene(loginPane);
        primaryStage.setScene(root);
        primaryStage.show();

        LoginController loginController = mLoginFile.getController();
        loginController.bind(mActionConsumer);

        mCommitListener.execute(() -> {
            boolean running = true;
            while (running) {
                try {
                    Mutation mutation = mIncommingMutations.take();
                    mutation.run(MutationConsumer.this);
                } catch (InterruptedException e) {
                    running = false;
                    Logger.log(Level.ERROR, "InterruptedException when commiting mutation to MutationConsumer: " + e.getCause());
                }
            }
        });
    }

    /**
     * Commit new mutations to GUI
     * @param mutation
     */
    public void commit(Mutation mutation) {
        try {
            mIncommingMutations.put(mutation);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Bind dependencies
     * @param actionConsumer
     */
    public void bind(ActionConsumer actionConsumer) {
        mActionConsumer = actionConsumer;
    }

    /**
     * Mutation
     * Display network error, do nothing
     */
    public void networkError() {

    }

    /**
     * Display logging in...
     */
    public void loginPending() {
        this.startMutation("loginPending");
        LoginController loginController = mLoginFile.getController();

        for (int i = 0; i < 360; ++i) {
            int time = i;
            try {
                Platform.runLater(() -> loginController.mRectangle.setRotate(time));

                Thread.sleep(2);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * @mutation
     */
    public void loginSuccess() {
        this.startMutation("loginSuccess");

    }

    /**
     * @mutation
     */
    public void loginError() {
        this.startMutation("loginError");
    }


    /**
     * @mutation
     */
    public void LogoutPending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void LogoutSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void LogoutError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void GetUserPending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void GetUserSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void GetUserError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void CreateUserPending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void CreateUserSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void CreateUserError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void UpdateUserPending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void UpdateUserSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void UpdateUserError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void DeleteUserPending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void DeleteUserSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void DeleteUserError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void GetFriendPending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void GetFriendSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void GetFriendError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void FriendPending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void FriendSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void FriendError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void UnfriendPending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void UnfriendSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void UnfriendError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void JoinChatPending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void JoinChatSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void JoinChatError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void LeaveChatPending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void LeaveChatSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void LeaveChatError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void GetChatPending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void GetChatSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void GetChatError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void CreateChatPending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void CreateChatSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void CreateChatError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void SendChatMessagePending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void SendChatMessageSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void SendChatMessageError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void SendChatInvitePending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void SendChatInviteSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void SendChatInviteError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void CreateGamePending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void CreateGameSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void CreateGameError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void JoinGamePending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void JoinGameSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void JoinGameError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void LeaveGamePending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void LeaveGameSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void LeaveGameError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void SendGameInvitePending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void SendGameInviteSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void SendGameInviteError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void DeclineGameInvitePending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void DeclineGameInviteSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void DeclineGameInviteError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void StartGamePending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void StartGameSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void StartGameError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void GetGameHeaderPending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void GetGameHeaderSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void GetGameHeaderError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void GetGameStatePending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void GetGameStateSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void GetGameStateError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void SendRollDicePending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void SendRollDiceSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void SendRollDiceError() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void MovePiecePending() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void MovePieceSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void MovePieceError() {
        this.startMutation("");
    }

    /**
     * Log action level info
     *
     * @param methodName name of callee
     */
    private void startMutation(String methodName) {
        Logger.log(Logger.Level.INFO, "Mutation -> " + methodName);
    }

}
