package no.ntnu.imt3281.ludo.api;

import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.server.Database;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class UserAPITests {
    private static final Database.User USER_1 = new Database.User(1, "User1", null, "User1@mail.com", null, null, "User1Password");
    private static final Database.User USER_2 = new Database.User(2, "User2", null, "User2@mail.com", null, null, "User2Password");
    private static final Database.User USER_3 = new Database.User(3, "User3", null, "User3@mail.com", null, null, "User3Password");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Logger.setLogLevel(Logger.Level.DEBUG);

        TestServer.start();

        var usersToCreate = new Database.User[]{
                USER_1, USER_2, USER_3,
        };

        for (var user : usersToCreate) {
            TestUtility.runTestWithNewUser(user.username, user.email, user.password, null);
            //TestUtility.sendPreparationMessageToServer(TestUtility.createUserRequest(user.username, user.email, user.password), null);
        }
    }


    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        TestServer.stop();
    }

    ///////////////////////////////////////////////////////
    /// User Sunshine tests
    ///////////////////////////////////////////////////////
    @Test
    public void canCreateUser() {
        TestUtility.runTestWithNewUser("USER_TO_TEST_CREATE", "USER_TO_TEST_CREATE@mail", "password", (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.CREATE_USER_RESPONSE) {
                var successes = msg.getJSONArray(FieldNames.SUCCESS);
                assertEquals(1, successes.length());
                context.running.set(false);
            }
        });
    }

    @Test
    public void canLogIn() {
        TestUtility.runTestWithNewUser("USER_TO_TEST_LOGIN", "USER_TO_TEST_LOGIN@mail", "password", (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                var successes = msg.getJSONArray(FieldNames.SUCCESS);
                assertEquals(1, successes.length());
                context.running.set(false);
            }
        });
    }

    @Test
    public void canLogOut() {
        TestUtility.runTestWithNewUser("USER_TO_LOG_OUT", "USER_TO_LOG_OUT@mail", "pwd", (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                var payload = new JSONArray();
                var request = new JSONObject();
                request.put(FieldNames.ID, context.user.id);
                request.put(FieldNames.USER_ID, context.user.id);
                payload.put(0, request);
                var json = TestUtility.createRequest(RequestType.LOGOUT_REQUEST, payload);
                json.put(FieldNames.AUTH_TOKEN, context.user.token);
                TestUtility.sendMessage(context.socket, json);
            }

            if (ResponseType.fromString(type) == ResponseType.LOGOUT_RESPONSE) {
                var msgID = msg.getInt(FieldNames.ID);
                assertEquals(0, msgID);
                var success = msg.getJSONArray(FieldNames.SUCCESS);
                assertEquals(1, success.length());
                var firstSuccess = success.getJSONObject(0);
                var id = firstSuccess.get(FieldNames.ID);
                assertEquals(context.user.id, id);
                var error = msg.getJSONArray(FieldNames.ERROR);
                assert (error.length() == 0);

                context.running.set(false);
            }
        });
    }

    @Test
    public void canGetUser() {
        TestUtility.runTestWithNewUser("USER_TO_CHECK_GET", "USER_TO_CHECK_GET@mail.com", "password", (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createGetUserRequest(USER_1.id, context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.GET_USER_RESPONSE) {
                var msgID = msg.getInt(FieldNames.ID);
                assertEquals(0, msgID);
                var success = msg.getJSONArray(FieldNames.SUCCESS);
                assertEquals(1, success.length());
                var firstSuccess = success.getJSONObject(0);
                var id = firstSuccess.get(FieldNames.ID);
                assertEquals(0, id);
                assertEquals(1, firstSuccess.getInt(FieldNames.USER_ID));
                assertEquals(USER_1.username, firstSuccess.getString(FieldNames.USERNAME));
                assertEquals(USER_1.avatarURI, firstSuccess.getString(FieldNames.AVATAR_URI));

                var error = msg.getJSONArray(FieldNames.ERROR);
                assert (error.length() == 0);

                context.running.set(false);
            }
        });
    }

    @Test
    public void canGetUserRange() {
        TestUtility.runTestWithNewUser("USER_TO_CHECK_GET_RANGE", "USER_TO_CHECK_GET_RANGE@mail.com", "password", (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                var payload = new JSONArray();
                var request = new JSONObject();
                request.put(FieldNames.ID, 0);
                request.put(FieldNames.PAGE_INDEX, 0);
                payload.put(request);
                var json = TestUtility.createRequest(RequestType.GET_USER_RANGE_REQUEST, payload);
                json.put(FieldNames.AUTH_TOKEN, context.user.token);

                TestUtility.sendMessage(context.socket, json);
            }

            if (ResponseType.fromString(type) == ResponseType.GET_USER_RANGE_RESPONSE) {
                Logger.log(Logger.Level.DEBUG, "Received message here: %s", msg.toString());
                var success = msg.getJSONArray(FieldNames.SUCCESS);
                assertEquals(1, success.length());
                // Since the tests aren't necessarily run after each other, we only check the first 3 users, because we know what their values are supposed to be.
                var firstPage = success.getJSONObject(0);
                assertEquals(0, firstPage.getInt(FieldNames.ID));
                var users = firstPage.getJSONArray(FieldNames.RANGE);
                var userTruth = new Database.User[]{USER_1, USER_2, USER_3,};
                assertTrue(users.length() >= 3);
                for (int i = 0; i < 3; i++) {
                    var user = users.getJSONObject(i);
                    assertEquals(userTruth[i].id, user.getInt(FieldNames.USER_ID));
                    assertEquals(userTruth[i].username, user.getString(FieldNames.USERNAME));
                    assertEquals(userTruth[i].avatarURI, user.getString(FieldNames.AVATAR_URI));
                }

                context.running.set(false);
            }
        });
    }

    @Test
    public void canUpdateUser() {
        TestUtility.runTestWithNewUser("USER_TO_TEST_UPDATE", "USER_TO_TEST_UPDATE@mail.com", "password", (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                var payload = new JSONArray();
                var request = new JSONObject();
                request.put(FieldNames.ID, 0);
                request.put(FieldNames.USER_ID, context.user.id);
                request.put(FieldNames.USERNAME, context.user.username);
                request.put(FieldNames.PASSWORD, context.user.password);
                request.put(FieldNames.EMAIL, context.user.email);
                request.put(FieldNames.AVATAR_URI, "Some Cool Avatar");
                payload.put(0, request);
                TestUtility.sendMessage(context.socket, TestUtility.createRequest(RequestType.UPDATE_USER_REQUEST, payload, context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.UPDATE_USER_RESPONSE) {
                var success = msg.getJSONArray(FieldNames.SUCCESS);
                assertEquals(1, success.length());
                TestUtility.sendMessage(context.socket, TestUtility.createGetUserRequest(context.user.id, context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.GET_USER_RESPONSE) {
                Logger.log(Logger.Level.DEBUG, "Got message: %s", msg.toString());
                var success = msg.getJSONArray(FieldNames.SUCCESS);
                assertEquals(1, success.length());
                var firstUser = success.getJSONObject(0);
                assertEquals("Some Cool Avatar", firstUser.getString(FieldNames.AVATAR_URI));
                context.running.set(false);
            }
        });
    }

    @Test
    public void canDeleteUser() {
        // Hate using atomic integers this way, but I cannot modify an Integer class within the callback (Java, you are stupid at times!))
        final var userID = new AtomicInteger(0);

        TestUtility.runTestWithNewUser("USER_TO_BE_DELETED", "USER_TO_BE_DELETED@mail", "password", (context, message) -> {
            var type = message.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                var payload = new JSONArray();
                var request = new JSONObject();
                request.put(FieldNames.ID, 0);
                request.put(FieldNames.USER_ID, context.user.id);
                payload.put(0, request);
                var json = TestUtility.createRequest(RequestType.DELETE_USER_REQUEST, payload);
                json.put(FieldNames.AUTH_TOKEN, context.user.token);
                TestUtility.sendMessage(context.socket, json);
            }

            if (ResponseType.fromString(type) == ResponseType.DELETE_USER_RESPONSE) {
                var msgID = message.getInt(FieldNames.ID);
                assertEquals(0, msgID);
                var success = message.getJSONArray(FieldNames.SUCCESS);
                assertEquals(1, success.length());
                var firstSuccess = success.getJSONObject(0);
                var id = firstSuccess.get(FieldNames.ID);
                assertEquals(0, id);
                var error = message.getJSONArray(FieldNames.ERROR);
                assert (error.length() == 0);

                userID.set(context.user.id);
                context.running.set(false);
            }
        });

        TestUtility.runTestWithNewUser("USER_TO_CHECK_DELETED", "USER_TO_CHECK_DELETED@mail.com", "Password", (context, message) -> {
            var type = message.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createGetUserRequest(userID.get(), context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.GET_USER_RESPONSE) {
                TestUtility.assertError(Error.USER_ID_NOT_FOUND, message);
                context.running.set(false);
            }
        });
    }

    ///////////////////////////////////////////////////////
    /// User Failing tests
    ///////////////////////////////////////////////////////
    @Test
    public void errorOnGetIllegalUserID() {
        TestUtility.runTestWithNewUser("USER_TO_TEST_ILLEGAL_ID", "USER_TO_TEST_ILLEGAL_ID@mail.com", "password", (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);

            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createGetUserRequest(500, context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.GET_USER_RESPONSE) {
                TestUtility.assertError(Error.USER_ID_NOT_FOUND, msg);
                context.running.set(false);
            }
        });
    }

    @Test
    public void errorOnCreateNonUniqueUserName() {
        var callback = TestUtility.createCallback((context, string) -> {
            var json = new JSONObject(string);
            TestUtility.assertError(Error.NOT_UNIQUE_USERNAME, json);
            context.running.set(false);
        });

        TestUtility.runTestNotRequiringLogin(TestUtility.createUserRequest(USER_1.username, "TotallyUniqueEmail1923@email", USER_1.password).toString(), callback);
    }

    @Test
    public void errorOnCreateNonUniqueEmail() {
        var callback = TestUtility.createCallback((context, string) -> {
            var json = new JSONObject(string);
            TestUtility.assertError(Error.NOT_UNIQUE_EMAIL, json);
            context.running.set(false);
        });

        TestUtility.runTestNotRequiringLogin(TestUtility.createUserRequest("TotallyUniqueUsername29843905", USER_1.email, USER_1.password).toString(), callback);
    }

    @Test
    public void errorOnUpdateNonUniqueUserName() {
        TestUtility.runTestWithNewUser("USER_TO_TEST_NON_UNIQUE_USERNAME_UPDATE", "USER_TO_TEST_NON_UNIQUE_USERNAME_UPDATE@mail.com", "password", (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                var payload = new JSONArray();
                var request = new JSONObject();
                request.put(FieldNames.ID, 0);
                request.put(FieldNames.USER_ID, context.user.id);
                request.put(FieldNames.USERNAME, USER_1.username);
                request.put(FieldNames.PASSWORD, context.user.password);
                request.put(FieldNames.EMAIL, context.user.email);
                request.put(FieldNames.AVATAR_URI, "Some Cool Avatar");
                payload.put(0, request);
                TestUtility.sendMessage(context.socket, TestUtility.createRequest(RequestType.UPDATE_USER_REQUEST, payload, context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.UPDATE_USER_RESPONSE) {
                TestUtility.assertError(Error.NOT_UNIQUE_USERNAME, msg);
                context.running.set(false);
            }
        });
    }

    @Test
    public void errorOnUpdateNonUniqueEmail() {
        TestUtility.runTestWithNewUser("USER_TO_TEST_NON_UNIQUE_EMAIL_UPDATE", "USER_TO_TEST_NON_UNIQUE_EMAIL_UPDATE@mail.com", "password", (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                var payload = new JSONArray();
                var request = new JSONObject();
                request.put(FieldNames.ID, 0);
                request.put(FieldNames.USER_ID, context.user.id);
                request.put(FieldNames.USERNAME, context.user.username);
                request.put(FieldNames.PASSWORD, context.user.password);
                request.put(FieldNames.EMAIL, USER_1.email);
                request.put(FieldNames.AVATAR_URI, "Some Cool Avatar");
                payload.put(0, request);
                TestUtility.sendMessage(context.socket, TestUtility.createRequest(RequestType.UPDATE_USER_REQUEST, payload, context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.UPDATE_USER_RESPONSE) {
                TestUtility.assertError(Error.NOT_UNIQUE_EMAIL, msg);
                context.running.set(false);
            }
        });
    }

    @Test
    public void getUnauthorizedErrorOnDeletingSomeoneElsesAccount() {
        TestUtility.runTestWithNewUser("USER_TO_TEST_DELETING_SOMEONE_ELSES_ACCOUNT", "USER_TO_TEST_DELETING_SOMEONE_ELSES_ACCOUNT@mail.com", "password", (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                var payload = new JSONArray();
                var request = new JSONObject();
                request.put(FieldNames.ID, 0);
                request.put(FieldNames.USER_ID, USER_1.id);
                payload.put(0, request);
                TestUtility.sendMessage(context.socket, TestUtility.createRequest(RequestType.DELETE_USER_REQUEST, payload, context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.DELETE_USER_RESPONSE) {
                TestUtility.assertError(Error.UNAUTHORIZED, msg);
                context.running.set(false);
            }
        });
    }

    @Test
    public void getUnauthorizedErrorOnUpdatingSomeoneElsesAccount() {
        TestUtility.runTestWithNewUser("USER_TO_TEST_UPDATING_SOMEONE_ELSE", "USER_TO_TEST_UPDATING_SOMEONE_ELSE@mail.com", "password", (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                var payload = new JSONArray();
                var request = new JSONObject();
                request.put(FieldNames.ID, 0);
                request.put(FieldNames.USER_ID, USER_1.id);
                request.put(FieldNames.USERNAME, context.user.username);
                request.put(FieldNames.PASSWORD, context.user.password);
                request.put(FieldNames.EMAIL, USER_1.email);
                request.put(FieldNames.AVATAR_URI, "Some Cool Avatar");
                payload.put(0, request);
                TestUtility.sendMessage(context.socket, TestUtility.createRequest(RequestType.UPDATE_USER_REQUEST, payload, context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.UPDATE_USER_RESPONSE) {
                TestUtility.assertError(Error.UNAUTHORIZED, msg);
                context.running.set(false);
            }
        });
    }

    @Test
    public void errorOnLoggingInWithWrongPassword() {
        TestUtility.runTestWithNewUser("USER_TO_TEST_INVALID_PASSWORD", "USER_TO_TEST_INVALID_PASSWORD@mail.com", "password", (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);

            Logger.log(Logger.Level.DEBUG, "Got message: %s", msg.toString());
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                if (context.count.get() == 0) {
                    var payload = new JSONArray();
                    var request = new JSONObject();
                    request.put(FieldNames.ID, 0);
                    request.put(FieldNames.PASSWORD, "WRONG PASSWORD");
                    request.put(FieldNames.EMAIL, context.user.email);
                    payload.put(0, request);
                    TestUtility.sendMessage(context.socket, TestUtility.createRequest(RequestType.LOGIN_REQUEST, payload));
                    context.count.incrementAndGet();

                } else if (context.count.get() == 1) {
                    TestUtility.assertError(Error.INVALID_USERNAME_OR_PASSWORD, msg);
                    context.running.set(false);
                }
            }
        });
    }

    // TODO: Test receive forced logout event

    @Test
    public void errorOnLoggingInWithWrongEmail() {
        var callback = TestUtility.createCallback((context, string) -> {
            var json = new JSONObject(string);
            TestUtility.assertError(Error.INVALID_USERNAME_OR_PASSWORD, json);
            context.running.set(false);
        });

        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.PASSWORD, USER_1.password);
        request.put(FieldNames.EMAIL, "WRONG PASSWORD");
        payload.put(0, request);

        TestUtility.runTestNotRequiringLogin(TestUtility.createRequest(RequestType.LOGIN_REQUEST, payload).toString(), callback);
    }
}
