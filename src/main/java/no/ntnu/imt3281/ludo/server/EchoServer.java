package no.ntnu.imt3281.ludo.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @see https://www.oracle.com/webfolder/technetwork/tutorials/obe/java/SocketProgramming/files/EchoServer.txt
 */
public class EchoServer {

    public static void main(String[] args) throws IOException {

        try (ServerSocket serverSocket = new ServerSocket(9010);
             Socket clientSocket = serverSocket.accept();
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));) {
             System.out.println("Client connected on port " + 9010 +". Servicing requests.");
             String inputLine;
             while ((inputLine = in.readLine()) != null) {
                 System.out.println("Received message: " + inputLine + " from " + clientSocket.toString());
                 out.println(inputLine);
             }
        } catch (IOException e) {
             System.out.println("Exception caught when trying to listen on port "
                    + 9010 + " or listening for a connection");
        }
    }
}
