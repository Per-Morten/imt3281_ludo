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
     public String mToken = "";

     private static final String filename = "state.json";

    /**
     * Load state object from state.json
     *
     * @return created state
     */
     static State load() {

        var json = new JSONObject();
        var state = new State();

        try (var reader = new FileReader(State.filename)) {
            json = new JSONObject(reader.read());
            state.mToken = json.get("token").toString();

        } catch (IOException e) {
            Logger.log(Logger.Level.INFO, "Failed to load" + State.filename);
        } catch (JSONException e) {
            Logger.log(Logger.Level.INFO, "Missing token not found in" + State.filename);
        }
        return state;
    }

    /**
     * Dump state object into a state.json file
     *
     * @param state
     */
    static void dump(State state) {

        var json = new JSONObject();
        json.put("token", state.mToken);

        try(var writer = new FileWriter(State.filename)) {
            writer.write(json.toString());
        }catch (IOException e) {
            Logger.log(Logger.Level.INFO, "Failed to write to "  + State.filename);
        }
    }


}