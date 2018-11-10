package no.ntnu.imt3281.ludo.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * This is the main class for the server. **Note, change this to extend other
 * classes if desired.**
 *
 * @author
 *
 */
public class Server {

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(9010)) {
            boolean stopping = false;
            while (!stopping) {
                Socket s = server.accept();
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                try (br; bw) {
                    String remote = s.getInetAddress().getHostName();
                    int remotePort = s.getPort();
                    System.out.println(String.format("Connected from %s on port %d", remote, remotePort));
                    bw.write(String.format("Hello %s on port %d, please say something!%n", remote, remotePort));
                    bw.flush(); // Remember to flush!
                    String fromRemote;

                    while((fromRemote = br.readLine()) != null) {// This is a blocking method, will not return until a line is
                        // available.
                        System.out.println(String.format("Remote said: %s", fromRemote));
                        if (fromRemote.equals("STOP")) {
                            stopping = true;
                            System.out.println("Server is stopping");
                        }
                        bw.write(String.format("'%s' right back at you.%n", fromRemote));
                        bw.flush();
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                s.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}