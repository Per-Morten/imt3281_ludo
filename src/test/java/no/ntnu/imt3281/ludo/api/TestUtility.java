package no.ntnu.imt3281.ludo.api;

import no.ntnu.imt3281.ludo.client.SocketManager;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.NetworkConfig;
import no.ntnu.imt3281.ludo.server.Database;
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

/**
 * Container of different utility functions, like creating different requests,
 * or running the tests (which is a bit more boilerplate when you are doing it async with sockets etc).
 */
class TestUtility {

    private static final long TIMEOUT_TIME_MS = 1000;

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

    static JSONObject createGetFriendRangeRequest(int userID, String token, int page) {
        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.USER_ID, userID);
        request.put(FieldNames.PAGE_INDEX, page);
        payload.put(request);
        return TestUtility.createRequest(RequestType.GET_FRIEND_RANGE_REQUEST, payload, token);
    }

    static JSONObject createFriendRelationshipRequest(RequestType type, int userID, int otherID, String token) {
        // Setup friend message
        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.USER_ID, userID);
        request.put(FieldNames.OTHER_ID, otherID);
        payload.put(0, request);
        return TestUtility.createRequest(type, payload, token);
    }

    static JSONObject createNewChatRequest(int userID, String name, String token) {
        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.USER_ID, userID);
        request.put(FieldNames.NAME, name);
        payload.put(request);
        return createRequest(RequestType.CREATE_CHAT_REQUEST, payload, token);
    }

    static JSONObject createChatJoinRequest(int userID, int chatID, String token) {
        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.USER_ID, userID);
        request.put(FieldNames.CHAT_ID, chatID);
        payload.put(request);
        return createRequest(RequestType.JOIN_CHAT_REQUEST, payload);
    }

    static JSONObject createGetChatRequest(int userID, int chatID, String token) {
        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.USER_ID, userID);
        request.put(FieldNames.CHAT_ID, chatID);
        payload.put(request);
        return createRequest(RequestType.GET_CHAT_REQUEST, payload);
    }

    static void assertError(Error desired, JSONObject object) {
        var error = object.getJSONArray(FieldNames.ERROR);
        assertEquals(1, error.length());
        var firstError = error.getJSONObject(0);
        assertEquals(0, firstError.getInt(FieldNames.ID));
        assertEquals(desired, Error.fromInt(firstError.getJSONArray(FieldNames.CODE).getInt(0)));
    }

    static void assertType(String type, JSONObject object) {
        var field = object.getString(FieldNames.TYPE);
        assertEquals(type, field);
    }

    static void sendMessage(SocketManager socket, JSONObject message) {
        try {
            socket.send(message.toString());
        } catch (IOException e) {
            fail();
        }
    }

    /**
     * Function to hide the uglyness of typing BiConsumer all the time, but Java don't have proper type inference on
     * these things when you try to create them as local variables.
     * So this function is simply an identity function, returning the callback it gets in.
     *
     * @param callback
     * @return
     */
    // Don't want to type out the BiConsumer type all the time.
    static BiConsumer<TestContext, String> createCallback(BiConsumer<TestContext, String> callback) {
        return callback;
    }
    ///////////////////////////////////////////////////////
    /// Running tests Utility
    ///////////////////////////////////////////////////////
    // TODO: Find a way to refactor the three runTest functions so they call to each other instead, there is too much duplication here.

    static void runTestNotRequiringLogin(String firstMessage, BiConsumer<TestContext, String> onReceiveCallback) {
        final var context = new TestContext();

        context.running = new AtomicBoolean(true);
        context.count = new AtomicInteger(0);
        context.socket = new SocketManager(InetAddress.getLoopbackAddress(), NetworkConfig.LISTENING_PORT);

        context.socket.setOnReceiveCallback((string) -> {
            try {
                if (onReceiveCallback != null) {
                    onReceiveCallback.accept(context, string);
                } else {
                    context.running.set(false);
                }
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

    static void runTestWithNewUser(String username, String email, String password, BiConsumer<TestContext, JSONObject> onReceiveCallback) {
        final var context = new TestContext();

        context.running = new AtomicBoolean(true);
        context.count = new AtomicInteger(0);
        context.socket = new SocketManager(InetAddress.getLoopbackAddress(), NetworkConfig.LISTENING_PORT);

        // Used to ensure that the onReceiveCallback of this function don't interferes with
        final var internalCount = new AtomicInteger(0);

        final String pwd = (password != null) ? password : generatePWD();

        context.socket.setOnReceiveCallback((string) -> {
            var msg = new JSONObject(string);

            if (ResponseType.fromString(msg.getString(FieldNames.TYPE)) != null) {
                var type = ResponseType.fromString(msg.getString(FieldNames.TYPE));

                if (internalCount.get() < 2) {
                    if (type == ResponseType.CREATE_USER_RESPONSE) {
                        var successes = msg.getJSONArray(FieldNames.SUCCESS);
                        assertEquals(1, successes.length());
                        TestUtility.sendMessage(context.socket, createLoginRequest(email, pwd));
                        internalCount.incrementAndGet();
                    }

                    if (type == ResponseType.LOGIN_RESPONSE) {
                        var successes = msg.getJSONArray(FieldNames.SUCCESS);
                        assertEquals(1, successes.length());
                        var user = successes.getJSONObject(0);
                        context.user = new Database.User(user.getInt(FieldNames.USER_ID), username, null, email, null, user.getString(FieldNames.AUTH_TOKEN), pwd);
                        internalCount.incrementAndGet();
                    }
                }
            }

            if (onReceiveCallback != null) {
                onReceiveCallback.accept(context, msg);
            } else if (internalCount.get() >= 2) {
                context.running.set(false);
            }
        });

        try {
            context.socket.start();
            context.socket.send(TestUtility.createUserRequest(username, email, pwd).toString());
        } catch (Exception e) {
            Logger.logException(Logger.Level.WARN, e, "Test Threw Exception:");
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

    static void runTestWithExistingUser(Database.User user, BiConsumer<TestContext, JSONObject> onReceiveCallback) {
        final var context = new TestContext();

        context.running = new AtomicBoolean(true);
        context.count = new AtomicInteger(0);
        context.socket = new SocketManager(InetAddress.getLoopbackAddress(), NetworkConfig.LISTENING_PORT);

        // Used to ensure that the onReceiveCallback of this function don't interferes with
        final var internalCount = new AtomicInteger(0);

        context.socket.setOnReceiveCallback((string) -> {
            var msg = new JSONObject(string);
            if (ResponseType.fromString(msg.getString(FieldNames.TYPE)) != null) {
                var type = ResponseType.fromString(msg.getString(FieldNames.TYPE));

                if (internalCount.get() < 1) {
                    if (type == ResponseType.LOGIN_RESPONSE) {
                        var successes = msg.getJSONArray(FieldNames.SUCCESS);
                        assertEquals(1, successes.length());
                        var userJSON = successes.getJSONObject(0);
                        user.id = userJSON.getInt(FieldNames.USER_ID);
                        user.token = userJSON.getString(FieldNames.AUTH_TOKEN);
                        context.user = user;
                        internalCount.incrementAndGet();
                    }
                }

                onReceiveCallback.accept(context, msg);
            }
        });

        try {
            context.socket.start();
            context.socket.send(TestUtility.createLoginRequest(user.email, user.password).toString());
        } catch (Exception e) {
            Logger.logException(Logger.Level.WARN, e, "Test Threw Exception:");
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

    ///////////////////////////////////////////////////////
    /// Utility for generating different aspects
    ///////////////////////////////////////////////////////
    static void setupRelationshipEnvironment(Database.User[] users,
                                                Database.User[][] friends,
                                                Database.User[][] pendingRequests,
                                                Database.User[][] breakups,
                                                Database.User[][] ignored) {
        // Creating the users
        for (var user : users) {
            TestUtility.runTestWithNewUser(user.username, user.email, user.password, (context, msg) -> {
                var type = msg.getString(FieldNames.TYPE);
                if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                    user.id = context.user.id;
                    context.running.set(false);
                }
            });
        }

        // Creating friendships
        for (var friendPair : friends) {
            for (int i = 0; i < 2; i++) {
                final int idx = i;
                TestUtility.runTestWithExistingUser(friendPair[i], (context, msg) -> {
                    TestUtility.sendMessage(context.socket, TestUtility.createFriendRelationshipRequest(RequestType.FRIEND_REQUEST, context.user.id, friendPair[(idx + 1) % 2].id, context.user.token));
                    context.running.set(false);
                });
            }
        }

        // Creating pending requests
        for (var request : pendingRequests) {
            final int i = 0;
            TestUtility.runTestWithExistingUser(request[i], (context, msg) -> {
                TestUtility.sendMessage(context.socket, TestUtility.createFriendRelationshipRequest(RequestType.FRIEND_REQUEST, context.user.id, request[(i + 1)].id, context.user.token));
                context.running.set(false);
            });
        }

        // Creating Existing Breakups
        for (var breakup : breakups) {
            for (int i = 0; i < 2; i++) {
                final var idx = i;
                TestUtility.runTestWithExistingUser(breakup[i], (context, msg) -> {
                    TestUtility.sendMessage(context.socket, TestUtility.createFriendRelationshipRequest(RequestType.UNFRIEND_REQUEST, context.user.id, breakup[(idx + 1) % 2].id, context.user.token));
                    context.running.set(false);
                });
            }
        }

        // Existing Ignores
        for (var ignoredPair : ignored) {
            final int i = 0;
            TestUtility.runTestWithExistingUser(ignoredPair[i], (context, msg) -> {
                TestUtility.sendMessage(context.socket, TestUtility.createFriendRelationshipRequest(RequestType.IGNORE_REQUEST, context.user.id, ignoredPair[(i + 1)].id, context.user.token));
                context.running.set(false);
            });
        }
    }


    private static String generatePWD() {
        var random = new java.security.SecureRandom();

        var builder = new StringBuilder();
        // Restricting the values here a bit because we got issues when we put "illegal" characters such as \n into json.
        // However, this should still provide enough place for randomness.
        random.ints(64, 32, 126).forEach(i -> builder.append((char) i));

        return builder.toString();
    }


    // Probably need to create lots of callbacks within each other.
    // However, not being able to log in should always fail.

    static class TestContext {
        AtomicBoolean running;
        AtomicInteger count;
        SocketManager socket;
        Database.User user;
    }
}
