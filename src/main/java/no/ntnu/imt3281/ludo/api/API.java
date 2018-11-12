package no.ntnu.imt3281.ludo.api;

import no.ntnu.imt3281.ludo.api.APIFunctions;
import no.ntnu.imt3281.ludo.api.RequestFactory;
import no.ntnu.imt3281.ludo.api.RequestType;
import no.ntnu.imt3281.ludo.api.Response;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;
import no.ntnu.imt3281.ludo.gui.Transitions;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;


public class Messages {
    private final RequestFactory mRequestFactory = new RequestFactory();

    private Transitions mTransitions;
    private final ArrayBlockingQueue<JSONObject> mPendingRequests = new ArrayBlockingQueue<JSONObject>(1);

    void bind(Transitions transitions) {
        mTransitions = transitions;
    }

    public void feedRequest(JSONObject request) {
        try {
            mIncommingRequests.put(request);
        } catch (InterruptedException e) {
            Logger.log(Logger.Level.INFO, "InterruptedException when feeding request" + e.toString());
        }
    }
    public void sendRequest(RequestType type, ArrayList<JSONObject> payload) {
        this.sendRequest(type, payload, "");
    }

    public void sendRequest(RequestType type, ArrayList<JSONObject> payload, String token) {

        var request = mRequestFactory.makeRequest(RequestType.LoginRequest, token, payload);
        {

        }
    }

    public void feedMessage(String message) {
        Logger.log(Level.INFO, "Got a response: " + message);

        JSONObject request = mIncommingRequests.poll();
        if (request == null) {
            Logger.log(Level.WARN, "No request found when processing response in Messages");
            return;
        }

        var response = new Response();
        try {
            response = APIFunctions.makeResponse(message);
        } catch (JSONException e) {
            Logger.log(Level.WARN, "Exception when parsing JSON" + e.toString());
            return;
        } catch (IllegalArgumentException e) {
            Logger.log(Level.WARN, "Response type is invalid" + e.toString());
            return;
        } catch (ClassCastException e) {
            Logger.log(Level.WARN, "Woot" + e.toString());
            return;
        } catch (Exception e) {
            Logger.log(Level.WARN,  e.toString());
            return;
        }

    }
}