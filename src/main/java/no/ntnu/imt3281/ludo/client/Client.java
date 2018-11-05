package no.ntnu.imt3281.ludo.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

/**
 * 
 * This is the main class for the client. **Note, change this to extend other
 * classes if desired.**
 * 
 * @author
 *
 */
public class Client extends Application {

    ExecutorService mExecutor = Executors.newCachedThreadPool();

    @Override
    public void start(Stage primaryStage) throws Exception {

        var actions = new Actions();
        var mutations = new Mutations();
        var messageListener = new MessageListener();

        mExecutor.execute(actions);
        mExecutor.execute(mutations);
        mExecutor.execute(messageListener);

        AnchorPane root = (AnchorPane) FXMLLoader.load(getClass().getResource("../gui/Ludo.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Handle close window
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                Platform.exit();
            }
        });

        // Handle escape key
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode() == KeyCode.ESCAPE) {
                    Platform.exit();
                }
            }
        });
    }

    @Override
    public void stop() {
        mExecutor.shutdownNow();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
