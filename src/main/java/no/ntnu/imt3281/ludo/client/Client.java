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

    private final String mAddress = "localhost";
    private final int mPort = 9010;
    private ExecutorService mExecutorService = Executors.newFixedThreadPool(1);
    private final ActionConsumer mActionConsumer = new ActionConsumer();
    private final MutationConsumer mMutationConsumer = new MutationConsumer();
    private final MessageConsumer mMessageConsumer = new MessageConsumer();
    private SocketManager mSocketManager;

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

        // Set up Socket
        try {
            mSocketManager = new SocketManager(InetAddress.getByName(mAddress), mPort);
        } catch (UnknownHostException e) {
            Logger.log(Level.ERROR, "UnknownHostException on new SocketManager: " + e.toString());
        }
        try {
            mSocketManager.start();
        } catch (IOException e) {
            Logger.log(Level.ERROR, "IOException on SocketMAnager.start()): " + e.toString());
        }

        // Bind consumer dependencies
        mActionConsumer.bind(mMutationConsumer, mSocketManager);
        mMessageConsumer.bind(mMutationConsumer, mSocketManager);
        mMutationConsumer.bind(mActionConsumer);

        // Action consumer runs in its own thread
        mExecutorService.execute(mActionConsumer);

        // Mutation consumer runs on the FXML thread
        mMutationConsumer.run(primaryStage);
    }

    /**
     * FXML exit point
     */
    @Override
    public void stop() {
        mExecutorService.shutdownNow();
        try {
            mSocketManager.stop();
        } catch (InterruptedException e) {
            Logger.log(Level.ERROR, "InterruptedException when trying to stop mSocketManager: " + e.toString());
        }
    }
}
