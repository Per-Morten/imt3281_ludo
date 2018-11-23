package no.ntnu.imt3281.ludo.server;

import no.ntnu.imt3281.ludo.api.*;
import no.ntnu.imt3281.ludo.api.Error;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.logic.Ludo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * But for now, just lock on everything (Lock on all public facing calls, assume lock on others).
 * Use re-entrant lock.
 */
public class GameManager {
    private static class Game {
        int id;
        int ownerID;
        String name;
        ArrayList<Integer> players;
        ArrayList<Integer> pendingPlayers; // Players who have been invited but has not yet accepted.
        Ludo ludo;

        boolean allowRandoms;

        GameStatus status;

        int chatID;

        Game(int id, int ownerID, String name) {
            this(id, ownerID, name, 0);
        }

        Game(int id, int ownerID, String name, int chatID) {
            this(id, ownerID, name, false, chatID);
        }

        Game(int id, int ownerID, String name, boolean allowRandoms, int chatID) {
            this.id = id;
            this.ownerID = ownerID;
            players = new ArrayList<>();
            players.add(ownerID);

            pendingPlayers = new ArrayList<>();

            this.name = name;
            status = GameStatus.IN_LOBBY;
            this.allowRandoms = allowRandoms;
            this.chatID = chatID;
        }

        public void start() {
            ludo = new Ludo();
            players.forEach(i -> ludo.addPlayer(i));

            pendingPlayers.clear();
            status = GameStatus.IN_SESSION;
        }
    }

    private HashMap<Integer, Game> mGames = new HashMap<>();
    private int mNextGameID = 0;

    private ReentrantLock mLock = new ReentrantLock();

    private UserManager mUserManager;
    private ChatManager mChatManager;

    public GameManager(UserManager userManager, ChatManager chatManager) {
        mUserManager = userManager;
        mChatManager = chatManager;
    }

    /**
     * Creates a game, and gives the client back a game ID.
     * This function always succeeds (unless you are not authorized, but then you have already been removed)
     * <p>
     * TODO: Check that the name is well formed.
     *
     * @param requests  The incoming create game requests.
     * @param successes The array to store the successes in.
     * @param errors    The array to store the errors in.
     * @param events    Function will not lead to any events.
     */
    public void createGame(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        try (var lock = new LockGuard(mLock)) {
            applyFirstOrderFilter(RequestType.CREATE_GAME_REQUEST, requests, errors);

            MessageUtility.each(requests, (requestID, request) -> {
                var name = request.getString(FieldNames.NAME);
                var owner = request.getInt(FieldNames.USER_ID);
                var gameID = mNextGameID++;


                mGames.put(gameID, new Game(gameID, owner, name, mChatManager.createChatForGame(name)));

                var success = new JSONObject();
                success.put(FieldNames.GAME_ID, gameID);
                MessageUtility.appendSuccess(successes, requestID, success);
            });
        }
    }

    /**
     * Gets the meta information about the games indicated by the ID's in the requests.
     *
     * @param requests  The requests containing the game ids to get meta information of.
     * @param successes The array to store the successes in.
     * @param errors    The array to store the errors in.
     * @param events    Function will not lead to any events.
     */
    public void getGame(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        try (var lock = new LockGuard(mLock)) {
            applyFirstOrderFilter(RequestType.GET_GAME_REQUEST, requests, errors);

            MessageUtility.each(requests, (requestID, request) -> {
                var gameID = request.getInt(FieldNames.GAME_ID);
                var game = mGames.get(gameID);

                var success = new JSONObject();
                success.put(FieldNames.GAME_ID, gameID);
                success.put(FieldNames.PLAYER_ID, DeepCopy.copy(game.players));
                success.put(FieldNames.STATUS, game.status.toInt());
                success.put(FieldNames.OWNER_ID, game.ownerID);
                success.put(FieldNames.PENDING_ID, DeepCopy.copy(game.pendingPlayers));
                success.put(FieldNames.NAME, game.name);
                success.put(FieldNames.ALLOW_RANDOMS, game.allowRandoms);
                success.put(FieldNames.CHAT_ID, game.chatID);
                MessageUtility.appendSuccess(successes, requestID, success);
            });
        }
    }

