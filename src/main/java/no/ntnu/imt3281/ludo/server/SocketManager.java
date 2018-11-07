package no.ntnu.imt3281.ludo.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.json.JSONObject;

// Thoughts:
// * Also need a way to be notified when people disconnect, as that means they must be removed from all their games.
//      * Can possibly just log people out if the connection is destroyed?
//      * Want a way to go through and remove all sockets that are disconnected.
//          * Cannot just do it in manageEstablishedConnections on fail,
//            as we can get a "race" on it being added to mSockets before it is removed from mUnknown and mSockets.
//            However, the chances of that race happening are quite slim, so rethink if it is really needed.
//            I.e. do we need 100% thread safety, or do we just need to be thread safe enough?

/**
 * SocketManager handles all forms of communication with clients through
 * sockets. Sending messages can be done through the send function, while
 * receiving messages happens through callbacks.
 */
public class SocketManager {
    private Consumer<Long> mOnConnectAcceptedCallback;
    private Consumer<JSONObject> mOnReceiveCallback;
    private ExecutorService mThreadPool;
    private AtomicBoolean mRunning;
    private int mListeningPort;

    // Sockets that has yet to receive a user id.
    private AtomicLong mUnknownId;
    // Sockets with a negative integer
    private ConcurrentHashMap<Long, Socket> mSockets;

    /**
     * Initializes the SocketManager with the callback it should use when receiving
     * a message.
     *
     * @param onReceiveCallback The function to call when a message has been
     *                          received. This function must be safe to call in a
     *                          concurrent setting.
     *
     * @param listeningPort     The port that the SocketManager should listen to
     *                          incoming connections on.
     */
    public SocketManager(Consumer<JSONObject> onReceiveCallback,
                         Consumer<Long> onConnectAcceptedCallback,
                         int listeningPort) {

        mOnConnectAcceptedCallback = onConnectAcceptedCallback;
        mOnReceiveCallback = onReceiveCallback;
        mThreadPool = Executors.newCachedThreadPool();
        mRunning = new AtomicBoolean(false);
        mListeningPort = listeningPort;

        mSockets = new ConcurrentHashMap<>();

        mUnknownId = new AtomicLong();
    }

    /**
     * Starts running the SocketManager, note, blocking method, so should be run in
     * its own thread.
     *
     * @throws IOException
     */
    public void run() throws IOException {
        mRunning.set(true);

        try (var server = new ServerSocket(mListeningPort)) {
            while (mRunning.get()) {
                var socket = server.accept();

                // Don't know the "user_id" of the client belonging to this socket connection
                // yet,
                // therefore we are temporarily marking it as unknown.
                // once we get an "user_id" for it, we will update the key so it is a regular socket.
                // and it can be used to send messages.

                var newSocketId = mUnknownId.decrementAndGet();
                mSockets.put(newSocketId, socket);

                mOnConnectAcceptedCallback.accept(newSocketId);
                mThreadPool.execute(() -> manageEstablishedConnection(socket));
            }

        } catch (RuntimeException e) {
            System.out.print(String.format("Exception encountered: %s", e.getMessage()));
        }
    }

    private void manageEstablishedConnection(Socket socket) {
        try (var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            while (mRunning.get()) {
                var line = reader.readLine();
                if (line != null) {
                    try {
                        // Should we really do json parsing here, or just send the string?
                        // Would be cool to avoid the whole string thing here.
                        var json = new JSONObject(line);
                        mOnReceiveCallback.accept(json);
                    } catch (RuntimeException e) {
                        // TODO: Log when logger is in place.
                    }
                }
            }
        } catch (IOException e) {

            // TODO: Execute like if the user logged off.
        }

        mSockets.values().remove(socket);
    }

    public void assignIdToSocket(long unknownId, long id) {
        Socket socket;

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
        if ((socket = mSockets.remove(unknownId)) != null && !socket.isClosed()) {
            mSockets.put(id, socket);
        }
    }

    public void shutdown() {
        mRunning.set(false);

        // Need to close all sockets, as that will also stop any potential blocking readlines,
        // so an ugly but working solution to stop blocking readlines is to close the sockets.
        for (var s : mSockets.values()) {
            try {
                s.close();
            } catch(Exception e) {
                // TODO: Log this, get a logger in place
            }
        }

        mThreadPool.shutdown();
    }

    // Need to also
    public void send(JSONObject obj) {

    }

}
