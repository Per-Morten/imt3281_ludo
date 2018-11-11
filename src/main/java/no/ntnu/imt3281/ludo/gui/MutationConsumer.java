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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import no.ntnu.imt3281.ludo.client.State;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;


/**
 * Controls FXML Controllers
 */
public class MutationConsumer implements Runnable {
    private final ArrayBlockingQueue<Mutation> mIncommingMutations = new ArrayBlockingQueue<Mutation>(100);
    private ActionConsumer mActionConsumer;
    private ExecutorService mCommitListener = Executors.newSingleThreadExecutor();
    private State mIntermediateState;
    private State mCommitedState;
    private Stage mPrimaryStage;

    private HashMap<String, IGUIController> mControllers = new HashMap<>();

    /**
     * Make a synchronized copy of the current commited state
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
    public void bind(Stage primaryStage, ActionConsumer actionConsumer, State initialState) {
        mPrimaryStage = primaryStage;
        mActionConsumer = actionConsumer;
        mCommitedState = initialState;
        mIntermediateState = initialState;
    }

    /**
     * Implement the runnable interface
     */
    @Override
    public void run() {

        // Handle close window
        mPrimaryStage.setOnCloseRequest((WindowEvent e) -> {
            Platform.exit();
        });

        // Set initial scene root
        AnchorPane loginPane = this.loadFXML("Login.fxml");

        Platform.runLater(() -> {
            var root = new Scene(loginPane);
            mPrimaryStage.setScene(root);
            mPrimaryStage.show();
        });

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
    }

    /**
     * Feed mutation to consumer from other threads
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
     * Log in user, display main windows
     */
    public void loginSuccess() {
        this.startMutation("loginSuccess");

        var ludoPane = this.loadFXML("Ludo.fxml");
        this.redirect(ludoPane);
    }

    /**
     * Login failed display error
     */
    public void loginError() {
        this.startMutation("loginError");
        this.toastError("Username or password is wrong");
    }

    /**
     * Logout user and send back to login screen
     */
    public void logoutSuccess() {
        this.startMutation("logoutSuccess");

        var loginPane = this.loadFXML("Login.fxml");
        this.redirect(loginPane);
    }


    /**
     * Logout failed display error
     */
    public void logoutError() {
        this.startMutation("logoutError");
        this.toastError("Could not log out successfully. Try restarting the app");
    }

    /**
     */
    public void CreateUserSuccess() {
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

    private void startMutation(String methodName) {
        Logger.log(Logger.Level.INFO, "Mutation -> " + methodName);
    }


    private void toastPending(String message) {
        int toastMsgTime = 1500;
        int fadeInTime = 100;
        int fadeOutTime= 100;
        Toast.makeText(mPrimaryStage, message, toastMsgTime, fadeInTime, fadeOutTime, Color.color((float)0x62/0xff, (float)0x00/0xff, (float)0xEE/0xff));
    }

    private void toastError(String message) {
        int toastMsgTime = 1500;
        int fadeInTime = 100;
        int fadeOutTime= 100;
        Toast.makeText(mPrimaryStage, message, toastMsgTime, fadeInTime, fadeOutTime, Color.color((float)0xB0 / 0xff, (float)0x00/0xff, (float)0x20/0xff));
    }

    private AnchorPane loadFXML(String filename) {
        var root = new AnchorPane();
        var fxmlLoader = new FXMLLoader(getClass().getResource(filename));
        try {
            root = fxmlLoader.load();
            IGUIController guiController = fxmlLoader.getController();
            guiController.bind(mActionConsumer);
            mControllers.put(filename, guiController);
        } catch (IOException e) {
            Logger.log(Level.ERROR, "IOException loading .fxml file:" + e.getStackTrace());
        }
        return root;
    }

    private void redirect(AnchorPane rootPane) {
        var root = new Scene(rootPane);
        Platform.runLater(() -> {
            mPrimaryStage.setScene(root);
            mPrimaryStage.show();
        });
    }
}
