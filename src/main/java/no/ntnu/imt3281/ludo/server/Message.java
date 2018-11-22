package no.ntnu.imt3281.ludo.server;

import org.json.JSONObject;

import java.util.List;

public class Message {
    public List<Integer> receivers;
    public JSONObject object;

    public Message() {

    }

    public Message(JSONObject object, List<Integer> receivers) {
        this.object = object;
        this.receivers = DeepCopy.copy(receivers);
    }

}
