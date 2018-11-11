package no.ntnu.imt3281.ludo.api;

import java.util.ArrayList;

import org.json.JSONObject;

public class Request {
    public String id;
    public String type;
    public String token;
    public ArrayList<JSONObject> payload;

    /**
     * Create a request in format specified in client-server-communication.md
     *
     * @param id unique id of request
     * @param type type of request
     * @param token authentication token
     * @param payload content of the request
     *
     * @return valid request object ready to be sent to server
     */
    public static JSONObject make(int id, RequestType type, String token, ArrayList<JSONObject> payload) {

        var json = new JSONObject();

        json.put("id", String.valueOf(id));
        json.put("type", APIFunctions.toSnakeCase(type));
        json.put("token", token);
        json.put("payload", payload);

        return json;
    }

    public static JSONObject makeItem(int id) {
        var item = new JSONObject();
        item.put("id", String.valueOf(id));
        return item;
    }
}