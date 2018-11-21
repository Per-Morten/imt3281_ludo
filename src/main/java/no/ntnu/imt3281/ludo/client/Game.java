package no.ntnu.imt3281.ludo.client;

import org.json.JSONObject;

import java.util.ArrayList;

import static no.ntnu.imt3281.ludo.api.FieldNames.*;

public class Game {
    public int id = -1;
    public String name = "";
    public int status = -1;
    public int ownerId = -1;
    public ArrayList<Integer> playerId = new ArrayList<>();
    public ArrayList<Integer> pendingId = new ArrayList<>();

    Game(){}
    Game(JSONObject json) {
        id = json.getInt(GAME_ID);
        name = json.getString(NAME);
        status = json.getInt(STATUS);
        ownerId = json.getInt(OWNER_ID);

        for (var plId: json.getJSONArray(PLAYER_ID)) {
            playerId.add((int)plId);
        }
        for (var pendId: json.getJSONArray(PENDING_ID)) {
            pendingId.add((int)pendId);
        }
    }
}