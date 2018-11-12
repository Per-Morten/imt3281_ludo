package no.ntnu.imt3281.ludo.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Class represents a connection between two sockets, and handles reading and
 * writing to communicate with that socket. However, the creation of the socket
 * is not the responsibility of the connection.
 *
 * Reading is done via callbacks, while sending is done through a straight
 * function call. A connection runs in its own thread, and therefore needs to be
 * started and stopped manually.
 *
 * Note: It is important that you set up reception callbacks before starting the
 * connection.
 */
public class Connection {
    private Consumer<String> mOnReceiveMessage;
    private Socket mSocket;
    private BufferedWriter mSocketWriter;
    private AtomicBoolean mRunning = new AtomicBoolean(false);

    private Thread mThread;

    /**
     * Creates a new connection with the specified socket.
     *
     * @param socket The socket that this connection will communicate through.
     */
    public Connection(Socket socket) {
        mSocket = socket;
    }

    /**
     * Set the callback that should be run when this connection receives data from
     * the other endpoint.
     *
     * @param onReceiveCallback The callback to run, important the callback must
     *                          support being called concurrently.
     */
    public void setOnReceiveCallback(Consumer<String> onReceiveCallback) {
        mOnReceiveMessage = onReceiveCallback;
    }

    /**
     * Starts running this connection within its own thread.
     *
     * @throws IOException Throws IOException from BufferedWriter if there is any
     *                     problems arising from that.
     */
    public void start() throws IOException {
        mSocketWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));

        mRunning.set(true);
        mThread = new Thread(() -> run());
        mThread.start();
    }

    /**
     * Stops the thread and closes the connection.
     *
     * @throws InterruptedException Throws InterruptedException in the case the
     *                              thread does it on join (should not happen).
     */
    public void stop() throws InterruptedException {
        mRunning.set(false);

        try {
            mSocketWriter.close();
            mSocket.close();
        } catch (IOException e) {
            Logger.log(Logger.Level.WARN, String.format("Could not close Connection socket, exception: %s, trace: %s",
                    e.getClass().getName(), e.getStackTrace()));
        }

        mThread.join();
    }

    /**
     * Sends the specified message to the counterpart of this socket. Throws
     * IOException on failure.
     *
     * Note: This signature might change to just return a bool indicating success or
     * failure, as there isn't really anything that can be done further up in the
     * system other than suppressing this thing.
     *
     * @param message The message to send. (Must not end with "%n" as this is added
     *                before the message is sent.
     *
     * @throws IOException Throws IOException from the SocketWriter if it throws.
     */
    public void send(String message) throws IOException {
        Logger.log(Logger.Level.DEBUG, String.format("Sending message to:%s: %d, message: %s",
                mSocket.getInetAddress().toString(), mSocket.getPort(), message));

        // Don't remove + "\n", it is needed for readLine on the other side.
        // Tok me 5 hours to figure out.
        mSocketWriter.write(String.format("%s%n",message));
        mSocketWriter.flush();
    }

    private void run() {
        try (var input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()))) {
            String line;
            while (mRunning.get() && (line = input.readLine()) != null) {
                mOnReceiveMessage.accept(line);
            }
        } catch (Exception e) {
            if (mRunning.get()) {
                Logger.log(Logger.Level.WARN, String.format("Connection closed unexpectedly, exception: %s, trace: %s",
                        e.getClass().toString(), e.getStackTrace()));
                mRunning.set(false);
            }
        }
    }
}
