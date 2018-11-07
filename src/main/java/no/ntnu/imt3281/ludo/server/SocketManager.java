package no.ntnu.imt3281.ludo.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.json.JSONObject;

// Thoughts:
// * Also need a way to be notified when people disconnect, as that means they must be removed from all their games.
//      * Can possibly just log people out if the connection is destroyed?

/**
 * SocketManager handles all forms of communication with clients through
 * sockets. Sending messages can be done through the send function, while
 * receiving messages happens through callbacks.
 */
public class SocketManager {
    private Consumer<JSONObject> mOnReceiveCallback;
    private ExecutorService mThreadPool;
    private AtomicBoolean mRunning;
    private int mListeningPort;

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
    public SocketManager(Consumer<JSONObject> onReceiveCallback, int listeningPort) {
        mOnReceiveCallback = onReceiveCallback;
        mThreadPool = Executors.newCachedThreadPool();
        mRunning = new AtomicBoolean(false);
        mListeningPort = listeningPort;
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
                // TODO: Log that a connection was accepted.
                mThreadPool.execute(() -> {
                    manageEstablishedConnection(socket, mRunning);
                });
            }

            server.close();
        } catch (RuntimeException e) {
            System.out.print(String.format("Exception encountered: %s", e.getMessage()));
        }

        // Look for incoming connections
        // Accept incoming connections and put them in a separate thread using the
        // thread pool.
        // We should be able to handle most exceptions.

        // Need to identify who the message should be sent to.
        // Cannot just broadcast it.

    }

    public void shutdown() {
        mRunning.set(false);

        // Need to read docs and check what happens if a task is blocking,
        // because bufferedReader.readline is a blocking process.
        mThreadPool.shutdown();
    }

    // Need to also
    public void send(JSONObject obj) {

    }

    private static void manageEstablishedConnection(Socket socket, AtomicBoolean running) {
        try (var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            while (running.get()) {

            }
        } catch (IOException e) {
            // TODO: Execute like if the user logged off.
        }

    }

}
