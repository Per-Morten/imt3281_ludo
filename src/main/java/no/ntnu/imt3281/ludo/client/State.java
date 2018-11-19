package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.api.FieldNames;
import no.ntnu.imt3281.ludo.common.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Holds all state of client
 */
public class State {
    //
    // Persistent state - stored in file between client sessions
    //
    public String authToken = "";
    public String username = "";
    public String email = "";
    public int userId = -1;
    public String avatarURI = "@../../../../../images/basic-avatar.png";

    //
    // In-memory state- cleared on every startup
    //
    public Map<Integer, JSONObject> activeGames = new HashMap<>();
    public Map<Integer, JSONObject> activeChats = new HashMap<>();
    public Map<Integer, JSONObject> gameInvites = new HashMap<>();
    public Map<Integer, JSONObject> chatInvites = new HashMap<>();

    private static final String filepath = "client-state.json";

    static State shallowCopy(State state) {
        var copy = new State();
        copy.authToken = state.authToken;
        copy.username = state.username;
        copy.email = state.email;
        copy.userId = state.userId;
        copy.avatarURI = state.avatarURI;

        copy.activeChats.putAll(state.activeChats);
        copy.activeChats.putAll(state.activeChats);
        copy.gameInvites.putAll(state.gameInvites);
        copy.chatInvites.putAll(state.chatInvites);

        return copy;
    }

    /**
     * Load state object from client-state.json
     *
     * @return created state
     */
    static State load() {

        var state = new State();

        try {
            String text = new String(Files.readAllBytes(Paths.get(State.filepath)), StandardCharsets.UTF_8);
            var json = new JSONObject(text);
            try {
                state.authToken = json.getString(FieldNames.AUTH_TOKEN);
                state.username = json.getString(FieldNames.USERNAME);
                state.email = json.getString(FieldNames.EMAIL);
                state.userId = json.getInt(FieldNames.USER_ID);
                state.avatarURI = json.getString(FieldNames.AVATAR_URI);
            } catch (JSONException e) {
                Logger.log(Logger.Level.WARN, "Missing key in " + State.filepath);
            }
        } catch (IOException e) {
            Logger.log(Logger.Level.INFO, "Failed to load " + State.filepath);
        }
        return state;
    }

    /**
     * Dump state object into a client-state.json file
     *
     * @param state
     */
    static void dump(State state) {

        var json = new JSONObject();
        json.put(FieldNames.AUTH_TOKEN, state.authToken);
        json.put(FieldNames.USERNAME, state.username);
        json.put(FieldNames.EMAIL, state.email);
        json.put(FieldNames.USER_ID, state.userId);
        json.put(FieldNames.AVATAR_URI, state.avatarURI);

        try (var writer = new FileWriter(State.filepath)) {
            writer.write(json.toString());
        } catch (IOException e) {
            Logger.log(Logger.Level.WARN, "Failed to write to " + State.filepath + " : " + e.getCause().toString());
        }
    }
}