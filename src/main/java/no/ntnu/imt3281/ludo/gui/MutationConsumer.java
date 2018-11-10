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

import javafx.concurrent.Task;
/**
 * Controls FXML Controllers
 */
public class MutationConsumer {
    private final ArrayBlockingQueue<Mutation> mIncommingMutations = new ArrayBlockingQueue<Mutation>(100);
    private ActionConsumer mActionConsumer;
    private FXMLLoader mLoginFile;
    private FXMLLoader mLudoFile;
    private FXMLLoader mGameBoardFile;
    private ExecutorService mExecutor = Executors.newCachedThreadPool();
    /**
     * Setup initial state, then listen for mutations
     * @param primaryStage
     */
    public void run(Stage primaryStage) {

        mLoginFile = new FXMLLoader(getClass().getResource("../gui/Login.fxml"));
        mLudoFile = new FXMLLoader(getClass().getResource("../gui/Ludo.fxml"));
        mGameBoardFile = new FXMLLoader(getClass().getResource("../gui/GameBoard.fxml"));

        // Handle close window
        primaryStage.setOnCloseRequest((WindowEvent e) -> {
            Platform.exit();
            // TODO LOG ERROR
        });

        // Set initial scene root
        AnchorPane loginPane = new AnchorPane();
        try {
            loginPane = mLoginFile.load();
        } catch (IOException e) {
            e.printStackTrace();
            Platform.exit();
            // TODO LOG ERROR
        }
        var root = new Scene(loginPane);
        primaryStage.setScene(root);
        primaryStage.show();

        LoginController loginController = mLoginFile.getController();
        loginController.bind(mActionConsumer);

        mExecutor.execute(() -> {
            boolean running = true;
            while (running) {
                try {
                    Mutation mutation = mIncommingMutations.take();
                    mutation.run(MutationConsumer.this);
                } catch (InterruptedException e) {
                    running = false;
                    Platform.exit();
                    // TODO LOG ERROR
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
            System.out.println("Mutation incomming");
            // TODO LOG INFO
        } catch (InterruptedException e) {
            Platform.exit();
            // TODO LOG ERROR
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
     * @mutation
     * Display logging in...
     */
    public void loginPending() {
        LoginController loginController = mLoginFile.getController();

        mExecutor.execute(() -> {
            boolean running = true;
            int i = 0;
            int step = 8;
            while (running) {
                int time = i;
                try {
                    Platform.runLater(() -> loginController.mRectangle.setRotate(time));

                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    running = false;
                }
                i += step;
            }
        });
    }

    /**
     * @mutation
     */
    public void loginSuccess() {

    }

    /**
     * @mutation
     */
    public void loginError() {

    }


    /**
     * @mutation
     */
    public void LogoutPending() {

    }


    /**
     * @mutation
     */
    public void LogoutSuccess() {

    }


    /**
     * @mutation
     */
    public void LogoutError() {

    }


    /**
     * @mutation
     */
    public void GetUserPending() {

    }


    /**
     * @mutation
     */
    public void GetUserSuccess() {

    }


    /**
     * @mutation
     */
    public void GetUserError() {

    }


    /**
     * @mutation
     */
    public void CreateUserPending() {

    }


    /**
     * @mutation
     */
    public void CreateUserSuccess() {

    }


    /**
     * @mutation
     */
    public void CreateUserError() {

    }


    /**
     * @mutation
     */
    public void UpdateUserPending() {

    }


    /**
     * @mutation
     */
    public void UpdateUserSuccess() {

    }


    /**
     * @mutation
     */
    public void UpdateUserError() {

    }


    /**
     * @mutation
     */
    public void DeleteUserPending() {

    }


    /**
     * @mutation
     */
    public void DeleteUserSuccess() {

    }


    /**
     * @mutation
     */
    public void DeleteUserError() {

    }


    /**
     * @mutation
     */
    public void GetFriendPending() {

    }


    /**
     * @mutation
     */
    public void GetFriendSuccess() {

    }


    /**
     * @mutation
     */
    public void GetFriendError() {

    }


    /**
     * @mutation
     */
    public void FriendPending() {

    }


    /**
     * @mutation
     */
    public void FriendSuccess() {

    }


    /**
     * @mutation
     */
    public void FriendError() {

    }


    /**
     * @mutation
     */
    public void UnfriendPending() {

    }


    /**
     * @mutation
     */
    public void UnfriendSuccess() {

    }


    /**
     * @mutation
     */
    public void UnfriendError() {

    }


    /**
     * @mutation
     */
    public void JoinChatPending() {

    }


    /**
     * @mutation
     */
    public void JoinChatSuccess() {

    }


    /**
     * @mutation
     */
    public void JoinChatError() {

    }


    /**
     * @mutation
     */
    public void LeaveChatPending() {

    }


    /**
     * @mutation
     */
    public void LeaveChatSuccess() {

    }


    /**
     * @mutation
     */
    public void LeaveChatError() {

    }


    /**
     * @mutation
     */
    public void GetChatPending() {

    }


    /**
     * @mutation
     */
    public void GetChatSuccess() {

    }


    /**
     * @mutation
     */
    public void GetChatError() {

    }


    /**
     * @mutation
     */
    public void CreateChatPending() {

    }


    /**
     * @mutation
     */
    public void CreateChatSuccess() {

    }


    /**
     * @mutation
     */
    public void CreateChatError() {

    }


    /**
     * @mutation
     */
    public void SendChatMessagePending() {

    }


    /**
     * @mutation
     */
    public void SendChatMessageSuccess() {

    }


    /**
     * @mutation
     */
    public void SendChatMessageError() {

    }


    /**
     * @mutation
     */
    public void SendChatInvitePending() {

    }


    /**
     * @mutation
     */
    public void SendChatInviteSuccess() {

    }


    /**
     * @mutation
     */
    public void SendChatInviteError() {

    }


    /**
     * @mutation
     */
    public void CreateGamePending() {

    }


    /**
     * @mutation
     */
    public void CreateGameSuccess() {

    }


    /**
     * @mutation
     */
    public void CreateGameError() {

    }


    /**
     * @mutation
     */
    public void JoinGamePending() {

    }


    /**
     * @mutation
     */
    public void JoinGameSuccess() {

    }


    /**
     * @mutation
     */
    public void JoinGameError() {

    }


    /**
     * @mutation
     */
    public void LeaveGamePending() {

    }


    /**
     * @mutation
     */
    public void LeaveGameSuccess() {

    }


    /**
     * @mutation
     */
    public void LeaveGameError() {

    }


    /**
     * @mutation
     */
    public void SendGameInvitePending() {

    }


    /**
     * @mutation
     */
    public void SendGameInviteSuccess() {

    }


    /**
     * @mutation
     */
    public void SendGameInviteError() {

    }


    /**
     * @mutation
     */
    public void DeclineGameInvitePending() {

    }


    /**
     * @mutation
     */
    public void DeclineGameInviteSuccess() {

    }


    /**
     * @mutation
     */
    public void DeclineGameInviteError() {

    }


    /**
     * @mutation
     */
    public void StartGamePending() {

    }


    /**
     * @mutation
     */
    public void StartGameSuccess() {

    }


    /**
     * @mutation
     */
    public void StartGameError() {

    }


    /**
     * @mutation
     */
    public void GetGameHeaderPending() {

    }


    /**
     * @mutation
     */
    public void GetGameHeaderSuccess() {

    }


    /**
     * @mutation
     */
    public void GetGameHeaderError() {

    }


    /**
     * @mutation
     */
    public void GetGameStatePending() {

    }


    /**
     * @mutation
     */
    public void GetGameStateSuccess() {

    }


    /**
     * @mutation
     */
    public void GetGameStateError() {

    }


    /**
     * @mutation
     */
    public void SendRollDicePending() {

    }


    /**
     * @mutation
     */
    public void SendRollDiceSuccess() {

    }


    /**
     * @mutation
     */
    public void SendRollDiceError() {

    }


    /**
     * @mutation
     */
    public void MovePiecePending() {

    }


    /**
     * @mutation
     */
    public void MovePieceSuccess() {

    }


    /**
     * @mutation
     */
    public void MovePieceError() {

    }


}
