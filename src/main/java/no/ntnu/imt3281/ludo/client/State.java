package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.api.Request;
import no.ntnu.imt3281.ludo.api.Response;
import no.ntnu.imt3281.ludo.common.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Holds all state of client
 */
public class State {
     public String token = "";

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

        var json = new JSONObject();
        var state = new State();

        try (var reader = new FileReader(State.filename)) {
            json = new JSONObject(reader.read());

            state.token = json.getString("token");

        } catch (IOException e) {
            Logger.log(Logger.Level.INFO, "Failed to load" + State.filename);
        } catch (JSONException e) {
            Logger.log(Logger.Level.INFO, "Missing token in" + State.filename);
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

        try(var writer = new FileWriter(State.filename)) {
            writer.write(json.toString());
        } catch (IOException e) {
            Logger.log(Logger.Level.INFO, "Failed to write to "  + State.filename);
        }
    }
}