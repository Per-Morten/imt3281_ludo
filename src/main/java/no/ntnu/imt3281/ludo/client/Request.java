package no.ntnu.imt3281.ludo.client;

import java.util.ArrayList;

import no.ntnu.imt3281.ludo.api.FieldNames;
import org.json.JSONObject;

import no.ntnu.imt3281.ludo.api.RequestType;

/**
 * POD object
 */
public class Request {
    public int id = -1;
    public String token = "";
    public RequestType type;
    public ArrayList<JSONObject> payload;
    public RequestCallback onSuccess;
    public RequestCallback onError;

    public JSONObject toJSON() {

        var json = new JSONObject();
        json.put(FieldNames.ID, id);
        json.put(FieldNames.AUTH_TOKEN, token);
        json.put(FieldNames.TYPE, type.toLowerCaseString());
        json.put(FieldNames.PAYLOAD, payload);

        return json;
    }

    public JSONObject toJSONWithoutToken() {

        var json = new JSONObject();
        json.put(FieldNames.ID, id);
        json.put(FieldNames.TYPE, type.toLowerCaseString());
        json.put(FieldNames.PAYLOAD, payload);

        return json;
    }
}
