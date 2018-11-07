package no.ntnu.imt3281.ludo.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

/**
 *
 * This is the main class for the client.
 * **Note, change this to extend other classes if desired.**
 *
 * @author
 *
 */
//public class Client extends Application {
//
//	@Override
//	public void start(Stage primaryStage) {
//		try {
//			AnchorPane root = (AnchorPane)FXMLLoader.load(getClass().getResource("../gui/Ludo.fxml"));
//			Scene scene = new Scene(root);
//			primaryStage.setScene(scene);
//			primaryStage.show();
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static void main(String[] args) {
//		launch(args);
//	}
//}

public class Client{
    public static void main(String[] args) {
            try (Socket s = new Socket(InetAddress.getLocalHost(), 9010)) { // change the IP address to your server's ip address
                BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
                System.out.println("Server said: " + input.readLine());
                while (true){

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
