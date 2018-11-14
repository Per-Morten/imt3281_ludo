package no.ntnu.imt3281.ludo.client;

import java.util.ArrayList;

import no.ntnu.imt3281.ludo.api.RequestType;
import no.ntnu.imt3281.ludo.client.Request;
import no.ntnu.imt3281.ludo.client.RequestCallback;
import org.json.JSONObject;

/**
 * Makes requests in accordance to the message protocol
 * Makes sure every request has a unique id
 * Makes sure every item in payload has a unique id
 */
public class RequestFactory {

    public Request make(RequestType _type, ArrayList<JSONObject> _payload, RequestCallback _onSuccess, RequestCallback _onError) {

        var req = new Request();
        req.id = this.nextRequestId();
        req.type = _type;
        req.onSuccess = _onSuccess;
        req.onError = _onError;

        _payload.forEach(item -> item.put("id", this.nextItemId()));

        req.payload = _payload;
        return req;
    }

    public Request make(RequestType _type, ArrayList<JSONObject> _payload, RequestCallback _onSuccess, RequestCallback _onError, String _token) {

        var req = new Request();
        req.id = this.nextRequestId();
        req.type = _type;
        req.token = _token;
        req.onSuccess = _onSuccess;
        req.onError = _onError;

        _payload.forEach(item -> item.put("id", this.nextItemId()));

        req.payload = _payload;
        return req;
    }

    public Request make(RequestType _type, JSONObject _payload, RequestCallback _onSuccess, RequestCallback _onError) {

        var req = new Request();
        req.id = this.nextRequestId();
        req.type = _type;
        req.onSuccess = _onSuccess;
        req.onError = _onError;

        var array = new ArrayList<JSONObject>();
        _payload.put("id", this.nextItemId());
        array.add(_payload);
        req.payload = array;
        return req;

    }

    public Request make(RequestType _type, JSONObject _payload, String _token, RequestCallback _onSuccess, RequestCallback _onError) {

        var req = new Request();
        req.id = this.nextRequestId();
        req.type = _type;
        req.token = _token;
        req.onSuccess = _onSuccess;
        req.onError = _onError;

        var array = new ArrayList<JSONObject>();
        _payload.put("id", this.nextItemId());
        array.add(_payload);
        req.payload = array;
        return req;
    }

    private int mItemIncrementer = 0;
    private int mRequestIncrementer = 0;

    synchronized private int nextItemId() {
        mItemIncrementer += 1;
        return mItemIncrementer;
    }

    synchronized private int nextRequestId() {
        mRequestIncrementer += 1;
        return mRequestIncrementer;
    }

}