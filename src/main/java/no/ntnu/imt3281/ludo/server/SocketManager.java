package no.ntnu.imt3281.ludo.server;

import no.ntnu.imt3281.ludo.common.Connection;
import no.ntnu.imt3281.ludo.common.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * SocketManager for the server side of the application. Handles sending and
 * receiving messages with the clients.
 *
 * Receiving messages is done through the onReceiveCallback. Note: Important to
 * set onReceiveCallback before starting the manager. Rather than just supplying
 * the message, the callback also supplies an ID belonging to the socket the
 * message comes from, which is useful for knowing who to respond to when
 * sending message.
 *
 * Manager runs in its own thread, and therefore needs to be started and stopped
 * manually.
 */
public class SocketManager {

    public static class Message {
        public Long socketID; // This is a problem if the client sends more messages before it has gotten an
        // ID. However, that should not happen, as we get to find out what the ID is when we log the user in.
        public String message;
    }


    // Need specific callback for logging in and creating user, so we can map user
    // id to socket.
    private Consumer<Message> mOnReceiveCallback;
    private Consumer<Long> mOnDisconnectCallback;

    private Thread mThread;
    private AtomicBoolean mRunning;
    private int mListeningPort;

    // Sockets that has yet to receive a user id.
    private AtomicLong mUnknownId;

    private ConcurrentHashMap<Long, Connection> mSockets;
    private ConcurrentHashMap<Long, Connection> mUserSockets;

    private ServerSocket mServerSocket;

    /**
     * Initializes the SocketManager with the port it should listen to.
     *
     * @param listeningPort The port that the SocketManager should listen to
     *                      incoming connections on.
     */
    public SocketManager(int listeningPort) {
        mRunning = new AtomicBoolean(false);
        mListeningPort = listeningPort;

        mSockets = new ConcurrentHashMap<>();
        mUserSockets = new ConcurrentHashMap<>();

        mUnknownId = new AtomicLong();
    }

    /**
     * Sets the callback to use when receiving a message. This must be done before
     * the manager is started.
     *
     * @param onReceiveCallback The function to call when a message has been
     *                          received. This function must be safe to call in a
     *                          concurrent setting.
     */
    public void setOnReceiveCallback(Consumer<Message> onReceiveCallback) {
        mOnReceiveCallback = onReceiveCallback;
    }

    /**
     * Sets the callback to use when a client is disconnecting. This must be done before the manager is started.
     * Note: This only happens if the user was ever logged in (i.e. tied to a userID).
     * @param onDisconnectCallback
     */
    public void setOnDisconnectCallback(Consumer<Long> onDisconnectCallback) {
        mOnDisconnectCallback = onDisconnectCallback;
    }

    /**
     * Starts the serversocket listening for incoming connections on the port. The
     * SocketManager runs in its own thread. It crashes the entire application in
     * the case where it cannot start the thread or establish the socket.
     */
    public void start() {
        try {
            mServerSocket = new ServerSocket(mListeningPort);
        } catch (IOException e) {
            Logger.logException(Logger.Level.ERROR, e, "Could not open ServerSocket in SocketManager");
        }
        mRunning.set(true);
        mThread = new Thread(() -> {
            try {
                run();
            } catch (IOException e) {
                Logger.logException(Logger.Level.ERROR,e , "Could not start SocketManagerThread");
            }
        });
        mThread.start();
    }

    /**
     * Stops the SocketManager, joining its thread. Crashes the application in case
     * it cannot close the server socket, or join the thread.
     */
    public void stop() {

        mRunning.set(false);
        try {
            mServerSocket.close();
        } catch (Exception e) {
            Logger.logException(Logger.Level.ERROR, e, "Could not close SocketManager socket");
        }

        // Need to close all sockets, as that will also stop any potential blocking
        // readlines,
        // so an ugly but working solution to stop blocking readlines is to close the
        // sockets.
        for (var s : mSockets.values()) {
            try {
                s.stop();
            } catch (Exception e) {
                Logger.logException(Logger.Level.WARN,e , "Exception Encountered when closing client sockets");
            }
        }

        mUserSockets.clear();

        try {
            mThread.join();
        } catch (Exception e) {
            Logger.logException(Logger.Level.ERROR, e, "Could not join the SocketManager Thread");
        }
    }

