package no.ntnu.imt3281.ludo.api;

import org.json.JSONObject;

import java.util.ArrayList;

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
        json.put("id", id);
        json.put("token", token);
        json.put("type", APIFunctions.toSnakeCase(type));
        json.put("payload", payload);

        return json;
    }
}
