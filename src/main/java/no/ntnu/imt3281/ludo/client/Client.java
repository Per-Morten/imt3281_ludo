package no.ntnu.imt3281.ludo.client;

import java.io.*;
import java.net.Socket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.stage.Stage;

import javax.net.SocketFactory;

import no.ntnu.imt3281.ludo.gui.MutationConsumer;

/**
 * This is the main class for the client. **Note, change this to extend other
 * classes if desired.
 */
public class Client extends Application {

    private final String mAddress = "localhost";
    private final int mPort = 9010;
    private Socket mSocket;
    private OutputStream mRequestOutputStream;
    private InputStream mResponseInputStream;
    private ExecutorService mLongRunningConsumers = Executors.newFixedThreadPool(3);
    private final ActionConsumer mActionConsumer = new ActionConsumer();
    private final MutationConsumer mMutationConsumer = new MutationConsumer();
    private final ResponseConsumer mResponseConsumer = new ResponseConsumer();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Set up Socket
        mSocket = SocketFactory.getDefault().createSocket(mAddress, mPort);
        mRequestOutputStream = mSocket.getOutputStream();
        mResponseInputStream = mSocket.getInputStream();

        // Bind consumer dependencies
        mActionConsumer.bind(mMutationConsumer, mRequestOutputStream);
        mResponseConsumer.bind(mMutationConsumer, mResponseInputStream);
        mMutationConsumer.bind(mActionConsumer);

        // Execute all consumers
        mLongRunningConsumers.execute(mActionConsumer);
        mLongRunningConsumers.execute(mResponseConsumer);
        mMutationConsumer.run(primaryStage);
    }

    @Override
    public void stop() throws IOException {
        mLongRunningConsumers.shutdownNow();
        mSocket.close();
    }
}
