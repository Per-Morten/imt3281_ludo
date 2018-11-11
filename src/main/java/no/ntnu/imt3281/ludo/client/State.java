package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.common.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * Holds all state of client
 */
public class State {
     public String token = "";
     public String username = "";
     public String email = "";

     private static final String filename = "client-cache.json";

    /**
     * Deep copy of state
     *
     * @param state to be copied
     * @return copy
     */
     public static State deepCopy(State state) {
         var copy = new State();
         copy.token = state.token;
         return copy;
     }

    /**
     * Load state object from client-cache.json
     *
     * @return created state
     */
     static State load() {

        var state = new State();

        try  {
            String text = new String(Files.readAllBytes(Paths.get(State.filename)), StandardCharsets.UTF_8);
            var json = new JSONObject(text);
            try {
                state.token = json.getString("token");
                state.username = json.getString("username");
                state.email = json.getString("email");
            } catch (JSONException e) {
                Logger.log(Logger.Level.WARN, "Missing key in " + State.filename);
            }
        } catch (IOException e) {
            Logger.log(Logger.Level.INFO, "Failed to load " + State.filename);
        }
        return state;
    }

    /**
     * Dump state object into a client-cache.json file
     *
     * @param state
     */
    static void dump(State state) {

        var json = new JSONObject();
        json.put("token", state.token);
        json.put("username", state.username);
        json.put("email", state.email);

        try(var writer = new FileWriter(State.filename)) {
            writer.write(json.toString());
        } catch (IOException e) {
            Logger.log(Logger.Level.WARN, "Failed to write to "  + State.filename +  " : " + e.getCause().toString());
        }
    }
}