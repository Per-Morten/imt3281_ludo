package no.ntnu.imt3281.ludo.server;

import no.ntnu.imt3281.ludo.common.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
public class EchoServer {

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(9010)) {

            try (final Socket s = server.accept()) {

                final BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

                String remote = s.getInetAddress().getHostName();
                int remotePort = s.getPort();
                System.out.println(String.format("Connected from %s on port %d", remote, remotePort));

                String fromRemote;
                boolean stopping = false;
                while (!stopping) {
                    while ((fromRemote = br.readLine()) != null) {// This is a blocking method, will not return until a
                                                                  // line is
                        Logger.log(Logger.Level.DEBUG, "Reading line...");
                        // available.
                        System.out.println(String.format("Remote said: %s", fromRemote));

                        try {
                            JSONObject req = new JSONObject(fromRemote);

                            JSONObject res = new JSONObject();
                            res.put("id", req.getInt("id"));
                            res.put("type", req.getString("type").replace("request", "response"));

                            var successItem = new JSONObject();
                            successItem.put("id", 1);

                            var success = new JSONArray();
                            success.put(0, successItem);

                            var errorItem = new JSONObject();
                            errorItem.put("id", 2);

                            var error = new JSONArray();
                            error.put(0, errorItem);

                            res.put("success", success);
                            res.put("error", error);
                            try {
                                Logger.log(Logger.Level.INFO, "Response: " + res.toString());
                                bw.write(res.toString() + '\n');
                                bw.flush();
                            } catch (IOException e) {
                                Logger.log(Logger.Level.WARN, "Writing exception " + e.toString());
                            }

                        } catch (JSONException e) {
                            Logger.log(Logger.Level.WARN, "Request not correct JSON " + e.toString());
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}