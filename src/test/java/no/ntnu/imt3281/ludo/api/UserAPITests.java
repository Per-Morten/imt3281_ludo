package no.ntnu.imt3281.ludo.api;

import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.server.Database;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class UserAPITests {
    private static final Database.User USER_1 = new Database.User(1, "User1", null, "User1@mail.com", null, null, "User1Password");
    private static final Database.User USER_2 = new Database.User(2, "User2", null, "User2@mail.com", null, null, "User2Password");
    private static final Database.User USER_3 = new Database.User(3, "User3", null, "User3@mail.com", null, null, "User3Password");
    private static final Database.User LOGGED_IN_USER = new Database.User(4, "LoggedInUser", null, "LoggedInUser@mail.com", null, null, "LoggedInPassword");
    private static final Database.User USER_TO_BE_UPDATED = new Database.User(5, "UserToBeUpdated", null, "UserToBeUpdated@mail.com", null, null, "UserToBeUpdatedPassword");
    private static final Database.User USER_TO_BE_DELETED = new Database.User(6, "UserToBeDeleted", null, "UserToBeDeleted@mail.com", null, null, "UserToBeDeletedPassword");


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Logger.setLogLevel(Logger.Level.DEBUG);

        TestServer.start();

        var usersToCreate = new Database.User[] {
                USER_1, USER_2, USER_3, LOGGED_IN_USER, USER_TO_BE_UPDATED, USER_TO_BE_DELETED,
        };

        for (var user : usersToCreate) {
            TestUtility.sendPreparationMessageToServer(TestUtility.createUserRequest(user.username, user.email, user.password), null);
        }

        var usersToLogIn = new Database.User[] {
            LOGGED_IN_USER, USER_TO_BE_UPDATED, USER_TO_BE_DELETED,
        };

        for (var user : usersToLogIn) {
            TestUtility.sendPreparationMessageToServer(TestUtility.createLoginRequest(user.email, user.password), (json) -> {
                var success = json.getJSONArray(FieldNames.SUCCESS);
                if (success.length() != 1) {
                    throw new RuntimeException("Did not get a valid log in back");
                }
                user.token = success.getJSONObject(0).getString(FieldNames.AUTH_TOKEN);
            });
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
        var callback = TestUtility.createCallback((context, string) -> {
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

        TestUtility.runTest(TestUtility.createRequest(RequestType.CREATE_USER_REQUEST, payload).toString(), callback);
    }

    @Test
    public void canLogIn() {

        // Desired ID is 1 because we are logging in as the first test user.
        int desiredID = 1;
        var callback = TestUtility.createCallback((context, string) -> {
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

        TestUtility.runTest(TestUtility.createRequest(RequestType.LOGIN_REQUEST, payload).toString(), callback);
    }

    @Test
    public void canLogOut() {

        var callback = TestUtility.createCallback((context, string) -> {
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
        var json = TestUtility.createRequest(RequestType.LOGOUT_REQUEST, payload);
        json.put(FieldNames.AUTH_TOKEN, LOGGED_IN_USER.token);
        TestUtility.runTest(json.toString(), callback);
    }

    @Test
    public void canGetUser() {
        var callback = TestUtility.createCallback((context, string) -> {
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
        var json = TestUtility.createRequest(RequestType.GET_USER_REQUEST, payload);
        json.put(FieldNames.AUTH_TOKEN, LOGGED_IN_USER.token);
        TestUtility.runTest(json.toString(), callback);
    }

    @Test
    public void canGetUserRange() {
        var callback = TestUtility.createCallback((context, string) -> {
           var json = new JSONObject(string);
           assertEquals(ResponseType.GET_USER_RANGE_RESPONSE, ResponseType.fromString(json.getString(FieldNames.TYPE)));
           var success = json.getJSONArray(FieldNames.SUCCESS);
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
        });

        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.PAGE_INDEX, 0);
        payload.put(request);
        var json = TestUtility.createRequest(RequestType.GET_USER_RANGE_REQUEST, payload);
        json.put(FieldNames.AUTH_TOKEN, LOGGED_IN_USER.token);
        TestUtility.runTest(json.toString(), callback);

        // TODO: Ask Jonas if we can update the API to not say friends, chats, or games for the reponses
        // to the ranges, as that isn't generic.
    }

    @Test
    public void canUpdateUser() {
        var callback = TestUtility.createCallback((context, string) -> {
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
                    context.socket.send(TestUtility.createGetUserRequest(USER_TO_BE_UPDATED.id, USER_TO_BE_UPDATED.token).toString());
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
        var json = TestUtility.createRequest(RequestType.UPDATE_USER_REQUEST, payload, USER_TO_BE_UPDATED.token);
        TestUtility.runTest(json.toString(), callback);

    }

    @Test
    public void canDeleteUser() {
        var callback = TestUtility.createCallback((context, string) -> {
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
                    context.socket.send(TestUtility.createGetUserRequest(USER_TO_BE_DELETED.id, USER_TO_BE_DELETED.token).toString());
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
        var json = TestUtility.createRequest(RequestType.DELETE_USER_REQUEST, payload);
        json.put(FieldNames.AUTH_TOKEN, USER_TO_BE_DELETED.token);
        TestUtility.runTest(json.toString(), callback);
    }

    ///////////////////////////////////////////////////////
    /// User Failing tests
    ///////////////////////////////////////////////////////
    @Test
    public void errorOnGetIllegalUserID() {
        var callback = TestUtility.createCallback((context, string) -> {
            var json = new JSONObject(string);
            var error = json.getJSONArray(FieldNames.ERROR);
            assertEquals(1, error.length());
            var firstError = error.getJSONObject(0);
            assertEquals(0, firstError.getInt(FieldNames.ID));
            assertEquals(Error.USER_ID_NOT_FOUND, Error.fromInt(firstError.getJSONArray(FieldNames.CODE).getInt(0)));

            context.running.set(false);
        });

        TestUtility.runTest(TestUtility.createGetUserRequest(500, LOGGED_IN_USER.token).toString(), callback);
    }

    @Test
    public void errorOnCreateNonUniqueUserName() {
        var callback = TestUtility.createCallback((context, string) -> {
            var json = new JSONObject(string);
            TestUtility.assertError(Error.NOT_UNIQUE_USERNAME, json);
            context.running.set(false);
        });

        TestUtility.runTest(TestUtility.createUserRequest(USER_1.username, "TotallyUniqueEmail1923@email", USER_1.password).toString(), callback);
    }

    @Test
    public void errorOnCreateNonUniqueEmail() {
        var callback = TestUtility.createCallback((context, string) -> {
            var json = new JSONObject(string);
            TestUtility.assertError(Error.NOT_UNIQUE_EMAIL, json);
            context.running.set(false);
        });

        TestUtility.runTest(TestUtility.createUserRequest("TotallyUniqueUsername29843905", USER_1.email, USER_1.password).toString(), callback);
    }

    @Test
    public void errorOnUpdateNonUniqueUserName() {
        var callback = TestUtility.createCallback((context, string) -> {
            var json = new JSONObject(string);
            TestUtility.assertError(Error.NOT_UNIQUE_USERNAME, json);
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
        var json = TestUtility.createRequest(RequestType.UPDATE_USER_REQUEST, payload, USER_TO_BE_UPDATED.token);
        TestUtility.runTest(json.toString(), callback);
    }

    @Test
    public void errorOnUpdateNonUniqueEmail() {
        var callback = TestUtility.createCallback((context, string) -> {
            var json = new JSONObject(string);
            TestUtility.assertError(Error.NOT_UNIQUE_EMAIL, json);
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
        var json = TestUtility.createRequest(RequestType.UPDATE_USER_REQUEST, payload, USER_TO_BE_UPDATED.token);
        TestUtility.runTest(json.toString(), callback);
    }

    @Test
    public void getUnauthorizedErrorOnDeletingSomeoneElsesAccount() {
        var callback = TestUtility.createCallback((context, string) -> {
            var json = new JSONObject(string);
            TestUtility.assertError(Error.UNAUTHORIZED, json);
            context.running.set(false);
        });

        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.USER_ID, USER_1.id);
        payload.put(0, request);
        TestUtility.runTest(TestUtility.createRequest(RequestType.DELETE_USER_REQUEST, payload, LOGGED_IN_USER.token).toString(), callback);
    }

    @Test
    public void getUnauthorizedErrorOnUpdatingSomeoneElsesAccount() {
        var callback = TestUtility.createCallback((context, string) -> {
            var json = new JSONObject(string);
            TestUtility.assertError(Error.UNAUTHORIZED, json);
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
        TestUtility.runTest(TestUtility.createRequest(RequestType.UPDATE_USER_REQUEST, payload, LOGGED_IN_USER.token).toString(), callback);

    }

    @Test
    public void errorOnLoggingInWithWrongPassword() {
        var callback = TestUtility.createCallback((context, string) -> {
            var json = new JSONObject(string);
            TestUtility.assertError(Error.INVALID_USERNAME_OR_PASSWORD, json);
            context.running.set(false);
        });

        var payload = new JSONArray();
        var request = new JSONObject();
        request.put(FieldNames.ID, 0);
        request.put(FieldNames.PASSWORD, "WRONG PASSWORD");
        request.put(FieldNames.EMAIL, USER_1.email);
        payload.put(0, request);

        TestUtility.runTest(TestUtility.createRequest(RequestType.LOGIN_REQUEST, payload).toString(), callback);
    }

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

        TestUtility.runTest(TestUtility.createRequest(RequestType.LOGIN_REQUEST, payload).toString(), callback);
    }
}
