package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.api.FieldNames;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Holds all state of client
 */
public class State {
    //
    // Persistent state - stored in file between client sessions
    //
    public String username = "";
    public String password = "";
    public String email = "";
    public int userId = -1;
    public String avatarURI = "@../../../../../images/basic-avatar.png";


    //
    // In-memory state- cleared on every startup
    //
    public String authToken = "";

    public String searchGames = "";
    public String searchChats = "";
    public String searchFriends = "";
    public String searchUsers = "";

    public Map<Integer, Game> activeGames = new HashMap<>();
    public Map<Integer, GameState> activeGameStates = new HashMap<>();
    public Map<Integer, Chat> activeChats = new HashMap<>();

    public Map<Integer, GameInvite> gameInvites = new HashMap<>();
    public Map<Integer, ChatInvite> chatInvites = new HashMap<>();


    static State fromJson(JSONObject json) throws JSONException {
        var state = new State();

        state.username = json.getString(FieldNames.USERNAME);
        state.password = json.getString(FieldNames.PASSWORD);
        state.email = json.getString(FieldNames.EMAIL);
        state.userId = json.getInt(FieldNames.USER_ID);

        return state;
    }

    static JSONObject toJson(State state) {
        var json = new JSONObject();

        json.put(FieldNames.USERNAME, state.username);
        json.put(FieldNames.PASSWORD, state.password);
        json.put(FieldNames.EMAIL, state.email);
        json.put(FieldNames.USER_ID, state.userId);
        json.put(FieldNames.AVATAR_URI, state.avatarURI);

        return json;
    }

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
        copy.activeGameStates.putAll(state.activeGameStates);

        copy.gameInvites.putAll(state.gameInvites);
        copy.chatInvites.putAll(state.chatInvites);

        return copy;
    }

    static void reset(State state) {
        state.userId = -1;
        state.email = "";
        state.authToken = "";
        state.username = "";
        state.password = "";
        state.avatarURI = "";

        state.searchUsers = "";
        state.searchChats = "";
        state.searchFriends = "";
        state.searchGames = "";

        state.activeChats.clear();
        state.activeGames.clear();
        state.activeGameStates.clear();

        state.chatInvites.clear();
        state.gameInvites.clear();
    }
}