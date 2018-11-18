package no.ntnu.imt3281.ludo.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.function.Consumer;

import no.ntnu.imt3281.ludo.common.Connection;

/**
 * SocketManager for the client side of the application. Handles sending and
 * receiving messages with the server.
 *
 * Receiving messages is done through the onReceiveCallback. Note: Important to
 * set onReceiveCallback before starting the manager.
 *
 * Manager runs in its own thread, and therefore needs to be started and stopped
 * manually.
 */
public class SocketManager {
    private Consumer<String> mOnReceiveMessage;
    private InetAddress mAddress;
    private int mPort;
    private Connection mConnection;

    /**
     * Creates the SocketManager instructing it to talk to the specified address on
     * the specified port.
     *
     * @param address The server address to connect to.
     * @param port    The server port to connect the socket to.
     */
    public SocketManager(InetAddress address, int port) {
        mAddress = address;
        mPort = port;
    }

    /**
     * Sets the callback to be called when receiving a message from the server.
     *
     * @param onReceiveCallback The callback to call. Note: The callback must be
     *                          able to be called concurrently.
     */
    public void setOnReceiveCallback(Consumer<String> onReceiveCallback) {
        mOnReceiveMessage = onReceiveCallback;
    }

    /**
     * Connects to the endpoint specified upon construction and starts running
     * within its own thread.
     *
     * @throws IOException Throws IOException in the case where sockets can throw.
     */
    public void start() throws IOException {
        var socket = new Socket(mAddress, mPort);
        mConnection = new Connection(socket);
        mConnection.setOnReceiveCallback(mOnReceiveMessage);

        mConnection.start();
    }

    /**
     * Stops the thread and closes the connection.
     *
     * @throws InterruptedException Throws InterruptedException in the case where
     *                              the Connection throws (should not happen).
     */
    public void stop() throws InterruptedException {
        mConnection.stop();
    }

    /**
     * Sends the specified message to the counterpart of this socket. Throws
     * IOException on failure.
     *
     * Note: This signature might change to just return a bool indicating success or
     * failure, as there isn't really anything that can be done further up in the
     * system other than suppressing this thing.
     *
     * @param message The message to sendWithSocketID. (Must not end with "%n" as this is added
     *                before the message is sent.
     *
     * @throws IOException Throws IOException from the SocketWriter if it throws.
     */
    public void send(String message) throws IOException {
        mConnection.send(message);
    }

}
