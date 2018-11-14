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

    private static final String filename = "client-cache.json";

    /**
     * Deep copy of cache
     *
     * @param cache to be copied
     * @return copy
     */
    public static Cache deepCopy(Cache cache) {
        var copy = new Cache();
        copy.authToken = cache.authToken;
        copy.username = cache.username;
        copy.email = cache.email;
        copy.userId = cache.userId;

        copy.gameId.addAll(cache.gameId);
        copy.clientId.addAll(cache.clientId);
        copy.friendId.addAll(cache.friendId);
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
                state.authToken = json.getString("auth_token");
                state.username = json.getString("username");
                state.email = json.getString("email");
                state.userId = json.getInt("user_id");
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
        json.put("auth_token", cache.authToken);
        json.put("username", cache.username);
        json.put("email", cache.email);
        json.put("user_id", cache.userId);
        try (var writer = new FileWriter(Cache.filename)) {
            writer.write(json.toString());
        } catch (IOException e) {
            Logger.log(Logger.Level.WARN, "Failed to write to " + Cache.filename + " : " + e.getCause().toString());
        }
    }
}