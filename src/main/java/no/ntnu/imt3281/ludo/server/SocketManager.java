package no.ntnu.imt3281.ludo.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

import no.ntnu.imt3281.ludo.common.Connection;
import no.ntnu.imt3281.ludo.common.Logger;

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

    // Need specific callback for logging in and creating user, so we can map user
    // id to socket.
    private BiConsumer<Long, String> mOnReceiveCallback;
    private Thread mThread;
    private AtomicBoolean mRunning;
    private int mListeningPort;

    // Sockets that has yet to receive a user id.
    private AtomicLong mUnknownId;

    private ConcurrentHashMap<Long, Connection> mSockets;

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
    public void setOnReceiveCallback(BiConsumer<Long, String> onReceiveCallback) {
        mOnReceiveCallback = onReceiveCallback;
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
            Logger.log(Logger.Level.ERROR,
                    String.format("Could not open ServerSocket in SocketManager, exception: %s, trace: %s",
                            e.getClass().getName(), e.getStackTrace()));
        }
        mRunning.set(true);
        mThread = new Thread(() -> {
            try {
                run();
            } catch (IOException e) {
                Logger.log(Logger.Level.ERROR,
                        String.format("Could not start SocketManagerThread, exception: %s, trace: %s",
                                e.getClass().getName(), e.getStackTrace()));
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
            Logger.log(Logger.Level.ERROR,
                    String.format("Could not close SocketManager socket, exception: %s, trace: %s",
                            e.getClass().getName(), e.getStackTrace()));
        }

        // Need to close all sockets, as that will also stop any potential blocking
        // readlines,
        // so an ugly but working solution to stop blocking readlines is to close the
        // sockets.
        for (var s : mSockets.values()) {
            try {
                s.stop();
            } catch (Exception e) {
                Logger.log(Logger.Level.WARN, String.format("Exception Encountered when closing client sockets, %s, %s",
                        e.getClass().getName(), e.getStackTrace()));
            }
        }

        try {
            mThread.join();
        } catch (Exception e) {
            Logger.log(Logger.Level.ERROR, "Could not join the SocketManager Thread");
        }
    }

    /**
     * Sends the specified message to the client indicated by the id.
     *
     * @param id      The ID of the socket to send the message through.
     * @param message The string containing the message to send. Must not end with
     *                \n
     */
    public void send(long id, String message) {
        var socket = mSockets.get(id);
        try {
            socket.send(message);
        } catch (Exception e) {
            Logger.log(Logger.Level.WARN, String.format("Exception Encountered when sending message, %s, %s",
                    e.getClass().getName(), e.getStackTrace()));
        }
    }

    /**
     * Updates the ID of the socket specified by oldId.
     *
     * @param oldId The current id of the socket to update.
     * @param id    The desired id of the socket.
     */
    public void assignIdToSocket(long oldId, long id) {
        Connection socket;

        // NOTE: This is not an "atomic" operation, we are doing 2 atomic operations !=
        // 1 atomic operation
        // There is a chance that a socket closes, but right before we remove the socket
        // in manageEstablishedConnection
        // we remove it in this if statement here, meaning that the remove in
        // manageEstablishedConnection will fail silently
        // and we will add the closed connection to mSockets. (assuming s.isClosed isn't
        // atomic).
        // Which is essentially a memory leak.
        // However, the chances of that happening are quite small.
        // But we should still check that a socket is connected in the send function(?)
        //
        // Another fix for this is simply to add a function that removes any closed
        // connections,
        // not the most elegant solution, but will work.
        // Another alternative is to just start locking down the entire sockets list
        // etc,
        // but I don't really want to do that, as we then are essentially making
        // multi-threaded programs single threaded
        // which is a horrible concept, and also most likely leads to worse performance
        // than just working single threaded.
        if ((socket = mSockets.remove(oldId)) != null) {
            mSockets.put(id, socket);
        }
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
                // and it can be used to send messages.
                try {
                    final var newSocketId = mUnknownId.decrementAndGet();
                    var connection = new Connection(socket);
                    connection.setOnReceiveCallback((value) -> {
                        mOnReceiveCallback.accept(newSocketId, value);
                    });

                    connection.start();
                    mSockets.put(newSocketId, connection);
                } catch (Exception e) {
                    Logger.log(Logger.Level.WARN,
                            String.format("Exception encountered when creating new connection: %s, %s",
                                    e.getClass().getName(), e.getStackTrace()));
                }
            }
        } catch (SocketException e) {
            if (mRunning.get()) {
                Logger.log(Logger.Level.ERROR,
                        String.format("SocketManager Socket closed unexpectedly, exception: %s, trace: %s",
                                e.getClass().toString(), e.getStackTrace()));
            }
        } finally {
            if (!mServerSocket.isClosed()) {
                mServerSocket.close();
            }
        }
    }

}
