package no.ntnu.imt3281.ludo.api;

import no.ntnu.imt3281.ludo.client.SocketManager;
import no.ntnu.imt3281.ludo.common.NetworkConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

class TestUtility {

    private static final long TIMEOUT_TIME_MS = 5000;

    ///////////////////////////////////////////////////////
    /// Utility Function to Simplify testing
    ///////////////////////////////////////////////////////
    static JSONObject createRequest(RequestType type, JSONArray payload) {
        return createRequest(type, payload, null);
    }

    static JSONObject createRequest(RequestType type, JSONArray payload, String token) {
        var json = new JSONObject();
        json.put(FieldNames.ID, 0);
        json.put(FieldNames.TYPE, type.toLowerCaseString());
        json.put(FieldNames.PAYLOAD, payload);
        json.put(FieldNames.AUTH_TOKEN, token);
        return json;
    }

    static JSONObject createUserRequest(String username, String email, String password) {
        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.USERNAME, username);
        request.put(FieldNames.PASSWORD, password);
        request.put(FieldNames.EMAIL, email);
        payload.put(0, request);
        return createRequest(RequestType.CREATE_USER_REQUEST, payload);
    }

    static JSONObject createLoginRequest(String email, String password) {
        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.EMAIL, email);
        request.put(FieldNames.PASSWORD, password);
        payload.put(0, request);
        return createRequest(RequestType.LOGIN_REQUEST, payload);
    }

    static JSONObject createGetUserRequest(int userID, String token) {
        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.USER_ID, userID);
        payload.put(0, request);
        return createRequest(RequestType.GET_USER_REQUEST, payload, token);
    }

    static void assertError(Error desired, JSONObject object) {
        var error = object.getJSONArray(FieldNames.ERROR);
        assertEquals(1, error.length());
        var firstError = error.getJSONObject(0);
        assertEquals(0, firstError.getInt(FieldNames.ID));
        assertEquals(desired, Error.fromInt(firstError.getJSONArray(FieldNames.CODE).getInt(0)));
    }

    // Don't want to type out the BiConsumer type all the time.
    static BiConsumer<TestContext, String> createCallback(BiConsumer<TestContext, String> callback)
    {
        return callback;
    }

    static void sendPreparationMessageToServer(JSONObject message, Consumer<JSONObject> callback) throws IOException, InterruptedException {
        var socket = new SocketManager(InetAddress.getLoopbackAddress(), NetworkConfig.LISTENING_PORT);
        final var running = new AtomicBoolean(true);
        socket.setOnReceiveCallback((string) -> {
            var json = new JSONObject(string);
            if (callback != null) {
                callback.accept(json);
            }
            running.set(false);
        });

        socket.start();
        socket.send(message.toString());
        long end = System.currentTimeMillis() + TIMEOUT_TIME_MS;
        while (running.get() && System.currentTimeMillis() < end) {
            // Empty
        }

        if (running.get()) {
            throw new RuntimeException("Didn't get response from server fast enough");
        }
        socket.stop();
    }

    static void runTest(BiConsumer<TestContext, String> onReceiveCallback, String firstMessage) {
        final var context = new TestContext();

        context.running = new AtomicBoolean(true);
        context.count = new AtomicInteger(0);
        context.socket = new SocketManager(InetAddress.getLoopbackAddress(), NetworkConfig.LISTENING_PORT);

        context.socket.setOnReceiveCallback((string) -> {
            try {
                onReceiveCallback.accept(context, string);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        });
        try {
            context.socket.start();
            context.socket.send(firstMessage);
        } catch (Exception e) {
            fail();
        }

        long end = System.currentTimeMillis() + TIMEOUT_TIME_MS;
        while (context.running.get() && System.currentTimeMillis() < end) {

        }

        if (context.running.get()) {
            fail("Timed out");
        }

        try {
            context.socket.stop();
        } catch (Exception e) {
            fail();
        }
    }

    static class TestContext {
        AtomicBoolean running;
        AtomicInteger count;
        SocketManager socket;
    }
}