    /**
     * Parses and executes the join game requests present in the requests JSONArray
     *
     * @param requests  The requests containing the game ids to try and join.
     * @param successes The array to store the successes in.
     * @param errors    The array to store the errors in.
     * @param events    Function will lead to GameUpdateEvent in the case where a player manages to join a game, which will be stored in this queue.
     */
    public void joinGame(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        try (var lock = new LockGuard(mLock)) {
            applyFirstOrderFilter(RequestType.JOIN_GAME_REQUEST, requests, errors);

            MessageUtility.each(requests, (requestID, request) -> {
                var gameID = request.getInt(FieldNames.GAME_ID);
                var game = mGames.get(gameID);

                if (game.players.size() >= Ludo.MAX_PLAYERS) {
                    MessageUtility.appendError(errors, requestID, Error.GAME_IS_FULL);
                    return;
                }

                if (game.status != GameStatus.IN_LOBBY) {
                    MessageUtility.appendError(errors, requestID, Error.GAME_ALREADY_STARTED);
                    return;
                }

                int userID = request.getInt(FieldNames.USER_ID);
                if (!game.pendingPlayers.contains(userID)) {
                    MessageUtility.appendError(errors, requestID, Error.NOT_INVITED_TO_GAME);
                    return;
                }

                putUserInGame(game, userID);
                events.add(createGameUpdateMessage(game));
                MessageUtility.appendSuccess(successes, requestID, new JSONObject());
            });
        }
    }

    /**
     * Removes users from any games requested.
     *
     * @param requests  The requests to remove users.
     * @param successes The array to store the successes in;
     * @param errors    The array to store the errors in.
     * @param events    Function will lead to GameUpdate event in the case where a player leaves the game,
     *                  additionally a GameStateUpdate event in the case where the game was in session.
     */
    public void leaveGame(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        try (var lock = new LockGuard(mLock)) {
            applyFirstOrderFilter(RequestType.LEAVE_GAME_REQUEST, requests, errors);

            MessageUtility.each(requests, (requestID, request) -> {
                var userID = request.getInt(FieldNames.USER_ID);
                var gameID = request.getInt(FieldNames.GAME_ID);
                var game = mGames.get(gameID);

                game.players.removeIf(item -> item == userID);
                game.pendingPlayers.removeIf(item -> item == userID);

                if (game.status == GameStatus.IN_SESSION) {
                    removePlayerFromActiveGame(game, userID, events);
                }

                if (game.players.size() < 1) {
                    mGames.remove(game.id);
                } else {
                    game.ownerID = game.players.get(0);
                    events.add(createGameUpdateMessage(game));
                }

                MessageUtility.appendSuccess(successes, requestID, new JSONObject());
            });
        }
    }

    /**
     * Starts the specified games in the requests.
     *
     * @param requests  The requests to start the games.
     * @param successes The array to store the successes in;
     * @param errors    The array to store the errors in.
     * @param events    Function will lead to GameStateUpdate event in the case where a game is started.
     */
    public void startGame(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        try (var lock = new LockGuard(mLock)) {
            applyFirstOrderFilter(RequestType.START_GAME_REQUEST, requests, errors);

            MessageUtility.each(requests, (requestID, request) -> {
                var userID = request.getInt(FieldNames.USER_ID);
                var gameID = request.getInt(FieldNames.GAME_ID);
                var game = mGames.get(gameID);

                if (userID != game.ownerID) {
                    MessageUtility.appendError(errors, requestID, Error.USER_IS_NOT_OWNER);
                    return;
                }

                if (game.status != GameStatus.IN_LOBBY) {
                    MessageUtility.appendError(errors, requestID, Error.GAME_ALREADY_STARTED);
                    return;
                }

                if (game.players.size() < Ludo.MIN_PLAYERS) {
                    MessageUtility.appendError(errors, requestID, Error.NOT_ENOUGH_PLAYERS);
                    return;
                }

                game.start();

                MessageUtility.appendSuccess(successes, requestID, new JSONObject());

                events.add(createGameStateUpdateMessage(game));
            });
        }
    }

