package no.ntnu.imt3281.ludo.client;

import javafx.application.Platform;
import no.ntnu.imt3281.ludo.api.FieldNames;
import no.ntnu.imt3281.ludo.api.GlobalChat;
import no.ntnu.imt3281.ludo.common.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

/**
 * Manage state in a thread safe manner.
 */
public class StateManager {

    private static final String filepath = "client-state.json";
    private ArrayBlockingQueue<State> mState = new ArrayBlockingQueue<>(1);
    private EncryptDecrypt mEncryptDecrypt = new EncryptDecrypt();

    /**
     * Get a copy of the userId
     *
     * @return userid logged in user
     */
    int getUserId() {
        int copy = -1;
        try {
            State state = mState.take();
            copy = state.userId;
            mState.put(state);
        } catch (InterruptedException e) {
            Platform.exit();
        }
        return copy;
    }

    /**
     * Get a copy of the auth token
     *
     * @return auth token of logged in user
     */
    String getAuthToken() {
        String copy = "";
        try {
            State state = mState.take();
            copy = state.authToken;
            mState.put(state);
        } catch (InterruptedException e) {
            Platform.exit();
        }
        return copy;
    }

    /**
     * Full deep copy of the current state. Blocking in case ongoing mutation
     *
     * @return state object
     */
    public State copy() {
        State copy = new State();
        try {
            State state = mState.take();
            copy = State.shallowCopy(state);
            mState.put(state);
        } catch (InterruptedException e) {
            Platform.exit();
        }
        return copy;
    }


    /**
     * Mutate the state with provided mutation. Block all incoming copies until
     * mutation is completed.
     *
     * @param mutation which should be applied on state
     */
    public void commit(Mutation mutation) {
        try {
            State state = mState.take();

            try {
                mutation.run(state);
            } catch (Exception e) {
                Logger.logException(Logger.Level.WARN, e, "Exception in mutation");
                mState.put(state);
                return;
            }
            mState.put(state);
        } catch (InterruptedException e) {
            Platform.exit();
        }
    }

    /**
     * Load state object from client-state.json
     */
    void load() throws InterruptedException {

        var state = new State();
        try {
            String encryptedText = new String(Files.readAllBytes(Paths.get(StateManager.filepath)),
                    StandardCharsets.UTF_8);
            String text = mEncryptDecrypt.decrypt(encryptedText);

            var json = new JSONObject(text);
            try {
                state = State.fromJson(json);
            } catch (JSONException e) {
                Logger.log(Logger.Level.WARN, "Missing key in " + StateManager.filepath);
            }
        } catch (Exception e) {
            Logger.log(Logger.Level.INFO, "Failed to load " + StateManager.filepath);
        }
        mState.put(state);
    }

    /**
     * Dump state object into a client-state.json file
     */
    void dump() {
        var state = this.copy();

        var json = State.toJson(state);

        try (var writer = new FileWriter(StateManager.filepath)) {

            var plainText = json.toString();
            var encryptedText = mEncryptDecrypt.encrypt(plainText);
            writer.write(encryptedText);
        } catch (Exception e) {
            Logger.logException(Logger.Level.WARN, e, "Failed to write to " + StateManager.filepath);
        }
    }

    /**
     * Reset state to initial state. Should always result in the same state
     */
    void reset() {
        try {
            State state = mState.take();
            State.reset(state);
            mState.put(state);
        } catch (InterruptedException e) {
            Platform.exit();
        }
    }

    // Filter based on search
    public ArrayList<Game> filteredGames() {
        var copy = this.copy();

        return this.copy().activeGames
                .values()
                .stream()
                .filter(game -> game.name.toUpperCase()
                        .contains(copy.searchGames.toUpperCase()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<GameInvite> filteredGameInvites() {
        var copy = this.copy();

        return copy.gameInvites
                .values()
                .stream()
                .filter(gameInv -> gameInv.gameName.toUpperCase()
                        .contains(copy.searchGames.toUpperCase()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Chat> filteredChats() {
        var copy = this.copy();

        return copy.activeChats
                .values()
                .stream()
                .filter(chat -> chat.name.toUpperCase()
                        .contains(copy.searchChats.toUpperCase()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<ChatInvite> filteredChatInvites() {
        var copy = this.copy();

        return copy.chatInvites
                .values()
                .stream()
                .filter(chatInv -> chatInv.chatName.toUpperCase()
                        .contains(copy.searchChats.toUpperCase()))
                .collect(Collectors.toCollection(ArrayList::new));

    }
}
