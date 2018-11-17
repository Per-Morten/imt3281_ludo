package no.ntnu.imt3281.ludo.api;

import no.ntnu.imt3281.ludo.client.SocketManager;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.NetworkConfig;
import no.ntnu.imt3281.ludo.server.Server;
import no.ntnu.imt3281.ludo.server.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class APITests {
    private static Thread sServerThread;
    private static final long TIMEOUT_TIME_MS = 5000;

    private static final User USER_1 = new User(1, "User1", null, "User1@mail.com", null, null, "User1Password");
    private static final User USER_2 = new User(2, "User2", null, "User2@mail.com", null, null, "User2Password");
    private static final User USER_3 = new User(3, "User3", null, "User3@mail.com", null, null, "User3Password");
    private static final User LOGGED_IN_USER = new User(4, "LoggedInUser", null, "LoggedInUser@mail.com", null, null, "LoggedInPassword");
    private static final User USER_TO_BE_UPDATED = new User(5, "UserToBeUpdated", null, "UserToBeUpdated@mail.com", null, null, "UserToBeUpdatedPassword");
    private static final User USER_TO_BE_DELETED = new User(6, "UserToBeDeleted", null, "UserToBeDeleted@mail.com", null, null, "UserToBeDeletedPassword");

    ///////////////////////////////////////////////////////
    /// Utility Function to Simplify testing
    ///////////////////////////////////////////////////////
    private static JSONObject createRequest(RequestType type, JSONArray payload) {
        return createRequest(type, payload, null);
    }

    private static JSONObject createRequest(RequestType type, JSONArray payload, String token) {
        var json = new JSONObject();
        json.put(FieldNames.ID, 0);
        json.put(FieldNames.TYPE, type.toLowerCaseString());
        json.put(FieldNames.PAYLOAD, payload);
        json.put(FieldNames.AUTH_TOKEN, token);
        return json;
    }

    private static JSONObject createUserRequest(String username, String email, String password) {
        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.USERNAME, username);
        request.put(FieldNames.PASSWORD, password);
        request.put(FieldNames.EMAIL, email);
        payload.put(0, request);
        return createRequest(RequestType.CREATE_USER_REQUEST, payload);
    }

    private static JSONObject createLoginRequest(String email, String password) {
        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.EMAIL, email);
        request.put(FieldNames.PASSWORD, password);
        payload.put(0, request);
        return createRequest(RequestType.LOGIN_REQUEST, payload);
    }

    private static JSONObject createGetUserRequest(int userID, String token) {
        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.USER_ID, userID);
        payload.put(0, request);
        return createRequest(RequestType.GET_USER_REQUEST, payload, token);
    }

    private static void assertError(Error desired, JSONObject object) {
        var error = object.getJSONArray(FieldNames.ERROR);
        assertEquals(1, error.length());
        var firstError = error.getJSONObject(0);
        assertEquals(0, firstError.getInt(FieldNames.ID));
        assertEquals(desired, Error.fromInt(firstError.getJSONArray(FieldNames.CODE).getInt(0)));
    }

    // Don't want to type out the BiConsumer type all the time.
    private static BiConsumer<TestContext, String> createCallback(BiConsumer<TestContext, String> callback)
    {
        return callback;
    }

    private static void sendPreparationMessageToServer(JSONObject message, Consumer<JSONObject> callback) throws IOException, InterruptedException {
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
        // We need to sleep a bit here to ensure that the thread is actually running.
        Thread.sleep(250);

        // Creation
        sendPreparationMessageToServer(createUserRequest(USER_1.username, USER_1.email, USER_1.password), null);
        sendPreparationMessageToServer(createUserRequest(USER_2.username, USER_2.email, USER_2.password), null);
        sendPreparationMessageToServer(createUserRequest(USER_3.username, USER_3.email, USER_3.password), null);
        sendPreparationMessageToServer(createUserRequest(LOGGED_IN_USER.username, LOGGED_IN_USER.email, LOGGED_IN_USER.password), null);
        sendPreparationMessageToServer(createUserRequest(USER_TO_BE_UPDATED.username, USER_TO_BE_UPDATED.email, USER_TO_BE_UPDATED.password), null);
        sendPreparationMessageToServer(createUserRequest(USER_TO_BE_DELETED.username, USER_TO_BE_DELETED.email, USER_TO_BE_DELETED.password), null);

        // Logging in
        sendPreparationMessageToServer(createLoginRequest(LOGGED_IN_USER.email, LOGGED_IN_USER.password), (json) -> {
            var success = json.getJSONArray(FieldNames.SUCCESS);
            if (success.length() != 1) {
                throw new RuntimeException("Did not get a valid log in back");
            }
            LOGGED_IN_USER.token = success.getJSONObject(0).getString(FieldNames.AUTH_TOKEN);
        });

        sendPreparationMessageToServer(createLoginRequest(USER_TO_BE_UPDATED.email, USER_TO_BE_UPDATED.password), (json) -> {
            var success = json.getJSONArray(FieldNames.SUCCESS);
            if (success.length() != 1) {
                throw new RuntimeException("Did not get a valid log in back");
            }
            USER_TO_BE_UPDATED.token = success.getJSONObject(0).getString(FieldNames.AUTH_TOKEN);
        });

        sendPreparationMessageToServer(createLoginRequest(USER_TO_BE_DELETED.email, USER_TO_BE_DELETED.password), (json) -> {
            var success = json.getJSONArray(FieldNames.SUCCESS);
            if (success.length() != 1) {
                throw new RuntimeException("Did not get a valid log in back");
            }
            USER_TO_BE_DELETED.token = success.getJSONObject(0).getString(FieldNames.AUTH_TOKEN);
        });
    }


    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        Server.stop();
        sServerThread.join();
    }

    private static class TestContext {
        AtomicBoolean running;
        AtomicInteger count;
        SocketManager socket;
    }

    private static void runTest(BiConsumer<TestContext, String> onReceiveCallback, String firstMessage) {
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

    ///////////////////////////////////////////////////////
    /// User Sunshine tests
    ///////////////////////////////////////////////////////
    @Test
    public void canCreateUser() {
        var callback = createCallback((context, string) -> {
            var json = new JSONObject(string);
            var msgID = json.getInt(FieldNames.ID);
            assertEquals(0, msgID);
            var type = json.getString(FieldNames.TYPE);
            assertEquals(ResponseType.CREATE_USER_RESPONSE.toLowerCaseString(), type);
            var success = json.getJSONArray(FieldNames.SUCCESS);
            assertEquals(1, success.length());
            var firstSuccess = success.getJSONObject(0);
            var id = firstSuccess.get(FieldNames.ID);
            assertEquals(0, id);
            var userID = firstSuccess.getInt(FieldNames.USER_ID);
            var error = json.getJSONArray(FieldNames.ERROR);
            assert (error.length() == 0);

            context.running.set(false);
        });

        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.USERNAME, "JohnDoe");
        request.put(FieldNames.PASSWORD, "Secret Password");
        request.put(FieldNames.EMAIL, "john.doe@ubermail.com");
        payload.put(0, request);

        runTest(callback, createRequest(RequestType.CREATE_USER_REQUEST, payload).toString());
    }

    @Test
    public void canLogIn() {

        // Desired ID is 1 because we are logging in as the first test user.
        int desiredID = 1;
        var callback = createCallback((context, string) -> {
            var json = new JSONObject(string);
            var msgID = json.getInt(FieldNames.ID);
            assertEquals(0, msgID);
            var type = json.getString(FieldNames.TYPE);
            assertEquals(ResponseType.LOGIN_RESPONSE.toLowerCaseString(), type);
            var success = json.getJSONArray(FieldNames.SUCCESS);
            assertEquals(1, success.length());
            var firstSuccess = success.getJSONObject(0);
            var id = firstSuccess.get(FieldNames.ID);
            assertEquals(0, id);
            var userID = firstSuccess.getInt(FieldNames.USER_ID);
            assertEquals(desiredID, userID);
            var token = firstSuccess.getString(FieldNames.AUTH_TOKEN);
            var error = json.getJSONArray(FieldNames.ERROR);
            assertEquals(0, error.length());

            context.running.set(false);
        });

        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.PASSWORD, USER_1.password);
        request.put(FieldNames.EMAIL, USER_1.email);
        payload.put(0, request);

        runTest(callback, createRequest(RequestType.LOGIN_REQUEST, payload).toString());
    }

    @Test
    public void canLogOut() {

        var callback = createCallback((context, string) -> {
            var json = new JSONObject(string);
            var msgID = json.getInt(FieldNames.ID);
            assertEquals(0, msgID);
            var type = json.getString(FieldNames.TYPE);
            assertEquals(ResponseType.LOGOUT_RESPONSE.toLowerCaseString(), type);
            var success = json.getJSONArray(FieldNames.SUCCESS);
            assertEquals(1, success.length());
            var firstSuccess = success.getJSONObject(0);
            var id = firstSuccess.get(FieldNames.ID);
            assertEquals(0, id);
            var error = json.getJSONArray(FieldNames.ERROR);
            assert (error.length() == 0);

            context.running.set(false);
        });

        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.USER_ID, LOGGED_IN_USER.id);
        payload.put(0, request);
        var json = createRequest(RequestType.LOGOUT_REQUEST, payload);
        json.put(FieldNames.AUTH_TOKEN, LOGGED_IN_USER.token);
        runTest(callback, json.toString());
    }

    @Test
    public void canGetUser() {
        var callback = createCallback((context, string) -> {
            var json = new JSONObject(string);
            var msgID = json.getInt(FieldNames.ID);
            assertEquals(0, msgID);
            var type = json.getString(FieldNames.TYPE);
            assertEquals(ResponseType.GET_USER_RESPONSE.toLowerCaseString(), type);
            var success = json.getJSONArray(FieldNames.SUCCESS);
            assertEquals(1, success.length());
            var firstSuccess = success.getJSONObject(0);
            var id = firstSuccess.get(FieldNames.ID);
            assertEquals(0, id);
            assertEquals(1, firstSuccess.getInt(FieldNames.USER_ID));
            assertEquals(USER_1.username, firstSuccess.getString(FieldNames.USERNAME));
            assertEquals(USER_1.avatarURI, firstSuccess.getString(FieldNames.AVATAR_URI));

            var error = json.getJSONArray(FieldNames.ERROR);
            assert (error.length() == 0);

            context.running.set(false);
        });

        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.USER_ID, USER_1.id);
        payload.put(0, request);
        var json = createRequest(RequestType.GET_USER_REQUEST, payload);
        json.put(FieldNames.AUTH_TOKEN, LOGGED_IN_USER.token);
        runTest(callback, json.toString());
    }

    @Test
    public void canGetUserRange() {
        var callback = createCallback((context, string) -> {
           var json = new JSONObject(string);
           assertEquals(ResponseType.GET_USER_RANGE_RESPONSE, ResponseType.fromString(json.getString(FieldNames.TYPE)));
           var success = json.getJSONArray(FieldNames.SUCCESS);
           assertEquals(1, success.length());
           // Since the tests aren't necessarily run after each other, we only check the first 3 users, because we know what their values are supposed to be.
           var firstPage = success.getJSONObject(0);
           assertEquals(0, firstPage.getInt(FieldNames.ID));
           var users = firstPage.getJSONArray(FieldNames.RANGE);
           var userTruth = new User[]{USER_1, USER_2, USER_3,};
           assertTrue(users.length() >= 3);
           for (int i = 0; i < 3; i++) {
               var user = users.getJSONObject(i);
               assertEquals(userTruth[i].id, user.getInt(FieldNames.USER_ID));
               assertEquals(userTruth[i].username, user.getString(FieldNames.USERNAME));
               assertEquals(userTruth[i].avatarURI, user.getString(FieldNames.AVATAR_URI));
           }

           context.running.set(false);
        });

        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.PAGE_INDEX, 0);
        payload.put(request);
        var json = createRequest(RequestType.GET_USER_RANGE_REQUEST, payload);
        json.put(FieldNames.AUTH_TOKEN, LOGGED_IN_USER.token);
        runTest(callback, json.toString());

        // TODO: Ask Jonas if we can update the API to not say friends, chats, or games for the reponses
        // to the ranges, as that isn't generic.
    }

    @Test
    public void canUpdateUser() {
        var callback = createCallback((context, string) -> {
            if (context.count.get() == 0) {
                var json = new JSONObject(string);
                var msgID = json.getInt(FieldNames.ID);
                assertEquals(0, msgID);
                var type = json.getString(FieldNames.TYPE);
                assertEquals(ResponseType.UPDATE_USER_RESPONSE.toLowerCaseString(), type);
                var success = json.getJSONArray(FieldNames.SUCCESS);
                assertEquals(1, success.length());
                var firstSuccess = success.getJSONObject(0);
                var id = firstSuccess.get(FieldNames.ID);
                assertEquals(0, id);
                var error = json.getJSONArray(FieldNames.ERROR);
                assert (error.length() == 0);

                try {
                    context.socket.send(createGetUserRequest(USER_TO_BE_UPDATED.id, USER_TO_BE_UPDATED.token).toString());
                } catch (IOException e) {
                    fail();
                }
            }
            if (context.count.get() == 1) {
                var json = new JSONObject(string);
                var success = json.getJSONArray(FieldNames.SUCCESS);
                assertEquals(1, success.length());
                var firstUser = success.getJSONObject(0);
                assertEquals("Some Cool Avatar", firstUser.getString(FieldNames.AVATAR_URI));
            }


            context.count.incrementAndGet();
            if (context.count.get() >= 2) {
                context.running.set(false);
            }
        });


        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.USER_ID, USER_TO_BE_UPDATED.id);
        request.put(FieldNames.USERNAME, USER_TO_BE_UPDATED.username);
        request.put(FieldNames.PASSWORD, USER_TO_BE_UPDATED.password);
        request.put(FieldNames.EMAIL, USER_TO_BE_UPDATED.email);
        request.put(FieldNames.AVATAR_URI, "Some Cool Avatar");
        payload.put(0, request);
        var json = createRequest(RequestType.UPDATE_USER_REQUEST, payload, USER_TO_BE_UPDATED.token);
        runTest(callback, json.toString());

    }

    @Test
    public void canDeleteUser() {
        var callback = createCallback((context, string) -> {
            if (context.count.get() == 0) {
                var json = new JSONObject(string);
                var msgID = json.getInt(FieldNames.ID);
                assertEquals(0, msgID);
                var type = json.getString(FieldNames.TYPE);
                assertEquals(ResponseType.DELETE_USER_RESPONSE.toLowerCaseString(), type);
                var success = json.getJSONArray(FieldNames.SUCCESS);
                assertEquals(1, success.length());
                var firstSuccess = success.getJSONObject(0);
                var id = firstSuccess.get(FieldNames.ID);
                assertEquals(0, id);
                var error = json.getJSONArray(FieldNames.ERROR);
                assert (error.length() == 0);

                try {
                    context.socket.send(createGetUserRequest(USER_TO_BE_DELETED.id, USER_TO_BE_DELETED.token).toString());
                } catch (IOException e) {
                    fail();
                }
            }

            if (context.count.get() == 1) {
                var json = new JSONObject(string);
                var error = json.getJSONArray(FieldNames.ERROR);
                assertEquals(1, error.length());
                var firstError = error.getJSONObject(0);
                assertEquals(0, firstError.getInt(FieldNames.ID));
                assertEquals(Error.USER_ID_NOT_FOUND, Error.fromInt(firstError.getJSONArray(FieldNames.CODE).getInt(0)));
            }

            context.count.incrementAndGet();
            if (context.count.get() >= 2) {
                context.running.set(false);
            }
        });

        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.USER_ID, USER_TO_BE_DELETED.id);
        payload.put(0, request);
        var json = createRequest(RequestType.DELETE_USER_REQUEST, payload);
        json.put(FieldNames.AUTH_TOKEN, USER_TO_BE_DELETED.token);
        runTest(callback, json.toString());
    }

    ///////////////////////////////////////////////////////
    /// User Failing tests
    ///////////////////////////////////////////////////////
    @Test
    public void errorOnGetIllegalUserID() {
        var callback = createCallback((context, string) -> {
            var json = new JSONObject(string);
            var error = json.getJSONArray(FieldNames.ERROR);
            assertEquals(1, error.length());
            var firstError = error.getJSONObject(0);
            assertEquals(0, firstError.getInt(FieldNames.ID));
            assertEquals(Error.USER_ID_NOT_FOUND, Error.fromInt(firstError.getJSONArray(FieldNames.CODE).getInt(0)));

            context.running.set(false);
        });

        runTest(callback, createGetUserRequest(500, LOGGED_IN_USER.token).toString());
    }

    @Test
    public void errorOnCreateNonUniqueUserName() {
        var callback = createCallback((context, string) -> {
            var json = new JSONObject(string);
            assertError(Error.NOT_UNIQUE_USERNAME, json);
            context.running.set(false);
        });

        runTest(callback, createUserRequest(USER_1.username, "TotallyUniqueEmail1923@email", USER_1.password).toString());
    }

    @Test
    public void errorOnCreateNonUniqueEmail() {
        var callback = createCallback((context, string) -> {
            var json = new JSONObject(string);
            assertError(Error.NOT_UNIQUE_EMAIL, json);
            context.running.set(false);
        });

        runTest(callback, createUserRequest("TotallyUniqueUsername29843905", USER_1.email, USER_1.password).toString());
    }

    @Test
    public void errorOnUpdateNonUniqueUserName() {
        var callback = createCallback((context, string) -> {
            var json = new JSONObject(string);
            assertError(Error.NOT_UNIQUE_USERNAME, json);
            context.running.set(false);
        });

        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.USER_ID, USER_TO_BE_UPDATED.id);
        request.put(FieldNames.USERNAME, USER_1.username);
        request.put(FieldNames.PASSWORD, USER_TO_BE_UPDATED.password);
        request.put(FieldNames.EMAIL, USER_TO_BE_UPDATED.email);
        request.put(FieldNames.AVATAR_URI, "Some Cool Avatar");
        payload.put(0, request);
        var json = createRequest(RequestType.UPDATE_USER_REQUEST, payload, USER_TO_BE_UPDATED.token);
        runTest(callback, json.toString());
    }

    @Test
    public void errorOnUpdateNonUniqueEmail() {
        var callback = createCallback((context, string) -> {
            var json = new JSONObject(string);
            assertError(Error.NOT_UNIQUE_EMAIL, json);
            context.running.set(false);
        });

        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.USER_ID, USER_TO_BE_UPDATED.id);
        request.put(FieldNames.USERNAME, USER_TO_BE_UPDATED.username);
        request.put(FieldNames.PASSWORD, USER_TO_BE_UPDATED.password);
        request.put(FieldNames.EMAIL, USER_1.email);
        request.put(FieldNames.AVATAR_URI, "Some Cool Avatar");
        payload.put(0, request);
        var json = createRequest(RequestType.UPDATE_USER_REQUEST, payload, USER_TO_BE_UPDATED.token);
        runTest(callback, json.toString());
    }

    @Test
    public void getUnauthorizedErrorOnDeletingSomeoneElsesAccount() {
        var callback = createCallback((context, string) -> {
            var json = new JSONObject(string);
            assertError(Error.UNAUTHORIZED, json);
            context.running.set(false);
        });

        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.USER_ID, USER_1.id);
        payload.put(0, request);
        runTest(callback, createRequest(RequestType.DELETE_USER_REQUEST, payload, LOGGED_IN_USER.token).toString());
    }

    @Test
    public void getUnauthorizedErrorOnUpdatingSomeoneElsesAccount() {
        var callback = createCallback((context, string) -> {
            var json = new JSONObject(string);
            assertError(Error.UNAUTHORIZED, json);
            context.running.set(false);
        });

        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.USER_ID, USER_1.id);
        request.put(FieldNames.USERNAME, "Something");
        request.put(FieldNames.PASSWORD, "Something");
        request.put(FieldNames.AVATAR_URI, "Something");
        request.put(FieldNames.EMAIL, "Something@email.com");
        payload.put(0, request);
        runTest(callback, createRequest(RequestType.UPDATE_USER_REQUEST, payload, LOGGED_IN_USER.token).toString());

    }

    @Test
    public void errorOnLoggingInWithWrongPassword() {
        var callback = createCallback((context, string) -> {
            var json = new JSONObject(string);
            assertError(Error.INVALID_USERNAME_OR_PASSWORD, json);
            context.running.set(false);
        });

        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.PASSWORD, "WRONG PASSWORD");
        request.put(FieldNames.EMAIL, USER_1.email);
        payload.put(0, request);

        runTest(callback, createRequest(RequestType.LOGIN_REQUEST, payload).toString());
    }

    @Test
    public void errorOnLoggingInWithWrongEmail() {
        var callback = createCallback((context, string) -> {
            var json = new JSONObject(string);
            assertError(Error.INVALID_USERNAME_OR_PASSWORD, json);
            context.running.set(false);
        });

        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.PASSWORD, USER_1.password);
        request.put(FieldNames.EMAIL, "WRONG PASSWORD");
        payload.put(0, request);

        runTest(callback, createRequest(RequestType.LOGIN_REQUEST, payload).toString());
    }
}
