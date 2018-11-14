package no.ntnu.imt3281.ludo.client;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import javafx.stage.WindowEvent;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;
import no.ntnu.imt3281.ludo.gui.Transitions;

/**
 * Main class for the client.
 */
public class Client extends Application {

    private ExecutorService mExecutorService = Executors.newFixedThreadPool(2);
    private StateManager mStateManager;
    private SocketManager mSocketManager;

    final Transitions mTransitions = new Transitions();
    final API mApi = new API();
    final Actions mActions = new Actions();

    /**
     * Client entry point
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Logger.setLogLevel(Level.DEBUG);
        launch(args);
    }

    /**
     * FXML entry point
     * 
     * @param primaryStage fxml window context
     */
    @Override
    public void start(Stage primaryStage) {
        Logger.log(Level.INFO, "Starting FXML context");

        // Set up Socket
        try {
            mSocketManager = new SocketManager(InetAddress.getByName("localhost"), 9010);
        } catch (UnknownHostException e) {
            Logger.log(Level.WARN, "Could not create socket: " + e.toString());
        }

        // Bind dependencies between important systems.
        State initialState = State.load();
        mStateManager = new StateManager(initialState);
        mApi.bind(mSocketManager);
        mActions.bind(mTransitions, mApi, mStateManager);
        mTransitions.bind(primaryStage, mActions);

        // Start socket
        try {
            mSocketManager.start();
        } catch (IOException|NullPointerException e) {
            Logger.log(Level.WARN, "Could not create connection: " + e.toString());
        }

        // Handle close window
        primaryStage.setOnCloseRequest((WindowEvent e) -> {
            Platform.exit();
        });

        mTransitions.redirect("Login.fxml");
    }

    /**
     * FXML exit point
     */
    @Override
    public void stop() {
        Logger.log(Level.INFO, "Stopping FXML context");

        mExecutorService.shutdownNow();

        try {
            mSocketManager.stop();
        } catch (InterruptedException|NullPointerException e) {
            Logger.log(Level.WARN, "Trying to stop non-existing connection: " + e.toString());
        }
        State.dump(mStateManager.copy());
    }
}
