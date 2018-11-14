package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.api.APIFunctions;
import no.ntnu.imt3281.ludo.api.Response;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;


public class API {
    private final ArrayBlockingQueue<Request> mPendingRequests = new ArrayBlockingQueue<Request>(1);
    private SocketManager mSocketManager;

    /**
     * Bind API dependencies
     *
     * @param socketManager need to do networking
     */
    public void bind(SocketManager socketManager) {
        mSocketManager = socketManager;
        mSocketManager.setOnReceiveCallback(this::read);
    }

    /**
     * Send request to server and block as long as there is a request pending.
     *
     * @param request to be sent to server
     */
    public void send(Request request) {

        boolean success = mPendingRequests.offer(request);
        if (!success) {
            Logger.log(Logger.Level.WARN, "Still waiting for the previous request. Cannot send more than 1 request at a time");
            return;
        }

        try {
            mSocketManager.send(request.toJSON().toString());
        } catch (NullPointerException|IOException e) {
            Logger.log(Level.WARN, "No connection with server: " + e.toString());
            try {
                mPendingRequests.take();
                // ... Request failed, so we throw it away, to avoid blocking new attempts.
            } catch (InterruptedException e2) {
                Logger.log(Logger.Level.INFO, "mPendingRequests.take() interrupted" + e.toString());
            }
        }
    }

    /**
     * Read a message from socket. Determine if the message is a responseType or eventType message.
     * Parse message to json.
     *
     * @param message string from socket
     */
    private void read(String message) {

        Logger.log(Level.DEBUG, "Got a response: " + message);

        var jsonResponse = new JSONObject();
        try {
            jsonResponse = new JSONObject(message);
        } catch (JSONException e) {
            Logger.log(Level.WARN, "Exception when parsing JSON" + e.toString());
            return;
        }

        String snake_case_message_type = new String();
        try {
            snake_case_message_type = jsonResponse.getString("type");
        } catch (JSONException e) {
            Logger.log(Level.ERROR, "JSONException when parsing message type" + e.toString());
        }

        String PascalCasedMessageType = APIFunctions.fromSnakeCase(snake_case_message_type);

        if (APIFunctions.isResponseType(PascalCasedMessageType)) {
            this.handleResponse(jsonResponse);
        } else if (APIFunctions.isEventType(PascalCasedMessageType)) {
            this.handleEvent(jsonResponse);
        } else {
            Logger.log(Level.WARN, "Unkown message type " + PascalCasedMessageType + " from " + message);
        }
    }

    /**
     * Handle responseType json
     *
     * @param jsonResponse response as json
     */
    private void handleResponse(JSONObject jsonResponse) {

        Request request = mPendingRequests.poll();

        if (request == null) {
            Logger.log(Level.ERROR, "No request found when handling response");
            return;
        }

        var response = new Response();
        try {
            response = APIFunctions.makeResponse(jsonResponse);
        } catch (JSONException e) {
            Logger.log(Level.ERROR, "JSONException when parsing response" + e.toString());
        }

        if (request.id != response.id) {
            Logger.log(Level.ERROR, "requestId != responseId Request id and response id should match");
        }

        response.success.forEach(successItem -> {

            var requestItem = new JSONObject();
            for (int i = 0; i < request.payload.size(); ++i) {
                var reqItem = request.payload.get(i);
                if (reqItem.getInt("id") == successItem.getInt("id")) {
                    requestItem = reqItem;
                    break;
                }
            };
            request.onSuccess.run(requestItem, successItem);
        });

        response.error.forEach(errorItem -> {

            var requestItem = new JSONObject();
            for (int i = 0; i < request.payload.size(); ++i) {
                var reqItem = request.payload.get(i);
                if (reqItem.getInt("id") == errorItem.getInt("id")) {
                    requestItem = reqItem;
                    break;
                }
            };
            request.onError.run(requestItem, errorItem);
        });
    }

    /**
     * Handle evenType json
     *
     * @param event event as json
     */
    private void handleEvent(JSONObject event) {}
}