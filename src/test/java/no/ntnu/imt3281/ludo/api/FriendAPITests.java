package no.ntnu.imt3281.ludo.api;

import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.server.Database;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class FriendAPITests {

    // Tests aren't guaranteed to be run after each other, and doing that would also lead to hard to maintain tests
    // I could have created the users within the tests as well, but that would quickly be a lot of boiler plate.
    // I rather want to have the database set up in the order I need it to be to test the edge cases.
    private static final Database.User USER_1 = new Database.User(1, "User1", null, "User1@mail.com", null, null, "User1Password");
    private static final Database.User USER_2 = new Database.User(2, "User2", null, "User2@mail.com", null, null, "User2Password");
    private static final Database.User USER_3 = new Database.User(3, "User3", null, "User3@mail.com", null, null, "User3Password");
    private static final Database.User PENDING_FRIEND_USER = new Database.User(4, "PendingFriendUser", null, "Pending@mail.com", null, null, "PendingPassword");
    private static final Database.User SINK = new Database.User(5, "Sink", null, "Sink@mail.com", null, null, "Sink"); // All friend requests go to this person, but they never accept the requests they receive.

    // Named users, Easier for me to visualize what is supposed to happen with proper names, rather than just user 1 and user 2 etc.
    private static final Database.User KARL_FRIEND_OF_FRED = new Database.User(6, "Karl", null, "Karl@mail.com", null, null, "Karl");
    private static final Database.User FRED_FRIEND_OF_KARL = new Database.User(7, "Fred", null, "Fred@mail.com", null, null, "Karl");

    private static final Database.User LISA_TO_UNFRIEND_ARIN = new Database.User(8, "Lisa", null, "Lisa@mail.com", null, null, "Lisa");
    private static final Database.User ARIN_TO_BE_UNFRIENDED_BY_LISA = new Database.User(9, "Arin", null, "Arin@mail.com", null, null, "Lisa");

    private static final Database.User KARI_TO_IGNORE_LINN = new Database.User(10, "Kari", null, "Kari@mail.com", null, null, "Kari");
    private static final Database.User LINN_TO_BE_IGNORED_BY_KARI = new Database.User(11, "Linn", null, "Linn@mail.com", null, null, "Linn");

    private static final Database.User IVAR_ENEMY_OF_AGNES = new Database.User(12, "Ivar", null, "Ivar@mail.com", null, null, "Ivar");
    private static final Database.User AGNES_ENEMY_OF_IVAR = new Database.User(13, "Agnes", null, "Agnes@mail.com", null, null, "Agnes");

    private static final Database.User LARS_TRYING_TO_STOP_BEING_IGNORED_BY_RALF = new Database.User(14, "Lars", null, "Lars@mail.com", null, null, "Lars");
    private static final Database.User RALF_IGNORING_LARS = new Database.User(15, "Ralf", null, "Ralf@mail.com", null, null, "Ralf");

    private static final Database.User ARN_IGNORING_FRANK_WITHOUT_KNOWHING_HIM = new Database.User(16, "Arn", null, "Arn@mail.com", null, null, "Arn");
    private static final Database.User FRANK = new Database.User(17, "Frank", null, "Frank@mail.com", null, null, "Frank");


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Logger.setLogLevel(Logger.Level.DEBUG);

        TestServer.start();

        var users = new Database.User[]{
                USER_1, USER_2, USER_3,
                PENDING_FRIEND_USER,
                SINK,
                KARL_FRIEND_OF_FRED,
                FRED_FRIEND_OF_KARL,
                LISA_TO_UNFRIEND_ARIN, ARIN_TO_BE_UNFRIENDED_BY_LISA,
                KARI_TO_IGNORE_LINN, LINN_TO_BE_IGNORED_BY_KARI,
                IVAR_ENEMY_OF_AGNES, AGNES_ENEMY_OF_IVAR,
                LARS_TRYING_TO_STOP_BEING_IGNORED_BY_RALF,
                RALF_IGNORING_LARS, ARN_IGNORING_FRANK_WITHOUT_KNOWHING_HIM, FRANK
        };

        // Creation and logging in
        for (var user : users) {
            TestUtility.sendPreparationMessageToServer(TestUtility.createUserRequest(user.username, user.email, user.password), null);
            TestUtility.sendPreparationMessageToServer(TestUtility.createLoginRequest(user.email, user.password), (json) -> {
                var success = json.getJSONArray(FieldNames.SUCCESS);
                if (success.length() != 1) {
                    throw new RuntimeException("Did not get a valid log in back");
                }
                user.token = success.getJSONObject(0).getString(FieldNames.AUTH_TOKEN);
            });
        }

        // Create existing friendships
        var existingFriends = new Database.User[][]{
                {FRED_FRIEND_OF_KARL, KARL_FRIEND_OF_FRED},
                {LISA_TO_UNFRIEND_ARIN, ARIN_TO_BE_UNFRIENDED_BY_LISA},
                {IVAR_ENEMY_OF_AGNES, AGNES_ENEMY_OF_IVAR},
        };

        for (var friendPair : existingFriends) {
            for (int i = 0; i < 2; i++) {
                final int idx = i;
                TestUtility.sendPreparationMessageToServer(TestUtility.createLoginRequest(friendPair[i].email, friendPair[i].password), (json) -> {
                    var success = json.getJSONArray(FieldNames.SUCCESS);
                    if (success.length() != 1) {
                        throw new RuntimeException("Did not get a valid log in back");
                    }
                    friendPair[idx].token = success.getJSONObject(0).getString(FieldNames.AUTH_TOKEN);
                });

                TestUtility.sendPreparationMessageToServer(TestUtility.createFriendRelationshipRequest(RequestType.FRIEND_REQUEST, friendPair[i].id, friendPair[(i + 1) % 2].id, friendPair[i].token), null);
            }
        }

        // Create existing requests
        var existingFriendRequests = new Database.User[][]{
                {LINN_TO_BE_IGNORED_BY_KARI, KARI_TO_IGNORE_LINN},
                {LARS_TRYING_TO_STOP_BEING_IGNORED_BY_RALF, RALF_IGNORING_LARS},
        };

        for (var friendPair : existingFriendRequests) {
            final int i = 0;
            TestUtility.sendPreparationMessageToServer(TestUtility.createLoginRequest(friendPair[i].email, friendPair[i].password), (json) -> {
                var success = json.getJSONArray(FieldNames.SUCCESS);
                if (success.length() != 1) {
                    throw new RuntimeException("Did not get a valid log in back");
                }
                friendPair[i].token = success.getJSONObject(0).getString(FieldNames.AUTH_TOKEN);
            });

            TestUtility.sendPreparationMessageToServer(TestUtility.createFriendRelationshipRequest(RequestType.FRIEND_REQUEST, friendPair[i].id, friendPair[(i + 1)].id, friendPair[i].token), null);
        }

        // Create existing "breakups"
        var existingBreakups = new Database.User[][]{
                {IVAR_ENEMY_OF_AGNES, AGNES_ENEMY_OF_IVAR},
        };

        for (var breakup : existingBreakups) {
            for (int i = 0; i < 2; i++) {
                TestUtility.sendPreparationMessageToServer(TestUtility.createFriendRelationshipRequest(RequestType.UNFRIEND_REQUEST, breakup[i].id, breakup[(i + 1) % 2].id, breakup[i].token), null);
            }
        }

        TestUtility.sendPreparationMessageToServer(TestUtility.createFriendRelationshipRequest(RequestType.IGNORE_REQUEST, RALF_IGNORING_LARS.id, LARS_TRYING_TO_STOP_BEING_IGNORED_BY_RALF.id, RALF_IGNORING_LARS.token), null);
    }


    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        TestServer.stop();
    }

    ///////////////////////////////////////////////////////
    /// Friend Sunshine tests
    ///////////////////////////////////////////////////////
    @Test
    public void receiveFriendUpdateEventUponFriendRequest() {
        var callback = TestUtility.createCallback((context, string) -> {
            var json = new JSONObject(string);
            var type = json.getString(FieldNames.TYPE);

            if (ResponseType.fromString(type) != null) {
                var responseType = ResponseType.fromString(type);
                if (responseType == ResponseType.LOGIN_RESPONSE) {
                    PENDING_FRIEND_USER.token = json.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getString(FieldNames.AUTH_TOKEN);
                    try {
                        context.socket.send(TestUtility.createFriendRelationshipRequest(RequestType.FRIEND_REQUEST, PENDING_FRIEND_USER.id, SINK.id, PENDING_FRIEND_USER.token).toString());
                    } catch (IOException e) {
                        fail();
                    }
                    context.count.incrementAndGet();
                }

                if (responseType == ResponseType.FRIEND_RESPONSE) {
                    assertEquals(1, json.getJSONArray(FieldNames.SUCCESS).length());
                    assertEquals(0, json.getJSONArray(FieldNames.ERROR).length());
                    context.count.incrementAndGet();
                }
            }

            if (EventType.fromString(type) != null) {
                assertEquals(0, json.getJSONArray(FieldNames.PAYLOAD).length());
                context.count.incrementAndGet();
            }

            if (context.count.get() >= 3) {
                context.running.set(false);
            }
        });

        TestUtility.runTest(TestUtility.createLoginRequest(PENDING_FRIEND_USER.email, PENDING_FRIEND_USER.password).toString(), callback);
    }

    @Test
    public void canGetFriendRange() {
        var callback = TestUtility.createCallback((context, string) -> {
            var json = new JSONObject(string);
            var type = json.getString(FieldNames.TYPE);
            assertEquals(ResponseType.GET_FRIEND_RANGE_RESPONSE, ResponseType.fromString(type));
            var success = json.getJSONArray(FieldNames.SUCCESS);
            var first = success.getJSONObject(0);
            var range = first.getJSONArray(FieldNames.RANGE);
            var friend = range.getJSONObject(0);
            assertEquals(KARL_FRIEND_OF_FRED.id, friend.getInt(FieldNames.USER_ID));
            assertEquals(KARL_FRIEND_OF_FRED.username, friend.getString(FieldNames.USERNAME));
            assertEquals(FriendStatus.FRIENDED, FriendStatus.fromInt(friend.getInt(FieldNames.STATUS)));

            context.running.set(false);
        });

        TestUtility.runTest(TestUtility.createGetFriendRangeRequest(FRED_FRIEND_OF_KARL.id, FRED_FRIEND_OF_KARL.token, 0).toString(), callback);
    }

    @Test
    public void friendBecomesPendingUponRequest() {
        var callback = TestUtility.createCallback((context, string) -> {
            var json = new JSONObject(string);
            var type = ResponseType.fromString(json.getString(FieldNames.TYPE));
            if (type == ResponseType.FRIEND_RESPONSE) {
                try {
                    context.socket.send(TestUtility.createGetFriendRangeRequest(USER_1.id, USER_1.token, 0).toString());
                } catch (Exception e) {
                    fail();
                }

                context.count.incrementAndGet();
            }

            if (type == ResponseType.GET_FRIEND_RANGE_RESPONSE) {
                var success = json.getJSONArray(FieldNames.SUCCESS);
                var range = success.getJSONObject(0).getJSONArray(FieldNames.RANGE);
                assertTrue(range.length() >= 1);
                assertEquals(FriendStatus.PENDING, FriendStatus.fromInt(range.getJSONObject(0).getInt(FieldNames.STATUS)));
                context.count.incrementAndGet();
            }

            if (context.count.get() >= 2) {
                context.running.set(false);
            }
        });

        TestUtility.runTest(TestUtility.createFriendRelationshipRequest(RequestType.FRIEND_REQUEST, USER_1.id, USER_2.id, USER_1.token).toString(), callback);
    }

    @Test
    public void ifFriend1HasIgnoredFriend2RequestIsAlwaysMarkedAsPending() {
        // Kari ignores Linn
        TestUtility.runTest(TestUtility.createFriendRelationshipRequest(RequestType.IGNORE_REQUEST, KARI_TO_IGNORE_LINN.id, LINN_TO_BE_IGNORED_BY_KARI.id, KARI_TO_IGNORE_LINN.token).toString(),
                (context, string) -> {
                    var json = new JSONObject(string);
                    TestUtility.assertType(ResponseType.IGNORE_RESPONSE.toLowerCaseString(), json);
                    assertEquals(1, json.getJSONArray(FieldNames.SUCCESS).length());

                    context.running.set(false);
                });

        // Ensure that Linn shows up as ignored in Kari's friend range
        TestUtility.runTest(TestUtility.createGetFriendRangeRequest(KARI_TO_IGNORE_LINN.id, KARI_TO_IGNORE_LINN.token, 0).toString(),
                (context, string) -> {
                    var json = new JSONObject(string);
                    TestUtility.assertType(ResponseType.GET_FRIEND_RANGE_RESPONSE.toLowerCaseString(), json);
                    assertEquals(1, json.getJSONArray(FieldNames.SUCCESS).length());
                    assertEquals(FriendStatus.IGNORED.toInt(), json.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getJSONArray(FieldNames.RANGE).getJSONObject(0).getInt(FieldNames.STATUS));

                    context.running.set(false);
                });

        // Ensure that Kari shows up as pending in Linn's friends list
        TestUtility.runTest(TestUtility.createGetFriendRangeRequest(LINN_TO_BE_IGNORED_BY_KARI.id, LINN_TO_BE_IGNORED_BY_KARI.token, 0).toString(),
                (context, string) -> {
                    var json = new JSONObject(string);
                    TestUtility.assertType(ResponseType.GET_FRIEND_RANGE_RESPONSE.toLowerCaseString(), json);
                    assertEquals(1, json.getJSONArray(FieldNames.SUCCESS).length());
                    assertEquals(FriendStatus.PENDING.toInt(), json.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getJSONArray(FieldNames.RANGE).getJSONObject(0).getInt(FieldNames.STATUS));

                    context.running.set(false);
                });
    }

    @Test
    public void ifUnfriendedItShouldNotBeReturnedToClient() {
        // Ensure that Arin does not show up on Lisa's friendlist after she has unfriended him.
        var lisaCallback = TestUtility.createCallback((context, string) -> {
            var json = new JSONObject(string);
            var type = ResponseType.fromString(json.getString(FieldNames.TYPE));
            if (type == ResponseType.UNFRIEND_RESPONSE) {
                context.running.set(false);
            }
        });

        TestUtility.runTest(TestUtility.createFriendRelationshipRequest(RequestType.UNFRIEND_REQUEST, LISA_TO_UNFRIEND_ARIN.id, ARIN_TO_BE_UNFRIENDED_BY_LISA.id, LISA_TO_UNFRIEND_ARIN.token).toString(), lisaCallback);

        TestUtility.runTest(TestUtility.createGetFriendRangeRequest(LISA_TO_UNFRIEND_ARIN.id, LISA_TO_UNFRIEND_ARIN.token, 0).toString(), (context, string) -> {
            var json = new JSONObject(string);
            var type = ResponseType.fromString(json.getString(FieldNames.TYPE));
            if (type == ResponseType.GET_FRIEND_RANGE_RESPONSE) {
                var success = json.getJSONArray(FieldNames.SUCCESS);
                var range = success.getJSONObject(0).getJSONArray(FieldNames.RANGE);
                assertEquals(0, range.length());
                context.running.set(false);
            }

        });

        // Ensure that Lisa does not show up on Arin's friendlist after having been unfriended by her.
        var arinCallback = TestUtility.createCallback((context, string) -> {
            var json = new JSONObject(string);
            TestUtility.assertType(ResponseType.GET_FRIEND_RANGE_RESPONSE.toLowerCaseString(), json);
            var success = json.getJSONArray(FieldNames.SUCCESS);
            var range = success.getJSONObject(0).getJSONArray(FieldNames.RANGE);
            assertEquals(0, range.length());
            context.running.set(false);
        });

        TestUtility.runTest(TestUtility.createGetFriendRangeRequest(ARIN_TO_BE_UNFRIENDED_BY_LISA.id, ARIN_TO_BE_UNFRIENDED_BY_LISA.token, 0).toString(), arinCallback);
    }

    @Test
    public void ifUnfriendedNewFriendRequestCanOccur() {
        var callback = TestUtility.createCallback((context, string) -> {
            var json = new JSONObject(string);
            var type = ResponseType.fromString(json.getString(FieldNames.TYPE));
            if (type == ResponseType.FRIEND_RESPONSE) {
                try {
                    context.socket.send(TestUtility.createGetFriendRangeRequest(IVAR_ENEMY_OF_AGNES.id, IVAR_ENEMY_OF_AGNES.token, 0).toString());
                } catch (Exception e) {
                    fail();
                }

                context.count.incrementAndGet();
            }

            if (type == ResponseType.GET_FRIEND_RANGE_RESPONSE) {
                var success = json.getJSONArray(FieldNames.SUCCESS);
                var range = success.getJSONObject(0).getJSONArray(FieldNames.RANGE);
                assertTrue(range.length() >= 1);
                assertEquals(FriendStatus.PENDING, FriendStatus.fromInt(range.getJSONObject(0).getInt(FieldNames.STATUS)));
                context.count.incrementAndGet();
            }

            if (context.count.get() >= 2) {
                context.running.set(false);
            }
        });

        TestUtility.runTest(TestUtility.createFriendRelationshipRequest(RequestType.FRIEND_REQUEST, IVAR_ENEMY_OF_AGNES.id, AGNES_ENEMY_OF_IVAR.id, IVAR_ENEMY_OF_AGNES.token).toString(), callback);
    }

    ///////////////////////////////////////////////////////
    /// Friend Failing tests
    ///////////////////////////////////////////////////////
    @Test
    public void cannotHaveRelationshipWithSelf() {
        var callback = TestUtility.createCallback((context, string) -> {
            var json = new JSONObject(string);
            TestUtility.assertError(Error.USER_AND_OTHER_ID_IS_SAME, json);
            context.running.set(false);
        });

        TestUtility.runTest(TestUtility.createFriendRelationshipRequest(RequestType.FRIEND_REQUEST, USER_3.id, USER_3.id, USER_3.token).toString(), callback);
        TestUtility.runTest(TestUtility.createFriendRelationshipRequest(RequestType.IGNORE_REQUEST, USER_3.id, USER_3.id, USER_3.token).toString(), callback);
        TestUtility.runTest(TestUtility.createFriendRelationshipRequest(RequestType.UNFRIEND_REQUEST, USER_3.id, USER_3.id, USER_3.token).toString(), callback);
    }

    @Test
    public void cannotIgnoreFriend() {
        var callback = TestUtility.createCallback((context, string) -> {
            var json = new JSONObject(string);
            TestUtility.assertError(Error.USER_IS_FRIEND, json);
            context.running.set(false);
        });

        TestUtility.runTest(TestUtility.createFriendRelationshipRequest(RequestType.IGNORE_REQUEST, KARL_FRIEND_OF_FRED.id, FRED_FRIEND_OF_KARL.id, KARL_FRIEND_OF_FRED.token).toString(), callback);
    }

    @Test
    public void cannotFriendAgainToAcceptOnBehalfOfOtherUser() {
        final var friendRequest = TestUtility.createFriendRelationshipRequest(RequestType.FRIEND_REQUEST, USER_3.id, SINK.id, USER_3.token);
        TestUtility.runTest(friendRequest.toString(), null);
        TestUtility.runTest(friendRequest.toString(), null);

        var callback = TestUtility.createCallback((context, string) -> {

            var json = new JSONObject(string);
            var type = ResponseType.fromString(json.getString(FieldNames.TYPE));
            TestUtility.assertType(ResponseType.GET_FRIEND_RANGE_RESPONSE.toLowerCaseString(), json);
            var range = json.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getJSONArray(FieldNames.RANGE);
            assertEquals(1, range.length());

            var friend = range.getJSONObject(0);

            assertEquals(FriendStatus.PENDING.toInt(), friend.getInt(FieldNames.STATUS));
            context.running.set(false);

        });

        TestUtility.runTest(TestUtility.createGetFriendRangeRequest(USER_3.id, USER_3.token, 0).toString(), callback);
    }

    @Test
    public void cannotUnfriendYourselfAwayFromIgnore() {
        // Lars Unfriends Ralf
        var lars = LARS_TRYING_TO_STOP_BEING_IGNORED_BY_RALF;
        var ralf = RALF_IGNORING_LARS;
        TestUtility.runTest(TestUtility.createFriendRelationshipRequest(RequestType.UNFRIEND_REQUEST, lars.id, ralf.id, lars.token).toString(),
                (context, string) -> {
                    var json = new JSONObject(string);
                    TestUtility.assertType(ResponseType.UNFRIEND_RESPONSE.toLowerCaseString(), json);
                    assertEquals(1, json.getJSONArray(FieldNames.SUCCESS).length());
                    context.running.set(false);
                });


        // Lars Friends Ralf
        TestUtility.runTest(TestUtility.createFriendRelationshipRequest(RequestType.FRIEND_REQUEST, lars.id, ralf.id, lars.token).toString(),
                (context, string) -> {
                    var json = new JSONObject(string);
                    TestUtility.assertType(ResponseType.FRIEND_RESPONSE.toLowerCaseString(), json);
                    assertEquals(1, json.getJSONArray(FieldNames.SUCCESS).length());
                    context.running.set(false);
                });

        // Ralf still don't get Lars as pending on his list
        TestUtility.runTest(TestUtility.createGetFriendRangeRequest(ralf.id, ralf.token, 0).toString(),
                (context, string) -> {
                    var json = new JSONObject(string);
                    TestUtility.assertType(ResponseType.GET_FRIEND_RANGE_RESPONSE.toLowerCaseString(), json);
                    var range = json.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getJSONArray(FieldNames.RANGE);
                    assertEquals(1, range.length());
                    assertEquals(FriendStatus.IGNORED.toInt(), range.getJSONObject(0).getInt(FieldNames.STATUS));
                    context.running.set(false);
                });
    }

    @Test
    public void dontShowUpOnListOfPeopleYouIgnoreWithoutKnowingThem() {

        var arn = ARN_IGNORING_FRANK_WITHOUT_KNOWHING_HIM;
        // Arn ignores Frank
        TestUtility.runTest(TestUtility.createFriendRelationshipRequest(RequestType.IGNORE_REQUEST, arn.id, SINK.id, arn.token).toString(),
                (context, string) -> {
                    var json = new JSONObject(string);
                    TestUtility.assertType(ResponseType.IGNORE_RESPONSE.toLowerCaseString(), json);
                    assertEquals(1, json.getJSONArray(FieldNames.SUCCESS).length());

                    context.running.set(false);
                });

        // Ensure that Frank shows up as ignored in Arns list
        TestUtility.runTest(TestUtility.createGetFriendRangeRequest(arn.id, arn.token, 0).toString(),
                (context, string) -> {
                    var json = new JSONObject(string);
                    TestUtility.assertType(ResponseType.GET_FRIEND_RANGE_RESPONSE.toLowerCaseString(), json);
                    assertEquals(1, json.getJSONArray(FieldNames.SUCCESS).length());
                    assertEquals(FriendStatus.IGNORED.toInt(), json.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getJSONArray(FieldNames.RANGE).getJSONObject(0).getInt(FieldNames.STATUS));

                    context.running.set(false);
                });

        var frank = FRANK;
        // Ensure that Arn does not show up in Franks list
        TestUtility.runTest(TestUtility.createGetFriendRangeRequest(frank.id, frank.token, 0).toString(),
                (context, string) -> {
                    var json = new JSONObject(string);
                    TestUtility.assertType(ResponseType.GET_FRIEND_RANGE_RESPONSE.toLowerCaseString(), json);
                    assertEquals(0, json.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getJSONArray(FieldNames.RANGE).length());

                    context.running.set(false);
                });
    }
}
