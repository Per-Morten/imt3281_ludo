package no.ntnu.imt3281.ludo.server;

import no.ntnu.imt3281.ludo.api.Error;
import no.ntnu.imt3281.ludo.api.*;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.NetworkConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is the main class for the server. **Note, change this to extend other
 * classes if desired.**
 *
 * @author
 */
public class Server {
    @FunctionalInterface
    interface EventHandler {
        public void apply(JSONArray requests, JSONArray successes, JSONArray errors, LinkedBlockingQueue<Message> events);
    }

    private static LinkedBlockingQueue<SocketManager.Message> sPendingRequests = new LinkedBlockingQueue<>();
    private static LinkedBlockingQueue<Message> sPendingEvents = new LinkedBlockingQueue<>();

    private static Database sDB;
    private static AtomicBoolean sRunning = new AtomicBoolean();
    private static SocketManager sSocketManager = new SocketManager(NetworkConfig.LISTENING_PORT);
    private static UserManager sUserManager;
    private static ChatManager sChatManager;

    private static HashMap<RequestType, EventHandler> mRequestHandlers = new HashMap<>();

    private static String sDatabaseURL = "ludo.db";

    private static Thread sEventThread;

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

            var handler = mRequestHandlers.get(requestType);
            if (handler != null) {
                handler.apply(requests, successes, errors, sPendingEvents);
            } else {
                Logger.log(Logger.Level.WARN, "Unimplemented feature: %s", requestType.toLowerCaseString());
            }

            // Hack to deal with being able to map user id's to sockets.
            if (requestType == RequestType.LOGIN_REQUEST && message.socketID < 0) {
                updateSocketIDs(message, successes);
            }

