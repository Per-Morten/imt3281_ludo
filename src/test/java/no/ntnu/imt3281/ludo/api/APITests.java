package no.ntnu.imt3281.ludo.api;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import no.ntnu.imt3281.ludo.client.SocketManager;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.MessageType;
import no.ntnu.imt3281.ludo.common.NetworkConfig;
import no.ntnu.imt3281.ludo.server.Server;

public class APITests {
    static Thread sServerThread;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Logger.setLogLevel(Logger.Level.DEBUG);
        Files.deleteIfExists(Paths.get("ludo_tests.db"));

        Server.setDatabase("ludo_tests.db");
        Server.setPollTimeout(2);
        sServerThread = new Thread(() -> {
            try {
                System.out.println("Starting Main");
                Server.main(null);
            } catch (SQLException e) {
                System.out.println("Exception");
                Logger.logException(Logger.Level.WARN, e, "Unhandled Exception in Server");
            }
        });
        sServerThread.start();

        //Thread.sleep(250);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        Server.stop();
        sServerThread.join();
    }

    ///////////////////////////////////////////////////////
    /// User Sunshine tests
    ///////////////////////////////////////////////////////
    @Test
    public void canCreateUser() throws InterruptedException, IOException {
        final AtomicBoolean running = new AtomicBoolean(true);

        var socket = new SocketManager(InetAddress.getLoopbackAddress(), NetworkConfig.LISTENING_PORT);
        socket.setOnReceiveCallback((string) -> {
            var json = new JSONObject(string);
            var msgID = json.getInt(FieldNames.ID);
            assertEquals(msgID, 0);
            var type = json.getString(FieldNames.TYPE);
            assertEquals(type, MessageType.CREATE_USER_RESPONSE);
            var success = json.getJSONArray(FieldNames.SUCCESS);
            assert (success.length() == 1);
            var firstSuccess = success.getJSONObject(0);
            var id = firstSuccess.get(FieldNames.ID);
            assertEquals(id, 0);
            var userID = firstSuccess.getInt(FieldNames.USER_ID);
            assertEquals(userID, 2);

            var error = json.getJSONArray(FieldNames.ERROR);
            assert (error.length() == 0);

            running.set(false);

        });

        socket.start();
        {
            var json = new JSONObject();
            json.put(FieldNames.ID, 0);
            json.put(FieldNames.TYPE, MessageType.CREATE_USER_REQUEST);
            var payload = new JSONArray();
            var request = new JSONObject();
            request.put(FieldNames.ID, 0);
            request.put(FieldNames.USERNAME, "JohnDoe");
            request.put(FieldNames.PASSWORD, "Secret Password");
            request.put(FieldNames.EMAIL, "john.doe@ubermail.com");
            payload.put(0, request);
            json.put(FieldNames.PAYLOAD, payload);
            socket.send(json.toString());

            // TODO: Remove this!
            running.set(false);
        }

        //Thread.sleep(100);
        // Something

        // Should probably timeout at some point here, rather than just using the running.
        while (running.get()) {

        }

        socket.stop();
    }

    ///////////////////////////////////////////////////////
    /// User Failing tests
    ///////////////////////////////////////////////////////

}
