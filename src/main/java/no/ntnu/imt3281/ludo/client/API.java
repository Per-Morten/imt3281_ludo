package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.api.*;
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
        new Thread(() -> {
            try {
                mPendingRequests.put(request);
            } catch(InterruptedException e) {
                Logger.log(Logger.Level.INFO, "mPendingRequests.put() interrupted" + e.toString());
                return;
            }

            try {
                Logger.log(Level.DEBUG, "Sending request: " + request.toJSON().toString());
                mSocketManager.send(request.toJSON().toString());
            } catch (NullPointerException | IOException e) {
                Logger.log(Level.WARN, "No connection with server: " + e.toString());
                mPendingRequests.poll(); // Throw away request to avoid blocking
            }
        }).start();
    }

    /**
     * Same as above, but without token.
     *
     * @param request to be sent to server
     */
    public void sendNoToken(Request request) {
        new Thread(() -> {
            try {
                mPendingRequests.put(request);
            } catch(InterruptedException e) {
                Logger.log(Logger.Level.INFO, "mPendingRequests.put() interrupted" + e.toString());
                return;
            }

            try {
                Logger.log(Level.DEBUG, "Sending request: " + request.toJSON().toString());
                mSocketManager.send(request.toJSONWithoutToken().toString());
            } catch (NullPointerException | IOException e) {
                Logger.log(Level.WARN, "No connection with server: " + e.toString());
                mPendingRequests.poll(); // Throw away request to avoid blocking
            }
        }).start();
    }


    /**
     * Read a message from socket. Determine if the message is a responseType or eventType message.
     * Parse message to json.
     *
     * @param message string from socket
     */
    private void read(String message) {

        Logger.log(Level.DEBUG, "Got response|event: " + message);

        var jsonResponse = new JSONObject();
        try {
            jsonResponse = new JSONObject(message);
        } catch (JSONException e) {
            Logger.log(Level.WARN, "JSONException when jsonifying message" + e.toString());
            mPendingRequests.poll(); // Throw away request to avoid blocking
            return;
        }

        String messageType;
        try {
            messageType = jsonResponse.getString("type");
        } catch (JSONException e) {
            Logger.log(Level.WARN, "JSONException when parsing 'type':" + e.toString());
            mPendingRequests.poll(); // Throw away request to avoid blocking
            return;
        }

        ResponseType reqType = ResponseType.fromString(messageType);
        EventType eventType = EventType.fromString(messageType);

        if (reqType == null && eventType == null){
            Logger.log(Level.WARN, "Unkown message type: " + messageType);
            mPendingRequests.poll(); // Throw away request to avoid blocking
            return;
        }

        // Handle response or event
        if (reqType != null) {
            this.handleResponse(jsonResponse);
        } else {
            this.handleEvent(jsonResponse);
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
            Logger.log(Level.ERROR, "No matching request found when handling response");
            return;
        }

        Response response;
        try {
            response = Response.fromJSON(jsonResponse);
        } catch (JSONException e) {
            Logger.log(Level.WARN, "JSONException when parsing response: " + e.toString());
            return;
        }

        if (request.id != response.id) {
            Logger.log(Level.ERROR, "requestId != responseId Request id and response id should match");
            return;
        }

        response.success.forEach(successItem -> {
            Logger.log(Level.DEBUG, response.type.toLowerCaseString() + " -> success: " + successItem.toString());
            request.onSuccess.run(successItem);
        });

        response.error.forEach(errorItem -> {
            Logger.log(Level.DEBUG, response.type.toLowerCaseString() + " -> error: " + errorItem.toString());
            request.onError.run(errorItem);
        });
    }

    /**
     * Handle evenType json
     *
     * @param event event as json
     */
    private void handleEvent(JSONObject event) {
        Request request = mPendingRequests.poll();

    }
}
