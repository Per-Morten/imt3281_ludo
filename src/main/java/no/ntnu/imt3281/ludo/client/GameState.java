package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.api.ActionType;
import no.ntnu.imt3281.ludo.api.GameStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static no.ntnu.imt3281.ludo.api.FieldNames.*;


public class GameState {


    public int winner = -1;
    public int gameId = -1;
    public int currentPlayerId = -1;
    public int previousDiceThrow = -1;
    public GameStatus status = GameStatus.IN_LOBBY;
    public ActionType nextAction = ActionType.UNKNOWN;
    public ArrayList<Integer> playerOrder = new ArrayList<>(4);
    public ArrayList<ArrayList<Integer>> piecePositions = new ArrayList<>();

    public ArrayList<String> playerNames = new ArrayList<>(4);

    GameState() {
    }
    GameState(JSONObject json) {

        gameId = json.getInt(GAME_ID);
        currentPlayerId = json.getInt(CURRENT_PLAYER_ID);
        nextAction = ActionType.fromInt(json.getInt(NEXT_ACTION));
        previousDiceThrow = json.getInt(PREVIOUS_DICE_THROW);
        status = GameStatus.fromInt(json.getInt(STATUS));
        winner = json.getInt(WINNER);

        var playerOrderJson = json.getJSONArray(PLAYER_ORDER);
        for (var playerIdObj: playerOrderJson) {
            int playerId = (int)playerIdObj;
            playerOrder.add(playerId);
        }

        int i = 0;
        var piecePositionsJson = json.getJSONArray(PIECE_POSITIONS);
        for (var colorPositionObj: piecePositionsJson) {
            piecePositions.add(new ArrayList<>(4));
            var colorPositions = (JSONArray)colorPositionObj;
            for (var positionObj: colorPositions) {
                var position = (int)positionObj;
                piecePositions.get(i).add(position);
            }
            i++;
        }
    }
}
