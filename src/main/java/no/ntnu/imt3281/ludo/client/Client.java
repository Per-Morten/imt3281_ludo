package no.ntnu.imt3281.ludo.client;

import java.io.*;
import java.net.Socket;

import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import no.ntnu.imt3281.ludo.gui.GameBoardController;
import no.ntnu.imt3281.ludo.gui.LoginController;
import no.ntnu.imt3281.ludo.gui.LudoController;

import javax.imageio.IIOException;
import javax.net.SocketFactory;

/**
 * This is the main class for the client. **Note, change this to extend other
 * classes if desired.
 */
public class Client extends Application {

	/**
	 * Starting long running threads
	 */
    private ExecutorService mLongRunningConsumers = Executors.newFixedThreadPool(3);

    private final String mAddress = "localhost";
    private final int mPort = 9010;
    private Socket mSocket;
    private OutputStream mRequestOutputStream;
    private InputStream mResponseInputStream;

    private final ActionConsumer mActionConsumer = new ActionConsumer();
    private final MutationConsumer mMutationConsumer = new MutationConsumer();
    private final ResponseConsumer mResponseConsumer = new ResponseConsumer();

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Load all fxml files and controllers
        var loginPane = new AnchorPane();
        var loginController = new LoginController();
        var ludoPane = new AnchorPane();
        var ludoController = new LudoController();

        try {
            var loader = new FXMLLoader(getClass().getResource("../gui/Login.fxml"));
            loginPane = loader.load();
            loginController = loader.getController();

            loader = new FXMLLoader(getClass().getResource("../gui/Ludo.fxml"));
            ludoPane = loader.load();
            ludoController = loader.getController();


        } catch (IOException e) {
            e.printStackTrace();
            Platform.exit();
        }


        // Set initial scene root
        var root = new Scene(loginPane);
        primaryStage.setScene(root);
        primaryStage.show();

        mSocket = SocketFactory.getDefault().createSocket(mAddress, mPort);
        mRequestOutputStream = mSocket.getOutputStream();
        mResponseInputStream = mSocket.getInputStream();

        PrintWriter writer = new PrintWriter(mRequestOutputStream, true);
        writer.println("Hello from client");

        // Execute long running consumers
        mLongRunningConsumers.execute(mActionConsumer);
        mLongRunningConsumers.execute(mMutationConsumer);
        mLongRunningConsumers.execute(mResponseConsumer);

        // Bind consumer dependencies
        mActionConsumer.bind(mMutationConsumer, mRequestOutputStream);
        mResponseConsumer.bind(mMutationConsumer, mResponseInputStream);
        mMutationConsumer.bind(mActionConsumer);
        loginController.bind(mActionConsumer);
        ludoController.bind(mActionConsumer);

        // Handle close window
        primaryStage.setOnCloseRequest((WindowEvent e) -> {
            Platform.exit();
        });
    }

    @Override
    public void stop() throws IOException {
        mLongRunningConsumers.shutdownNow();
        mSocket.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
