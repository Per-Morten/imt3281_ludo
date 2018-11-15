package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.common.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds all state of client
 */
public class Cache {
    // Persistent cache
    public String authToken = "";
    public String username = "";
    public String email = "";
    public int userId = -1;

    // In memory cache
    public ArrayList<Integer> gameId = new ArrayList<Integer>();
    public ArrayList<Integer> clientId = new ArrayList<Integer>();
    public ArrayList<Integer> friendId = new ArrayList<Integer>();

    // In Search
    public Set<Integer> selectedGames = new HashSet<Integer>();
    public Set<Integer> selectedChats = new HashSet<Integer>();
    public Set<Integer> selectedFriends = new HashSet<Integer>();
    public Set<Integer> selectedUsers = new HashSet<Integer>();

    public ArrayList<JSONObject> gameRange = new ArrayList<JSONObject>();
    public ArrayList<JSONObject> chatRange = new ArrayList<JSONObject>();
    public ArrayList<JSONObject> friendRange = new ArrayList<JSONObject>();
    public ArrayList<JSONObject> userRange = new ArrayList<JSONObject>();


    private static final String filename = "client-cache.json";

    /**
     * Deep copy of cache
     *
     * @param cache to be copied
     * @return copy
     */
    public static Cache deepCopy(Cache cache) {
        var copy = new Cache();
        
        // Persistent cache
        copy.authToken = cache.authToken;
        copy.username = cache.username;
        copy.email = cache.email;
        copy.userId = cache.userId;

        // In memory cache
        copy.gameId.addAll(cache.gameId);
        copy.clientId.addAll(cache.clientId);
        copy.friendId.addAll(cache.friendId);

        copy.gameRange.addAll(cache.gameRange);
        copy.chatRange.addAll(cache.chatRange);
        copy.friendRange.addAll(cache.friendRange);
        copy.userRange.addAll(cache.userRange);
        
        copy.selectedGames.addAll(cache.selectedGames);
        copy.selectedChats.addAll(cache.selectedChats);
        copy.selectedFriends.addAll(cache.selectedFriends);
        copy.selectedUsers.addAll(cache.selectedUsers);

        return copy;
    }

    /**
     * Load state object from client-cache.json
     *
     * @return created state
     */
    static Cache load() {

        var state = new Cache();

        try {
            String text = new String(Files.readAllBytes(Paths.get(Cache.filename)), StandardCharsets.UTF_8);
            var json = new JSONObject(text);
            try {
                // Persistent cache
                state.authToken = json.getString("auth_token");
                state.username = json.getString("username");
                state.email = json.getString("email");
                state.userId = json.getInt("user_id");
                // Persistent cache

            } catch (JSONException e) {
                Logger.log(Logger.Level.WARN, "Missing key in " + Cache.filename);
            }
        } catch (IOException e) {
            Logger.log(Logger.Level.INFO, "Failed to load " + Cache.filename);
        }
        return state;
    }

    /**
     * Dump cache object into a client-cache.json file
     *
     * @param cache
     */
    static void dump(Cache cache) {

        var json = new JSONObject();
        // Persistent cache
        json.put("auth_token", cache.authToken);
        json.put("username", cache.username);
        json.put("email", cache.email);
        json.put("user_id", cache.userId);
        // Persistent cache

        try (var writer = new FileWriter(Cache.filename)) {
            writer.write(json.toString());
        } catch (IOException e) {
            Logger.log(Logger.Level.WARN, "Failed to write to " + Cache.filename + " : " + e.getCause().toString());
        }
    }
}