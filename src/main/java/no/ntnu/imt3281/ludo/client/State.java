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
    // Persistent state
    public String authToken = "";
    public String username = "";
    public String email = "";
    public int userId = -1;


    //
    // In memory state
    //

    // Live scene
    public Map<Integer, JSONObject> activeGames = new HashMap<>();
    public Map<Integer, JSONObject> activeChats = new HashMap<>();

    // Search scene
    public Set<Integer> selectedGames = new HashSet<>();
    public Set<Integer> selectedChats = new HashSet<>();
    public Set<Integer> selectedFriends = new HashSet<>();
    public Set<Integer> selectedUsers = new HashSet<>();

    public ArrayList<JSONObject> gameRange = new ArrayList<>();
    public ArrayList<JSONObject> chatRange = new ArrayList<>();
    public ArrayList<JSONObject> friendRange = new ArrayList<>();
    public ArrayList<JSONObject> userRange = new ArrayList<>();


    private static final String filepath = "client-state.json";

    /**
     * Deep copy of state
     *
     * @param state to be copied
     * @return copy
     */
    public static State deepCopy(State state) {
        var copy = new State();
        
        // Persistent state
        copy.authToken = state.authToken;
        copy.username = state.username;
        copy.email = state.email;
        copy.userId = state.userId;

        // In memory state
        copy.activeGames.putAll(state.activeGames);
        copy.activeChats.putAll(state.activeChats);

        copy.gameRange.addAll(state.gameRange);
        copy.chatRange.addAll(state.chatRange);
        copy.friendRange.addAll(state.friendRange);
        copy.userRange.addAll(state.userRange);
        
        copy.selectedGames.addAll(state.selectedGames);
        copy.selectedChats.addAll(state.selectedChats);
        copy.selectedFriends.addAll(state.selectedFriends);
        copy.selectedUsers.addAll(state.selectedUsers);

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
                // Persistent cache
                state.authToken = json.getString(FieldNames.AUTH_TOKEN);
                state.username = json.getString(FieldNames.USERNAME);
                state.email = json.getString(FieldNames.EMAIL);
                state.userId = json.getInt(FieldNames.USER_ID);
                // Persistent cache

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
        // Persistent state
        json.put(FieldNames.AUTH_TOKEN, state.authToken);
        json.put(FieldNames.USERNAME, state.username);
        json.put(FieldNames.EMAIL, state.email);
        json.put(FieldNames.USER_ID, state.userId);
        // Persistent state

        try (var writer = new FileWriter(State.filepath)) {
            writer.write(json.toString());
        } catch (IOException e) {
            Logger.log(Logger.Level.WARN, "Failed to write to " + State.filepath + " : " + e.getCause().toString());
        }
    }
}