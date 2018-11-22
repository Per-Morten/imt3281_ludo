package no.ntnu.imt3281.ludo.client;

import org.json.JSONArray;
import org.json.JSONObject;

import static no.ntnu.imt3281.ludo.api.FieldNames.*;

public class ChatMessage {
    public int userId = -1;
    public int chatId = -1;
    public String message = "";
    public String username = "";

    ChatMessage(JSONObject json) {
        userId = json.getInt(USER_ID);
        chatId = json.getInt(CHAT_ID);
        message = json.getString(MESSAGE);
    }
}