    /**
     * Sends the specified message to the client connected to the socket identified by the id.
     *
     * @param socketID      The ID of the socket to send the message through.
     * @param message The string containing the message to sendWithSocketID. Must not end with
     *                \n
     */
    public void sendWithSocketID(long socketID, String message) {
        var socket = mSockets.get(socketID);
        if (socket != null) {
            try {
                socket.send(message);
            } catch (Exception e) {
                Logger.logException(Logger.Level.WARN, e, "Exception Encountered when sending message");
            }
        } else {
            Logger.log(Logger.Level.WARN, "Tried to send message to non existent socket, socketID: %d", socketID);
        }
    }

    /**
     * Sends the specified message to the client connected to the socket identified by the id.
     *
     * @param userID      The ID of the user to send the message to.
     * @param message The string containing the message to send. Must not end with
     *                \n
     */
    public void sendWithUserID(long userID, String message) {
        var socket = mUserSockets.get(userID);
        if (socket != null) {
            try {
                socket.send(message);
            } catch (Exception e) {
                Logger.logException(Logger.Level.WARN, e, "Exception Encountered when sending message");
            }
        } else {
            Logger.log(Logger.Level.WARN, "Tried to send message to non existent socket, userID: %d, %s", userID, message);
        }
    }


    /**
     * Updates the ID of the socket specified by oldId.
     *
     * @param socketID The current id of the socket to update.
     * @param userID    The desired id of the socket.
     */
    public void assignIdToSocket(long socketID, long userID) {

        // What happens if someone else logs in with the same ID?
        // Need to fix that case.
        // Can we force all sockets in mUserSockets to be unique?
        var socket = mSockets.get(socketID);
        if (socket == null) {
            Logger.log(Logger.Level.WARN, "Tried to assign userID %d to socket %d, they have already disconnected!", socketID, userID);
            return;
        }

        if (mUserSockets.containsValue(socket)) {
            Logger.log(Logger.Level.WARN, "mUserSocket already contains the value identified by %d", socketID);
        }
        mUserSockets.put(userID, socket);
    }

    private void run() throws IOException {
        try {
            while (mRunning.get()) {
                var socket = mServerSocket.accept();

                // Don't know the "user_id" of the client belonging to this socket connection
                // yet,
                // therefore we are temporarily marking it as unknown.
                // once we get an "user_id" for it, we will update the key so it is a regular
                // socket.
                // and it can be used to sendWithSocketID messages.
                try {
                    final var newSocketId = mUnknownId.decrementAndGet();
                    var connection = new Connection(socket);
                    connection.setOnReceiveCallback((value) -> {
                        var msg = new Message();
                        msg.message = value;
                        msg.socketID = newSocketId; // This is a problem! Because this obviously isn't updated
                        mOnReceiveCallback.accept(msg);
                    });

                    connection.setOnSocketClosed(() -> {
                        mSockets.remove(newSocketId);

                        // Should just be 1.
                        var entries = mUserSockets.entrySet().stream().filter((item) -> item.getValue().equals(connection)).collect(Collectors.toList());
                        if (!entries.isEmpty()) {
                            var id = entries.get(0).getKey();
                            mUserSockets.remove(id);
                            mOnDisconnectCallback.accept(id);
                        }
                    });

                    connection.start();
                    mSockets.put(newSocketId, connection);
                } catch (Exception e) {
                    Logger.logException(Logger.Level.WARN, e, "Exception encountered when creating new connection");
                }
            }
        } catch (SocketException e) {
            if (mRunning.get()) {
                Logger.logException(Logger.Level.ERROR, e, "SocketManager Socket closed unexpectedly");
            }
        } finally {
            if (!mServerSocket.isClosed()) {
                mServerSocket.close();
            }
        }
    }

}