    /**
     * Parses and executes all the invite to game requests.
     *
     * @param requests  The requests to invite specific users to specific games.
     * @param successes The array to store the successes in;
     * @param errors    The array to store the errors in.
     * @param events    Function will lead to GameUpdate event in the case where a someone is invited successfully.
     */
    public void inviteToGame(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        try (var lock = new LockGuard(mLock)) {
            applyFirstOrderFilter(RequestType.SEND_GAME_INVITE_REQUEST, requests, errors);

            MessageUtility.each(requests, (requestID, request) -> {
                var gameID = request.getInt(FieldNames.GAME_ID);
                var game = mGames.get(gameID);

                if (game.status != GameStatus.IN_LOBBY) {
                    MessageUtility.appendError(errors, requestID, Error.GAME_ALREADY_STARTED);
                }

                var otherID = request.getInt(FieldNames.OTHER_ID);

                if (game.players.contains(otherID) || game.pendingPlayers.contains(otherID)) {
                    MessageUtility.appendError(errors, requestID, Error.USER_ALREADY_INVITED_OR_IN_GAME);
                    return;
                }

                var userID = request.getInt(FieldNames.USER_ID);
                if (!mUserManager.areUsersFriends(userID, otherID)) {
                    MessageUtility.appendError(errors, requestID, Error.USER_IS_NOT_FRIEND);
                    return;
                }

                // Notify all the other players.
                game.pendingPlayers.add(otherID);
                events.add(createGameUpdateMessage(game));

                // Setup invite message
                var invite = new JSONObject();
                invite.put(FieldNames.USER_ID, userID);
                invite.put(FieldNames.GAME_ID, gameID);

                events.add(new Message(MessageUtility.createEvent(EventType.GAME_INVITE, invite), List.of(otherID)));

                MessageUtility.appendSuccess(successes, requestID, new JSONObject());
            });
        }
    }

    /**
     * Parses and executes all the RollDice requests.
     *
     * @param requests  The requests contaning the user that want to roll and the game ids they want to roll dice in.
     * @param successes The array to store the successes in;
     * @param errors    The array to store the errors in.
     * @param events    Function will lead to GameStateUpdate event in the case where someone rolls successfully.
     */
    public void rollDice(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        try (var lock = new LockGuard(mLock)) {
            applyFirstOrderFilter(RequestType.ROLL_DICE_REQUEST, requests, errors);

            MessageUtility.each(requests, (requestID, request) -> {
                var gameID = request.getInt(FieldNames.GAME_ID);
                var game = mGames.get(gameID);

                Logger.log(Logger.Level.DEBUG, "Received roll dice event");

                if (game.status != GameStatus.IN_SESSION) {
                    MessageUtility.appendError(errors, requestID, Error.GAME_NOT_IN_SESSION);
                    return;
                }

                var userID = request.getInt(FieldNames.USER_ID);
                if (userID != game.ludo.getCurrentPlayerID()) {
                    MessageUtility.appendError(errors, requestID, Error.OUT_OF_TURN);
                    return;
                }

                if (game.ludo.getNextAction() != ActionType.THROW_DICE) {
                    MessageUtility.appendError(errors, requestID, Error.NOT_TIME_TO_THROW_DICE);
                    return;
                }

                Logger.log(Logger.Level.DEBUG, "Passed all test, now we are rolling dice");

                game.ludo.throwDice();
                events.add(createGameStateUpdateMessage(game));
                MessageUtility.appendSuccess(successes, requestID, new JSONObject());
            });
        }
    }

    /**
     * Parses and executes all the MovePiece requests.
     *
     * @param requests  The requests contaning the user that want to roll and the game ids they want to roll dice in.
     * @param successes The array to store the successes in;
     * @param errors    The array to store the errors in.
     * @param events    Function will lead to GameStateUpdate event in the case where someone moves successfully.
     */
    public void movePiece(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        try (var lock = new LockGuard(mLock)) {
            applyFirstOrderFilter(RequestType.MOVE_PIECE_REQUEST, requests, errors);

            MessageUtility.each(requests, (requestID, request) -> {
                var gameID = request.getInt(FieldNames.GAME_ID);
                var game = mGames.get(gameID);

                if (game.status != GameStatus.IN_SESSION) {
                    MessageUtility.appendError(errors, requestID, Error.GAME_NOT_IN_SESSION);
                    return;
                }

                var userID = request.getInt(FieldNames.USER_ID);
                if (userID != game.ludo.getCurrentPlayerID()) {
                    MessageUtility.appendError(errors, requestID, Error.OUT_OF_TURN);
                    return;
                }

                if (game.ludo.getNextAction() != ActionType.MOVE_PIECE) {
                    MessageUtility.appendError(errors, requestID, Error.NOT_TIME_TO_MOVE_PIECE);
                    return;
                }

                var piece = request.getInt(FieldNames.PIECE_INDEX);
                if (piece >= Ludo.MAX_PLAYERS) {
                    MessageUtility.appendError(errors, requestID, Error.PIECE_INDEX_OUT_OF_BOUNDS);
                    return;
                }

                if (game.ludo.movePiece(userID, piece)) {
                    if (game.ludo.getWinner() != Ludo.UNASSIGNED) {
                        game.status = GameStatus.GAME_OVER;
                    }
                    events.add(createGameStateUpdateMessage(game));
                } else {
                    Logger.log(Logger.Level.DEBUG, "Could not move piece");
                }

                MessageUtility.appendSuccess(successes, requestID, new JSONObject());
            });
        }
    }

