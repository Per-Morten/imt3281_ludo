package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.api.FieldNames;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import no.ntnu.imt3281.ludo.api.RequestType;

/**
 * Makes requests in accordance to the message protocol
 * Makes sure every request has a unique id
 * Makes sure every item in payload has a unique id
 */
class RequestFactory {
    private AtomicInteger mRequestIncrementer = new AtomicInteger();

     Request make(RequestType type, JSONObject payload, String token, RequestCallback onSuccess, RequestCallback onError) {

        var req = new Request();
        req.id = mRequestIncrementer.getAndAdd(1);
        req.type = type;
        req.token = token;
        req.onSuccess = onSuccess;
        req.onError = onError;

        var array = new ArrayList<JSONObject>();
        payload.put(FieldNames.ID, 0);
        array.add(payload);
        req.payload = array;
        return req;
    }

    Request make(RequestType type,  ArrayList<JSONObject> payload, String token, RequestCallback onSuccess, RequestCallback onError) {

        int i = 0;
        var req = new Request();
        req.id = mRequestIncrementer.getAndAdd(1);
        req.type = type;
        req.token = token;
        req.onSuccess = onSuccess;
        req.onError = onError;

        for (var item : payload) {
            item.put(FieldNames.ID, i++);
        }

        req.payload = payload;
        return req;
    }
}