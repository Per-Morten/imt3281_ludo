package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.common.Logger;
import org.json.JSONObject;

import java.util.ArrayList;

import static no.ntnu.imt3281.ludo.api.FieldNames.*;

public class Chat {

    public int id = -1;
    public String name = "";
    public ArrayList<Integer> participants = new ArrayList<>();
    public ArrayList<ChatMessage> messages = new ArrayList<>();

    public boolean removed = false;

    Chat(JSONObject json) {
        id = json.getInt(CHAT_ID);
        name = json.getString(NAME);
        for (var participant: json.getJSONArray(PARTICIPANT_ID)) {
            participants.add((int)participant);
        }
    }
}
