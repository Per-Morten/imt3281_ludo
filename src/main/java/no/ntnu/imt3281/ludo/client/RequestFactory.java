package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.api.RequestType;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Makes requests in accordance to the message protocol
 * Makes sure every request has a unique id
 * Makes sure every item in payload has a unique id
 */
public class RequestFactory {

    public Request make(RequestType type, JSONObject payload, String token, RequestCallback onSuccess, RequestCallback onError) {

        var req = new Request();
        req.id = this.nextRequestId();
        req.type = type;
        req.token = token;
        req.onSuccess = onSuccess;
        req.onError = onError;

        var array = new ArrayList<JSONObject>();
        payload.put("id", 0);
        array.add(payload);
        req.payload = array;
        return req;
    }

    public Request make(RequestType type,  ArrayList<JSONObject> payload, String token, RequestCallback onSuccess, RequestCallback onError) {

        int i = 0;
        var req = new Request();
        req.id = this.nextRequestId();
        req.type = type;
        req.token = token;
        req.onSuccess = onSuccess;
        req.onError = onError;

        for (var item : payload) {
            item.put("id", i++);
        }

        req.payload = payload;
        return req;
    }

    private int mRequestIncrementer = 0;

    synchronized private int nextRequestId() {
        mRequestIncrementer += 1;
        return mRequestIncrementer;
    }
}