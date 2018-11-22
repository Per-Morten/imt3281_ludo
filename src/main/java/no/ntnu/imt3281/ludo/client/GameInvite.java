package no.ntnu.imt3281.ludo.client;

import org.json.JSONObject;

import static no.ntnu.imt3281.ludo.api.FieldNames.GAME_ID;
import static no.ntnu.imt3281.ludo.api.FieldNames.USER_ID;

public class GameInvite {
    public int gameId = -1;
    public int userId = -1;
    public String gameName = "";
    public String userName = "";

    GameInvite(JSONObject json) {
        gameId = json.getInt(GAME_ID);
        userId = json.getInt(USER_ID);
    }
}
