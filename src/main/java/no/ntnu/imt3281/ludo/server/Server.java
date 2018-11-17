package no.ntnu.imt3281.ludo.server;

import no.ntnu.imt3281.ludo.api.Error;
import no.ntnu.imt3281.ludo.api.*;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.MessageUtility;
import no.ntnu.imt3281.ludo.common.NetworkConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

// Think:
// Have a hashmap with the response type mapped to a function
// Call those functions giving inn the payload, error and success objects.
//

/**
 *
 * This is the main class for the server. **Note, change this to extend other
 * classes if desired.**
 *
 * @author
 *
 */
public class Server {
    @FunctionalInterface
    interface EventHandler {
        public void apply(RequestType requestType, JSONArray requests, JSONArray successes, JSONArray errors);
    }

    private static LinkedBlockingQueue<SocketManager.Message> sPendingRequests = new LinkedBlockingQueue<>();
    private static Database sDB;
    private static AtomicBoolean sRunning = new AtomicBoolean();
    private static SocketManager sSocketManager = new SocketManager(NetworkConfig.LISTENING_PORT);
    private static UserManager sUserManager;

    private static HashMap<RequestType, EventHandler> mEventHandlers = new HashMap<>();

    private static String sDatabaseURL = "ludo.db";

    /**
     * Goldy locks number.
     * It is short enough that when shutting down the server, users don't really notice,
     * but long enough that the thread won't be spinning unnecessarily often.
     */
    private static long sPollTimeout = 250;

    private static void pushIncomingMessage(SocketManager.Message msg) {
        try {
            sPendingRequests.put(msg);
        } catch (InterruptedException e) {
            Logger.logException(Logger.Level.WARN, e, "Did not manage to put message in queue");
        }
    }



    // Currently only one thread is handling events on the server might want to fix
    // that in the future.
    private static void run() throws InterruptedException {
        while (sRunning.get()) {

            // Doing a timeout here, so that I can shutdown the server without ending in the situation where
            // the request is blocking.
            var message = sPendingRequests.poll(sPollTimeout, TimeUnit.MILLISECONDS);
            if (message == null) {
                continue;
            }

            // Think: Create general purpose API exception that can be thrown different
            // places, that can easily be turned into error code?
            if (!JSONValidator.isValidMessage(message.message)) {
                Logger.log(Logger.Level.WARN,
                        String.format("Message did not pass \"isValidMessage\" test, message: %s", message.message));
                continue;
            }

            var json = new JSONObject(message.message);
            var requestType = RequestType.fromString(json.getString(FieldNames.TYPE));

            var requests = json.getJSONArray(FieldNames.PAYLOAD);
            var successes = new JSONArray();
            var errors = new JSONArray();

            var response = new JSONObject();
            var requestID = json.getInt(FieldNames.ID);
            response.put(FieldNames.ID, requestID);
            response.put(FieldNames.TYPE, ResponseType.getCorrespondingResponse(requestType).toLowerCaseString());
            response.put(FieldNames.SUCCESS, successes);
            response.put(FieldNames.ERROR, errors);

            // Doing a filter of all things we aren't authorized to do.
            removeUnauthorizedRequests(json, errors);



                mEventHandlers.get(requestType).apply(requestType, requests, successes, errors);
                // Hack to deal with being able to map user id's to sockets.
                if (requestType == RequestType.LOGIN_REQUEST && message.socketID < 0) {
                    updateSocketIDs(message, successes);
                }

            sSocketManager.send(message.socketID, response.toString());
        }
    }