            sSocketManager.sendWithSocketID(message.socketID, response.toString());
        }
    }

    private static void startEventThread() {
        sEventThread = new Thread(() -> {
            try {
                while (sRunning.get()) {
                    var event = sPendingEvents.poll(sPollTimeout, TimeUnit.MILLISECONDS);
                    if (event == null) {
                        continue;
                    }

                    for (var receiver : event.receivers) {
                        sSocketManager.sendWithUserID(receiver, event.object.toString());
                    }

                }
            } catch (Exception e) {
                Logger.logException(Logger.Level.ERROR, e, "Unexpected exception in eventThread");
            }
        });

        sEventThread.start();
    }

    private static void stopEventThread() {
        try {
            sEventThread.join();
        } catch (Exception e) {
            Logger.logException(Logger.Level.ERROR, e, "Unexpected exception when joining eventThread");
        }
    }

    private static void removeUnauthorizedRequests(JSONObject message, JSONArray errors) {
        if (!JSONValidator.hasString(FieldNames.AUTH_TOKEN, message)) {
            return;
        }

        var type = RequestType.fromString(message.getString(FieldNames.TYPE));
        var token = message.getString(FieldNames.AUTH_TOKEN);
        var requests = message.getJSONArray(FieldNames.PAYLOAD);

        final var indexesToRemove = new ArrayList<Integer>();
        for (int i = 0; i < requests.length(); i++) {
            var request = requests.getJSONObject(i);
            boolean isAuthorized;
            if (type == RequestType.GET_USER_REQUEST
                    || type == RequestType.GET_USER_RANGE_REQUEST
                    || type == RequestType.GET_CHAT_REQUEST
                    || type == RequestType.GET_CHAT_RANGE_REQUEST) {
                
                isAuthorized = sUserManager.tokenExists(token);
            } else {
                isAuthorized = sUserManager.isUserAuthorized(request, token);
            }
            if (!isAuthorized) {
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
        }
    }

    private static void setupRequestHandlers() {

        /// User related
        mRequestHandlers.put(RequestType.CREATE_USER_REQUEST, sUserManager::createUser);
        mRequestHandlers.put(RequestType.DELETE_USER_REQUEST, sUserManager::deleteUser);
        mRequestHandlers.put(RequestType.UPDATE_USER_REQUEST, sUserManager::updateUser);
        mRequestHandlers.put(RequestType.GET_USER_REQUEST, sUserManager::getUser);
        mRequestHandlers.put(RequestType.GET_USER_RANGE_REQUEST, sUserManager::getUserRange);

        /// Login Related
        mRequestHandlers.put(RequestType.LOGIN_REQUEST, sUserManager::logInUser);
        // More probably needs to happen here, because we need to remove the user from several games as well.
        mRequestHandlers.put(RequestType.LOGOUT_REQUEST, (requests, successes, errors, events) -> {
            sUserManager.logOutUser(requests, successes, errors, events);
            sChatManager.onLogoutUser(requests, successes, errors, events);
        });

        /// Friends Related
        mRequestHandlers.put(RequestType.GET_FRIEND_RANGE_REQUEST, sUserManager::getFriendRange);

        // This probably needs to fire of events just like friend request?
        mRequestHandlers.put(RequestType.UNFRIEND_REQUEST, sUserManager::unfriend);
        mRequestHandlers.put(RequestType.IGNORE_REQUEST, sUserManager::ignore);

        mRequestHandlers.put(RequestType.FRIEND_REQUEST, sUserManager::friend);

        // Chats
        mRequestHandlers.put(RequestType.CREATE_CHAT_REQUEST, sChatManager::createChat);
        mRequestHandlers.put(RequestType.GET_CHAT_REQUEST, sChatManager::getChat);
        mRequestHandlers.put(RequestType.LEAVE_CHAT_REQUEST, sChatManager::leaveChat);

        mRequestHandlers.put(RequestType.SEND_CHAT_INVITE_REQUEST, sChatManager::inviteToChat);

        mRequestHandlers.put(RequestType.JOIN_CHAT_REQUEST, sChatManager::joinChat);
        mRequestHandlers.put(RequestType.SEND_CHAT_MESSAGE_REQUEST, sChatManager::sendChatMessage);

    }

    private static void setupSocketManager() {
        sSocketManager.setOnReceiveCallback(Server::pushIncomingMessage);
        sSocketManager.setOnDisconnectCallback((id) -> {
            try {
                sDB.setUserToken(id.intValue(), null);
                sChatManager.removeFromChats(id.intValue(), sPendingEvents);
            } catch (Exception e) {
                Logger.logException(Logger.Level.WARN, e, "Exception encountered when disconnect logging out user");
            }
        });
    }

    private static void setupShutdownHooks() {
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                sRunning.set(false);
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // TODO: Find out how we shall terminate the server. Should we just ctrl+c it?
    public static void main(String[] args) throws SQLException {
        Logger.setLogLevel(Logger.Level.DEBUG);
        setupShutdownHooks();

        setupSocketManager();
        sSocketManager.start();
        Logger.log(Logger.Level.INFO, "SocketManager Started");

        sDB = new Database(sDatabaseURL);
        Logger.log(Logger.Level.INFO, "Database connected");

        Logger.log(Logger.Level.INFO, "Setting up UserManager");
        sUserManager = new UserManager(sDB);

        Logger.log(Logger.Level.INFO, "Setting up ChatManager");
        sChatManager = new ChatManager(sDB, sUserManager);

        Logger.log(Logger.Level.INFO, "Setting up Event Handlers");
        setupRequestHandlers();

        Logger.log(Logger.Level.INFO, "Initializing JSONValidator");
        JSONValidator.init();

        Logger.log(Logger.Level.INFO, "Setting Running to true");
        sRunning.set(true);

        Logger.log(Logger.Level.INFO, "Starting Event thread");

        startEventThread();
        Logger.log(Logger.Level.INFO, "Server is up");

        try {
            run();
        } catch (Exception e) {
            Logger.logException(Logger.Level.WARN, e, "Caught unexpected exception, shutting down server");
        }

        Logger.log(Logger.Level.INFO, "Server Stopping");

        Logger.log(Logger.Level.INFO, "Stopping event thread");
        stopEventThread();

        sSocketManager.stop();
        Logger.log(Logger.Level.INFO, "Stopped SocketManager");

        // Drop by Rune's office and ask about this.
        // This feels really dirty.
        // Essentially what is happening is that the database apperantly is busy trying to run all the setUserToken
        // requests that happen in the onClosedConnection callback (which will be run for everyone when the server shuts down)
        // That seems to be done asynchroniously(?), and are often in the process of happening when the shutting down the server
        // Leading to a variety of exceptions, or potentially also a fatal error in the Runtime Environment.
        // Simply sleeping the thread a bit (not long enough to notice), seems to fix the problem.
        try {
            Thread.sleep(250);
        } catch (Exception e) {

        }

        Logger.log(Logger.Level.INFO, "Closing Database");
        sDB.close();
    }

    ///////////////////////////////////////////////////////
    /// Testing methods, don't use these!!
    ///////////////////////////////////////////////////////

    /**
     * Sets the total time that the main thread will try to poll from PendingRequests before
     * iterating and checking if it should stop.
     * This method is only here for shutting down the server during API testing,
     * it should not be used normally.
     *
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
