package no.ntnu.imt3281.ludo.client;

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
    public Map<Integer, JSONObject> activeGames = new HashMap<Integer, JSONObject>();
    public Map<Integer, JSONObject> activeChats = new HashMap<Integer, JSONObject>();

    // Search scene
    public Set<Integer> selectedGames = new HashSet<Integer>();
    public Set<Integer> selectedChats = new HashSet<Integer>();
    public Set<Integer> selectedFriends = new HashSet<Integer>();
    public Set<Integer> selectedUsers = new HashSet<Integer>();

    public ArrayList<JSONObject> gameRange = new ArrayList<JSONObject>();
    public ArrayList<JSONObject> chatRange = new ArrayList<JSONObject>();
    public ArrayList<JSONObject> friendRange = new ArrayList<JSONObject>();
    public ArrayList<JSONObject> userRange = new ArrayList<JSONObject>();


    private static final String filename = "client-state.json";

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
            String text = new String(Files.readAllBytes(Paths.get(State.filename)), StandardCharsets.UTF_8);
            var json = new JSONObject(text);
            try {
                // Persistent cache
                state.authToken = json.getString("auth_token");
                state.username = json.getString("username");
                state.email = json.getString("email");
                state.userId = json.getInt("user_id");
                // Persistent cache

            } catch (JSONException e) {
                Logger.log(Logger.Level.WARN, "Missing key in " + State.filename);
            }
        } catch (IOException e) {
            Logger.log(Logger.Level.INFO, "Failed to load " + State.filename);
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
        json.put("auth_token", state.authToken);
        json.put("username", state.username);
        json.put("email", state.email);
        json.put("user_id", state.userId);
        // Persistent state

        try (var writer = new FileWriter(State.filename)) {
            writer.write(json.toString());
        } catch (IOException e) {
            Logger.log(Logger.Level.WARN, "Failed to write to " + State.filename + " : " + e.getCause().toString());
        }
    }
}