    private static void removeUnauthorizedRequests(JSONObject message, JSONArray errors) {
        if (!JSONValidator.hasString(FieldNames.AUTH_TOKEN, message)) {
            return;
        }
        var type = RequestType.fromString(message.getString(FieldNames.TYPE));

        // TODO: Add more here.
        if (type == RequestType.GET_USER_REQUEST || type == RequestType.GET_USER_RANGE_REQUEST) {
            return;
        }

        var token = message.getString(FieldNames.AUTH_TOKEN);
        var requests = message.getJSONArray(FieldNames.PAYLOAD);

        var indexesToRemove = new ArrayList<Integer>();

        for (int i = 0; i < requests.length(); i++) {
            var request = requests.getJSONObject(i);
            if (!sUserManager.isUserAuthorized(request, token)) {
                MessageUtility.appendError(errors, request.getInt(FieldNames.ID), Error.UNAUTHORIZED);
                indexesToRemove.add(i);
            }
        }

        for (var idx : indexesToRemove) {
            requests.remove(idx);
        }
    }

    private static void updateSocketIDs(SocketManager.Message message, JSONArray successes) {
        if (successes.length() >= 1) {
            var success = successes.getJSONObject(0);
            var id = success.getInt(FieldNames.USER_ID);
            sSocketManager.assignIdToSocket(message.socketID, id);
            message.socketID = (long)id;
        }
    }

    private static void setupEventHandlers() {

        mEventHandlers.put(RequestType.CREATE_USER_REQUEST, sUserManager::createUser);
        mEventHandlers.put(RequestType.DELETE_USER_REQUEST, sUserManager::deleteUser);
        mEventHandlers.put(RequestType.UPDATE_USER_REQUEST, sUserManager::updateUser);
        mEventHandlers.put(RequestType.GET_USER_REQUEST, sUserManager::getUser);
        mEventHandlers.put(RequestType.GET_USER_RANGE_REQUEST, sUserManager::getUserRange);
        // Remember to add GetUserRangeRequest

        mEventHandlers.put(RequestType.LOGIN_REQUEST, sUserManager::logInUser);

        // More probably needs to happen here, because we need to remove the user from several games as well.
        mEventHandlers.put(RequestType.LOGOUT_REQUEST, sUserManager::logOutUser);

    }

    // TODO: Find out how we shall terminate the server. Should we just ctrl+c it?
    public static void main(String[] args) throws SQLException {
        Logger.setLogLevel(Logger.Level.DEBUG);

        sSocketManager.setOnReceiveCallback(Server::pushIncomingMessage);
        sSocketManager.start();
        Logger.log(Logger.Level.INFO, "SocketManager Started");

        sDB = new Database(sDatabaseURL);
        Logger.log(Logger.Level.INFO, "Database connected");

        Logger.log(Logger.Level.INFO, "Setting up UserManager");
        sUserManager = new UserManager(sDB);

        Logger.log(Logger.Level.INFO, "Setting up Event Handlers");
        setupEventHandlers();

        Logger.log(Logger.Level.INFO, "Initializing JSONValidator");
        JSONValidator.init();

        Logger.log(Logger.Level.INFO, "Server Running");
        sRunning.set(true);

        try {
            run();
        } catch (Exception e) {
            Logger.logException(Logger.Level.WARN, e, "Caught unexpected exception, shutting down server");
        }

        Logger.log(Logger.Level.INFO, "Server Stopping");

        sSocketManager.stop();
        Logger.log(Logger.Level.INFO, "Stopped SocketManager");

        sDB.close();
        Logger.log(Logger.Level.INFO, "Closing Database");
    }

    ///////////////////////////////////////////////////////
    /// Testing methods, don't use these!!
    ///////////////////////////////////////////////////////
    /**
     * Sets the total time that the main thread will try to poll from PendingRequests before
     * iterating and checking if it should stop.
     * This method is only here for shutting down the server during API testing,
     * it should not be used normally.
     * @param timeoutInMS
     */
    public static void setPollTimeout(long timeoutInMS) {
        sPollTimeout = timeoutInMS;
    }

    public static void stop() {
        sRunning.set(false);
    }

    public static void setDatabase(String database) {
        sDatabaseURL = database;
    }
}