    /**
     * Parses and executes all the allow randoms requests.
     *
     * @param requests  JSONArray containing the requests to allow randoms to join the specified games.
     * @param successes The array to store the successes in;
     * @param errors    The array to store the errors in.
     * @param events    Function will lead to GameStateUpdate event in the case where someone moves successfully, which will be stored in events.
     */
    public void setAllowRandoms(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        try (var lock = new LockGuard(mLock)) {
            applyFirstOrderFilter(RequestType.SET_ALLOW_RANDOMS_REQUEST, requests, errors);

            MessageUtility.each(requests, (requestID, request) -> {
                var userID = request.getInt(FieldNames.USER_ID);
                var gameID = request.getInt(FieldNames.GAME_ID);
                var game = mGames.get(gameID);

                if (userID != game.ownerID) {
                    MessageUtility.appendError(errors, requestID, Error.USER_IS_NOT_OWNER);
                    return;
                }

                game.allowRandoms = request.getBoolean(FieldNames.ALLOW_RANDOMS);
                MessageUtility.appendSuccess(successes, requestID, new JSONObject());
            });
        }
    }

    /**
     * Tries to find a game that allows randoms to join, if such a game exists, the user in the request will be invited to that game,
     * If it does not exist a new game will be created with the allow randoms flag set to true, so that the next
     * player looking for a random game can join that game.
     *
     * @param requests  The join random game requests.
     * @param successes The JSONArray to store the successes in.
     * @param errors    The JSONArray to store the errors in.
     * @param events    Function will lead to a GameUpdate event in the case where someone moves successfully, which will be stored in events.
     */
    public void joinRandomGame(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        try (var lock = new LockGuard(mLock)) {
            applyFirstOrderFilter(RequestType.JOIN_GAME_REQUEST, requests, errors);

            MessageUtility.each(requests, (requestID, request) -> {
                var userID = request.getInt(FieldNames.USER_ID);
                var item = mGames.values().stream()
                        .filter(game -> {
                            return game.allowRandoms &&
                                    game.status == GameStatus.IN_LOBBY &&
                                    game.players.size() < Ludo.MAX_PLAYERS &&
                                    !game.players.contains(userID) &&
                                    !game.pendingPlayers.contains(userID);
                        })
                        .findFirst();

                Game game;
                if (item.isPresent()) {
                    game = item.get();
                    putUserInGame(game, userID);
                    events.add(createGameUpdateMessage(game));
                } else {
                    var id = mNextGameID++;
                    var name = String.format("Random game %d", id);
                    game = new Game(id, userID, name, true, mChatManager.createChatForGame(name));
                    mGames.put(id, game);
                }

                var success = new JSONObject();
                success.put(FieldNames.GAME_ID, game.id);
                MessageUtility.appendSuccess(successes, requestID, success);
            });
        }
    }

    /**
     * Reactive function to what should happen when a user logs out. Function will remove the user from all games.
     *
     * @param requests  The users who are requesting to log out.
     * @param successes Successes are ignored, as the GameManager cant fail to remove a user.
     * @param errors    Errors are ignored, as the GameManager cant fail to remove a user.
     * @param events    Function will lead to GameUpdate event if a user is removed from a game, and a GameStateUpdate if that game was in session.
     */
    public void onLogoutUser(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        try (var lock = new LockGuard(mLock)) {
            MessageUtility.each(requests, (requestID, request) -> {
                removeFromGames(request.getInt(FieldNames.USER_ID), events);
            });
        }
    }

