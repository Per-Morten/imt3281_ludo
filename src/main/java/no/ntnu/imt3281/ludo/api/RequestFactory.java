package no.ntnu.imt3281.ludo.api;

import java.util.ArrayList;

import org.json.JSONObject;

public class RequestFactory {
    /**
     * Create a request in format specified in client-server-communication.md
     *
     * @param type type of request
     * @param token authentication token
     * @param payload content of the request
     *
     * @return valid request object ready to be sent to server
     */
    public JSONObject makeRequest(RequestType type, String token, ArrayList<JSONObject> payload) {

        var json = new JSONObject();

        json.put("id", String.valueOf(this.nextItemId()));
        json.put("type", APIFunctions.toSnakeCase(type));
        json.put("token", token);
        json.put("payload", payload);

        return json;
    }

    /**
     * Make payload item
     *
     * @return payload item
     */
    public JSONObject makeItem() {
        var item = new JSONObject();
        item.put("id", String.valueOf(this.nextRequestId()));
        return item;
    }

    private int mItemIncrementer = 0;
    private int mRequestIncrementer = 0;

    private int nextItemId() {
        mItemIncrementer += 1;
        return mItemIncrementer;
    }

    private int nextRequestId() {
        mRequestIncrementer += 1;
        return mRequestIncrementer;
    }
}