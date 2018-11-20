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
    public String password = "";
    public String email = "";
    public int userId = -1;
    public String avatarURI = "@../../../../../images/basic-avatar.png";

    public String searchGames = "";
    public String searchChats = "";
    public String searchFriends = "";
    public String searchUsers = "";

    //
    // In-memory state- cleared on every startup
    //
    public Map<Integer, JSONObject> activeGames = new HashMap<>();
    public Map<Integer, Chat> activeChats = new HashMap<>();
    public Map<Integer, JSONObject> gameInvites = new HashMap<>();
    public Map<Integer, JSONObject> chatInvites = new HashMap<>();


    static State shallowCopy(State state) {
        var copy = new State();
        copy.authToken = state.authToken;
        copy.username = state.username;
        copy.password = state.password;
        copy.email = state.email;
        copy.userId = state.userId;
        copy.avatarURI = state.avatarURI;

        copy.searchGames = state.searchGames;
        copy.searchChats = state.searchChats;
        copy.searchFriends = state.searchFriends;
        copy.searchUsers = state.searchUsers;

        copy.activeGames.putAll(state.activeGames);
        copy.activeChats.putAll(state.activeChats);
        copy.gameInvites.putAll(state.gameInvites);
        copy.chatInvites.putAll(state.chatInvites);

        return copy;
    }
}