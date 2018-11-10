package no.ntnu.imt3281.ludo.gui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
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
    private State mIntermediateState;
    private State mCommitedState;
    private Stage mPrimaryStage;

    /**
     * Make a synchronized copy of the current state
     *
     * @return current state
     */
    public synchronized State getCurrentState() {
        return mCommitedState;
    }


    /**
     * Bind dependencies
     * @param actionConsumer
     */
    public void bind(Stage primaryStage, ActionConsumer actionConsumer) {
        mPrimaryStage = primaryStage;
        mActionConsumer = actionConsumer;
    }

    /**
     * Setup initial state, then listen for mutations
     *
     * @param initialState
     */
    public void run(State initialState) {
        mCommitedState = initialState;
        mIntermediateState = initialState;

        mLoginFile = new FXMLLoader(getClass().getResource("../gui/Login.fxml"));
        mLudoFile = new FXMLLoader(getClass().getResource("../gui/Ludo.fxml"));
        mGameBoardFile = new FXMLLoader(getClass().getResource("../gui/GameBoard.fxml"));

        // Handle close window
        mPrimaryStage.setOnCloseRequest((WindowEvent e) -> {
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
        mPrimaryStage.setScene(root);
        mPrimaryStage.show();

        LoginController loginController = mLoginFile.getController();
        loginController.bind(mActionConsumer);

        this.runConsumeMutations();
    }

    /**
     * Feed new mutations to consumer
     *
     * @param mutation to be executed
     */
    public void feed(Mutation mutation) {
        try {
            mIncommingMutations.put(mutation);
        } catch (InterruptedException e) {
            Logger.log(Level.INFO, "Commit interrupted");
        }
    }

    /**
     * Display logging in...
     */
    public void loginPending() {
        this.startMutation("loginPending");

        this.toastPending("Logging in..."); // TODO i18n
        LoginController loginController = mLoginFile.getController();

        for (int i = 0; i < 360; ++i) {
            int time = i;
            Platform.runLater(() -> loginController.mRectangle.setRotate(time));
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * Log in user, display main windows
     */
    public void loginSuccess() {
        this.startMutation("loginSuccess");

        /*
            // Set initial scene root
            AnchorPane ludoPane = new AnchorPane();
            try {
                ludoPane = mLudoFile.load();
            } catch (IOException e) {
                Logger.log(Level.ERROR, "IOException loading .fxml file:" + e.getCause());
            }
            var root = new Scene(ludoPane);
            mPrimaryStage.setScene(root);
            mPrimaryStage.show();
           */
    }

    /**
     * Register user
     */
    public void createUserPending() {
        this.startMutation("createUserPending");

    }

    /**
     */
    public void CreateUserSuccess() {
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
    public void GetUserSuccess() {
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
    public void DeleteUserSuccess() {
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
    public void FriendSuccess() {
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
    public void JoinChatSuccess() {
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
    public void GetChatSuccess() {
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
    public void SendChatMessageSuccess() {
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
    public void CreateGameSuccess() {
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
    public void LeaveGameSuccess() {
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
    public void DeclineGameInviteSuccess() {
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
    public void GetGameSuccess() {
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
    public void SendRollDiceSuccess() {
        this.startMutation("");
    }


    /**
     * @mutation
     */
    public void MovePieceSuccess() {
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


    /**
     * Display pending toast
     *
     * @param message info
     */
    private void toastPending(String message) {
        int toastMsgTime = 1500;
        int fadeInTime = 100;
        int fadeOutTime= 100;
        Toast.makeText(mPrimaryStage, message, toastMsgTime, fadeInTime, fadeOutTime, Color.color((float)0x62/0xff, (float)0x00/0xff, (float)0xEE/0xff));
    }

    /**
     * Display pending toast
     *
     * @param message info
     */
    private void toastError(String message) {
        int toastMsgTime = 1500;
        int fadeInTime = 100;
        int fadeOutTime= 100;
        Toast.makeText(mPrimaryStage, message, toastMsgTime, fadeInTime, fadeOutTime, Color.color((float)0xB0 / 0xff, (float)0x00/0xff, (float)0x20/0xff));
    }

    private void runConsumeMutations() {
        mCommitListener.execute(() -> {
            boolean running = true;
            while (running) {
                try {
                    Mutation mutation = mIncommingMutations.take();
                    mutation.run(MutationConsumer.this);
                    mCommitedState = State.deepCopy(mIntermediateState);
                } catch (InterruptedException e) {
                    running = false;
                    Logger.log(Level.ERROR, "InterruptedException when commiting mutation to MutationConsumer: " + e.getCause());
                }
            }
        });
    }
}
