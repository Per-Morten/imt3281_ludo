package no.ntnu.imt3281.ludo.client;


import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.stage.Stage;

import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;
import no.ntnu.imt3281.ludo.gui.MutationConsumer;


/**
 * Main class for the client.
 * **Note, change this to extend other classes if desired.**
 *
 * @author
 *
 */
public class Client extends Application {

    private ExecutorService mExecutorService = Executors.newFixedThreadPool(2);
    private final ActionConsumer mActionConsumer = new ActionConsumer();
    private final MutationConsumer mMutationConsumer = new MutationConsumer();
    private final ResponseConsumer mResponseConsumer = new ResponseConsumer();
    private SocketManager mSocketManager;
    private State mState;
    /**
     * Client entry point
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Logger.setLogLevel(Level.DEBUG);
        launch(args);
    }

    /**
     * FXML entry point
     * @param primaryStage fxml window context
     */
    @Override
    public void start(Stage primaryStage)  {
        Logger.log(Level.INFO, "Starting FXML context");

        // Set up Socket
        try {
            mSocketManager = new SocketManager(InetAddress.getByName("localhost"), 9010);
        } catch (UnknownHostException e) {
            Logger.log(Level.ERROR, "UnknownHostException on new SocketManager: " + e.getCause().toString());
        }

        mState = State.load();

        // Bind consumer dependencies
        mActionConsumer.bind(mMutationConsumer, mResponseConsumer, mSocketManager);
        mResponseConsumer.bind(mMutationConsumer);
        mMutationConsumer.bind(primaryStage, mActionConsumer, mState);

        mSocketManager.setOnReceiveCallback(message -> mResponseConsumer.feedMessage(message));

        // Start socket
        try {
            mSocketManager.start();
        } catch (IOException e) {
            Logger.log(Level.ERROR, "IOException on SocketMAnager.start()): " + e.getCause().toString());
        }

        // Action consumer runs in its own thread
        mExecutorService.execute(mActionConsumer);
        mExecutorService.execute(mMutationConsumer);
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
        } catch (InterruptedException e) {
            Logger.log(Level.WARN, "InterruptedException when trying to stop mSocketManager");
        }
        State.dump(mState);
    }
}
