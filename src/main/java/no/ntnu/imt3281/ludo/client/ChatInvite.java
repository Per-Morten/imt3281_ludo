package no.ntnu.imt3281.ludo.client;

import org.json.JSONObject;

import static no.ntnu.imt3281.ludo.api.FieldNames.CHAT_ID;
import static no.ntnu.imt3281.ludo.api.FieldNames.USER_ID;

public class ChatInvite {

    public int chatId = 0;
    public int userId = 0;
    public String chatName = "";
    public String userName = "";

    public boolean removed = false;

    ChatInvite(JSONObject json) {
        chatId = json.getInt(CHAT_ID);
        userId = json.getInt(USER_ID);
    }
}
