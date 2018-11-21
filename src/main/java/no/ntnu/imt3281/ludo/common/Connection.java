package no.ntnu.imt3281.ludo.common;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Semaphore;
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
    private Runnable mOnSocketClosed;
    private Socket mSocket;
    private BufferedWriter mSocketWriter;

    private Semaphore mSemaphore = new Semaphore(1);

    private Thread mReceiveThread;

    /**
     * Creates a new connection with the specified socket.
     *
     * @param socket The socket that this connection will communicate through.
     */
    public Connection(Socket socket) {
        mSocket = socket;
    }

    /**
     * Set the callback that should be receive when this connection receives data from
     * the other endpoint.
     *
     * @param onReceiveCallback The callback to receive, important the callback must
     *                          support being called concurrently.
     */
    public void setOnReceiveCallback(Consumer<String> onReceiveCallback) {
        mOnReceiveMessage = onReceiveCallback;
    }

    public void setOnSocketClosed(Runnable onSocketClosed) {
        mOnSocketClosed = onSocketClosed;
    }

    /**
     * Starts running this connection within its own thread.
     *
     * @throws IOException Throws IOException from BufferedWriter if there is any
     *                     problems arising from that.
     */
    public void start() throws IOException {
        mSocketWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));

        mReceiveThread = new Thread(this::receive);
        mReceiveThread.start();
    }

    /**
     * Stops the thread and closes the connection.
     *
     * @throws InterruptedException Throws InterruptedException in the case the
     *                              thread does it on join (should not happen).
     */
    public void stop() throws InterruptedException {
        mReceiveThread.interrupt();

        try {
            mSocketWriter.close();
            mSocket.close();
        } catch (IOException e) {
            Logger.logException(Logger.Level.WARN, e, "Could not close connection socket");
        }

        mReceiveThread.join();
    }

    /**
     * Sends the specified message to the counterpart of this socket. Throws
     * IOException on failure.
     * <p>
     * Note: This signature might change to just return a bool indicating success or
     * failure, as there isn't really anything that can be done further up in the
     * system other than suppressing this thing.
     *
     * @param message The message to sendWithSocketID. (Must not end with "%n" as this is added
     *                before the message is sent.
     * @throws IOException Throws IOException from the SocketWriter if it throws.
     */
    public void send(String message) throws IOException {
//        Logger.log(Logger.Level.DEBUG, String.format("Sending message to:%s: %d, message: %s",
//                mSocket.getInetAddress().toString(), mSocket.getPort(), message));

        try {
            mSemaphore.acquire();
            // Don't remove + "%n", it is needed for readLine on the other side.
            // Tok me 5 hours to figure out.
            mSocketWriter.write(String.format("%s%n", message));

            // If we get nullptr exception here that is considered  a disconnect, need to handle that, add more callbacks.
            mSocketWriter.flush();
        } catch (SocketException e) {
            onException(e);
        } catch (InterruptedException e) {
            Logger.logException(Logger.Level.WARN, e, "Interrupt exception encountered");
        } finally {
            mSemaphore.release();
        }
    }

    private void receive() {
        try (var input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()))) {
            String line;
            while (!Thread.currentThread().isInterrupted() && (line = input.readLine()) != null) {
                mOnReceiveMessage.accept(line);
            }
        } catch (SocketException e) {
            if (e.getMessage() != null && !e.getMessage().equals("Socket closed")) {
                Logger.logException(Logger.Level.WARN, e, "Connection closed unexpectedly");
            }
        } catch (IOException e) {
            Logger.logException(Logger.Level.WARN, e, "Exception encountered in receive");
        } finally {
            // Need this also in the case where we are actually closing the connection.
            if (mOnSocketClosed != null) {
                mOnSocketClosed.run();
            }
        }
    }

    private void onException(Exception e) {
        if (e.getMessage() != null && !e.getMessage().equals("Socket closed")) {
            Logger.logException(Logger.Level.WARN, e, "Connection closed unexpectedly");
        }
    }

}