    /**
     * Removes the specified user from all games they are present in
     *
     * @param userID The user to remove from games.
     * @param events Function will lead to GameUpdate event for the games that the user was removed from, and a GameStateUpdate if that game was in session.
     */
    public void removeFromGames(int userID, Queue<Message> events) {
        try (var lock = new LockGuard(mLock)) {

            mGames.values().removeIf(game -> {
                game.players.removeIf(item -> item == userID);
                game.pendingPlayers.removeIf(item -> item == userID);

                events.add(createGameUpdateMessage(game));

                if (game.status == GameStatus.IN_SESSION) {
                    removePlayerFromActiveGame(game, userID, events);
                }
                return game.players.size() < 1;
            });
        }
    }

    private void putUserInGame(Game game, int userID) {
        game.players.add(userID);
        game.pendingPlayers.removeIf(item -> item == userID);
    }

    /**
     * Removes a player from an active game, stopping the game (by setting the status to game over, and removing the ludo reference)
     * if there is not enough players left in the game.
     *
     * @param game   The game to remove a user from.
     * @param userID The user to remove from the game
     * @param events Where to put any events generated. If a user is removed from a game that is active, a GameStateUpdate will be issued.
     */
    private void removePlayerFromActiveGame(Game game, int userID, Queue<Message> events) {
        if (game.players.size() < Ludo.MIN_PLAYERS) {

            game.status = GameStatus.GAME_OVER;
            events.add(createGameStateUpdateMessage(game));
            game.ludo = null;
        } else {
            game.ludo.removePlayer(userID);
        }
    }

    /**
     * Applied the first order filter in the requests within requests, removes the erroneous ones and
     * appends the errors to the errors array.
     * This filter is just a basic filter to get rid of the following common errors:
     * * Trying to access a game that does not exist.
     * * Trying to "participate" in a game your are not a player in.
     *
     * @param type     The type of request that are stored in requests.
     * @param requests The actual requests themselves
     * @param errors   The JSONArray to put the errors in.
     */
    private void applyFirstOrderFilter(RequestType type, JSONArray requests, JSONArray errors) {
        try (var lock = new LockGuard(mLock)) {

            MessageUtility.applyFilter(requests, (id, request) -> {
                if (JSONValidator.hasInt(FieldNames.GAME_ID, request)) {
                    var game = mGames.get(request.getInt(FieldNames.GAME_ID));
                    if (game == null) {
                        MessageUtility.appendError(errors, id, Error.GAME_ID_NOT_FOUND);
                        return false;
                    }

                    if (JSONValidator.hasInt(FieldNames.USER_ID, request) && type != RequestType.JOIN_GAME_REQUEST && type != RequestType.GET_GAME_REQUEST) {
                        var userID = request.getInt(FieldNames.USER_ID);
                        if (!game.players.contains(userID) && !game.pendingPlayers.contains(userID)) {
                            MessageUtility.appendError(errors, id, Error.USER_NOT_IN_GAME);
                            return false;
                        }
                    }
                }

                return true;
            });
        }
    }

    private static Message createGameUpdateMessage(Game game) {
        var event = MessageUtility.createEvent(EventType.GAME_UPDATE);
        var gameID = new JSONObject();
        gameID.put(FieldNames.GAME_ID, game.id);
        var payload = event.getJSONArray(FieldNames.PAYLOAD);
        payload.put(gameID);

        var receivers = DeepCopy.copy(game.players);
        return new Message(event, receivers);
    }

    private static Message createGameStateUpdateMessage(Game game) {
        var event = MessageUtility.createEvent(EventType.GAME_STATE_UPDATE);
        var payload = event.getJSONArray(FieldNames.PAYLOAD);
        var gameState = new JSONObject();
        gameState.put(FieldNames.GAME_ID, game.id);
        gameState.put(FieldNames.PLAYER_ORDER, DeepCopy.copy(game.ludo.getPlayerOrder()));
        gameState.put(FieldNames.CURRENT_PLAYER_ID, game.ludo.getCurrentPlayerID());
        gameState.put(FieldNames.NEXT_ACTION, game.ludo.getNextAction().toInt());
        gameState.put(FieldNames.PREVIOUS_DICE_THROW, game.ludo.previousRoll());
        gameState.put(FieldNames.PIECE_POSITIONS, game.ludo.getPiecePositions());
        gameState.put(FieldNames.STATUS, game.status.toInt());
        gameState.put(FieldNames.WINNER, game.ludo.getWinner());

        payload.put(gameState);

        return new Message(event, game.players);
    }

}
