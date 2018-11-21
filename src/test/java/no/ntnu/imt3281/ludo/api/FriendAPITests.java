package no.ntnu.imt3281.ludo.api;

import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.server.Database;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
    private static final Database.User SINK = new Database.User(4, "Sink", null, "Sink@mail.com", null, null, "Sink"); // All friend requests go to this person, but they never accept the requests they receive.

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
                USER_1, USER_2, USER_3, SINK,
                KARL_FRIEND_OF_FRED,
                FRED_FRIEND_OF_KARL,
                LISA_TO_UNFRIEND_ARIN, ARIN_TO_BE_UNFRIENDED_BY_LISA,
                KARI_TO_IGNORE_LINN, LINN_TO_BE_IGNORED_BY_KARI,
                IVAR_ENEMY_OF_AGNES, AGNES_ENEMY_OF_IVAR,
                LARS_TRYING_TO_STOP_BEING_IGNORED_BY_RALF,
                RALF_IGNORING_LARS, ARN_IGNORING_FRANK_WITHOUT_KNOWHING_HIM, FRANK
        };

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

        // Create existing friendships
        var existingFriends = new Database.User[][]{
                {FRED_FRIEND_OF_KARL, KARL_FRIEND_OF_FRED},
                {LISA_TO_UNFRIEND_ARIN, ARIN_TO_BE_UNFRIENDED_BY_LISA},
                {IVAR_ENEMY_OF_AGNES, AGNES_ENEMY_OF_IVAR},
        };

        for (var friendPair : existingFriends) {
            for (int i = 0; i < 2; i++) {
                final int idx = i;
                TestUtility.runTestWithExistingUser(friendPair[i], (context, msg) -> {
                    TestUtility.sendMessage(context.socket, TestUtility.createFriendRelationshipRequest(RequestType.FRIEND_REQUEST, context.user.id, friendPair[(idx + 1) % 2].id, context.user.token));
                    context.running.set(false);
                });
            }
        }

        // Create existing requests
        var existingFriendRequests = new Database.User[][]{
                {LINN_TO_BE_IGNORED_BY_KARI, KARI_TO_IGNORE_LINN},
                {LARS_TRYING_TO_STOP_BEING_IGNORED_BY_RALF, RALF_IGNORING_LARS},
        };

        for (var friendPair : existingFriendRequests) {
            final int i = 0;
            TestUtility.runTestWithExistingUser(friendPair[i], (context, msg) -> {
                TestUtility.sendMessage(context.socket, TestUtility.createFriendRelationshipRequest(RequestType.FRIEND_REQUEST, context.user.id, friendPair[(i + 1)].id, context.user.token));
                context.running.set(false);
            });
        }

        // Create existing "breakups"
        var existingBreakups = new Database.User[][]{
                {IVAR_ENEMY_OF_AGNES, AGNES_ENEMY_OF_IVAR},
        };

        for (var breakup : existingBreakups) {
            for (int i = 0; i < 2; i++) {
                final var idx = i;
                TestUtility.runTestWithExistingUser(breakup[i], (context, msg) -> {
                    TestUtility.sendMessage(context.socket, TestUtility.createFriendRelationshipRequest(RequestType.UNFRIEND_REQUEST, context.user.id, breakup[(idx + 1) % 2].id, context.user.token));
                    context.running.set(false);
                });
            }
        }

        TestUtility.runTestWithExistingUser(RALF_IGNORING_LARS, (context, msg) -> {
            TestUtility.sendMessage(context.socket, TestUtility.createFriendRelationshipRequest(RequestType.IGNORE_REQUEST, context.user.id, LARS_TRYING_TO_STOP_BEING_IGNORED_BY_RALF.id, context.user.token));
            context.running.set(false);
        });

        //TestUtility.sendPreparationMessageToServer(TestUtility.createFriendRelationshipRequest(RequestType.IGNORE_REQUEST, RALF_IGNORING_LARS.id, LARS_TRYING_TO_STOP_BEING_IGNORED_BY_RALF.id, RALF_IGNORING_LARS.token), null);
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
        TestUtility.runTestWithNewUser("USER_TO_TEST_EVENT_RECEPTION", "USER_TO_TEST_EVENT_RECEPTION@mail.com", null, (context, msg) -> {
            Logger.log(Logger.Level.DEBUG, "Received response: %s", msg.toString());

            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createFriendRelationshipRequest(RequestType.FRIEND_REQUEST, context.user.id, SINK.id, context.user.token));
                context.count.incrementAndGet();
            }

            if (ResponseType.fromString(type) == ResponseType.FRIEND_RESPONSE) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                assertEquals(0, msg.getJSONArray(FieldNames.ERROR).length());
                context.count.incrementAndGet();
            }

            if (EventType.fromString(type) != null) {
                assertEquals(0, msg.getJSONArray(FieldNames.PAYLOAD).length());
                context.count.incrementAndGet();
            }

            if (context.count.get() >= 3) {
                context.running.set(false);
            }
        });
    }

    @Test
    public void canGetFriendRange() {
        TestUtility.runTestWithExistingUser(FRED_FRIEND_OF_KARL, (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createGetFriendRangeRequest(context.user.id, context.user.token, 0));
            }
            if (ResponseType.fromString(type) == ResponseType.GET_FRIEND_RANGE_RESPONSE) {
                var success = msg.getJSONArray(FieldNames.SUCCESS);
                var first = success.getJSONObject(0);
                var range = first.getJSONArray(FieldNames.RANGE);
                var friend = range.getJSONObject(0);
                assertEquals(KARL_FRIEND_OF_FRED.id, friend.getInt(FieldNames.USER_ID));
                assertEquals(KARL_FRIEND_OF_FRED.username, friend.getString(FieldNames.USERNAME));
                assertEquals(FriendStatus.FRIENDED, FriendStatus.fromInt(friend.getInt(FieldNames.STATUS)));
                context.running.set(false);
            }
        });
    }

    @Test
    public void friendBecomesPendingUponRequest() {
        TestUtility.runTestWithExistingUser(USER_1, (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createFriendRelationshipRequest(RequestType.FRIEND_REQUEST, context.user.id, USER_2.id, context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.FRIEND_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createGetFriendRangeRequest(context.user.id, context.user.token, 0));
            }

            if (ResponseType.fromString(type) == ResponseType.GET_FRIEND_RANGE_RESPONSE) {
                var success = msg.getJSONArray(FieldNames.SUCCESS);
                var range = success.getJSONObject(0).getJSONArray(FieldNames.RANGE);
                assertTrue(range.length() >= 1);
                assertEquals(FriendStatus.PENDING, FriendStatus.fromInt(range.getJSONObject(0).getInt(FieldNames.STATUS)));
                context.running.set(false);
            }
        });
    }

    @Test
    public void ifFriend1HasIgnoredFriend2RequestIsAlwaysMarkedAsPending() {
        // Kari ignores Linn
        TestUtility.runTestWithExistingUser(KARI_TO_IGNORE_LINN, (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createFriendRelationshipRequest(RequestType.IGNORE_REQUEST, context.user.id, LINN_TO_BE_IGNORED_BY_KARI.id, context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.IGNORE_RESPONSE) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                TestUtility.sendMessage(context.socket, TestUtility.createGetFriendRangeRequest(context.user.id, context.user.token, 0));
            }

            // Ensure Linn shows up as ignored on Kari's list
            if (ResponseType.fromString(type) == ResponseType.GET_FRIEND_RANGE_RESPONSE) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                assertEquals(FriendStatus.IGNORED.toInt(), msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getJSONArray(FieldNames.RANGE).getJSONObject(0).getInt(FieldNames.STATUS));
                context.running.set(false);
            }
        });

        // Ensure that Kari shows up as pending in Linn's friends list
        TestUtility.runTestWithExistingUser(LINN_TO_BE_IGNORED_BY_KARI, (context, msg) -> {

            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createGetFriendRangeRequest(context.user.id, context.user.token, 0));
            }

            if (ResponseType.fromString(type) == ResponseType.GET_FRIEND_RANGE_RESPONSE) {
                TestUtility.assertType(ResponseType.GET_FRIEND_RANGE_RESPONSE.toLowerCaseString(), msg);
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                assertEquals(FriendStatus.PENDING.toInt(), msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getJSONArray(FieldNames.RANGE).getJSONObject(0).getInt(FieldNames.STATUS));

                context.running.set(false);
            }
        });
    }

    @Test
    public void ifUnfriendedItShouldNotBeReturnedToClient() {
        // Ensure that Arin does not show up on Lisa's friendlist after she has unfriended him.
        TestUtility.runTestWithExistingUser(LISA_TO_UNFRIEND_ARIN, (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createFriendRelationshipRequest(RequestType.UNFRIEND_REQUEST, context.user.id, ARIN_TO_BE_UNFRIENDED_BY_LISA.id, context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.UNFRIEND_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createGetFriendRangeRequest(context.user.id, context.user.token, 0));
            }

            if (ResponseType.fromString(type) == ResponseType.GET_FRIEND_RANGE_RESPONSE) {
                var success = msg.getJSONArray(FieldNames.SUCCESS);
                var range = success.getJSONObject(0).getJSONArray(FieldNames.RANGE);
                assertEquals(0, range.length());
                context.running.set(false);
            }
        });

        // Ensure that Lisa does not show up on Arin's friendlist after having been unfriended by her.
        TestUtility.runTestWithExistingUser(LISA_TO_UNFRIEND_ARIN, (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createGetFriendRangeRequest(context.user.id, context.user.token, 0));
            }

            if (ResponseType.fromString(type) == ResponseType.GET_FRIEND_RANGE_RESPONSE) {
                var success = msg.getJSONArray(FieldNames.SUCCESS);
                var range = success.getJSONObject(0).getJSONArray(FieldNames.RANGE);
                assertEquals(0, range.length());
                context.running.set(false);
            }
        });
    }

    @Test
    public void ifUnfriendedNewFriendRequestCanOccur() {
        TestUtility.runTestWithExistingUser(IVAR_ENEMY_OF_AGNES, (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createFriendRelationshipRequest(RequestType.FRIEND_REQUEST, context.user.id, AGNES_ENEMY_OF_IVAR.id, context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.FRIEND_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createGetFriendRangeRequest(context.user.id, context.user.token, 0));
            }

            if (ResponseType.fromString(type) == ResponseType.GET_FRIEND_RANGE_RESPONSE) {
                var success = msg.getJSONArray(FieldNames.SUCCESS);
                var range = success.getJSONObject(0).getJSONArray(FieldNames.RANGE);
                assertTrue(range.length() >= 1);
                assertEquals(FriendStatus.PENDING, FriendStatus.fromInt(range.getJSONObject(0).getInt(FieldNames.STATUS)));
                context.running.set(false);
            }
        });
    }

    ///////////////////////////////////////////////////////
    /// Friend Failing tests
    ///////////////////////////////////////////////////////
    @Test
    public void cannotHaveRelationshipWithSelf() {
        var requests = new RequestType[]{
            RequestType.FRIEND_REQUEST, RequestType.IGNORE_REQUEST, RequestType.UNFRIEND_REQUEST,
        };

        for (var request : requests) {
            TestUtility.runTestWithExistingUser(USER_3, (context, msg) -> {
                var type = msg.getString(FieldNames.TYPE);
                if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                    TestUtility.sendMessage(context.socket, TestUtility.createFriendRelationshipRequest(request, context.user.id, USER_3.id, context.user.token));
                } else {
                    TestUtility.assertError(Error.USER_AND_OTHER_ID_IS_SAME, msg);
                    context.running.set(false);
                }
            });
        }
    }

    @Test
    public void cannotHaveRelationshipWithNonExistingUser() {
        var requests = new RequestType[]{
                RequestType.FRIEND_REQUEST, RequestType.IGNORE_REQUEST, RequestType.UNFRIEND_REQUEST,
        };

        for (var request : requests) {
            TestUtility.runTestWithExistingUser(USER_3, (context, msg) -> {
                var type = msg.getString(FieldNames.TYPE);
                if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                    TestUtility.sendMessage(context.socket, TestUtility.createFriendRelationshipRequest(request, context.user.id, 1000, context.user.token));
                } else {
                    TestUtility.assertError(Error.OTHER_ID_NOT_FOUND, msg);
                    context.running.set(false);
                }
            });
        }
    }

    @Test
    public void cannotIgnoreFriend() {
        TestUtility.runTestWithExistingUser(KARL_FRIEND_OF_FRED, (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket,TestUtility.createFriendRelationshipRequest(RequestType.IGNORE_REQUEST, context.user.id, FRED_FRIEND_OF_KARL.id, context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.IGNORE_RESPONSE) {
                TestUtility.assertError(Error.USER_IS_FRIEND, msg);
                context.running.set(false);
            }
        });
    }

    @Test
    public void cannotFriendAgainToAcceptOnBehalfOfOtherUser() {
        TestUtility.runTestWithExistingUser(USER_3, (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createFriendRelationshipRequest(RequestType.FRIEND_REQUEST, context.user.id, SINK.id, context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.FRIEND_RESPONSE) {
                if (context.count.getAndIncrement() < 1) {
                    TestUtility.sendMessage(context.socket, TestUtility.createFriendRelationshipRequest(RequestType.FRIEND_REQUEST, context.user.id, SINK.id, context.user.token));
                } else {
                    TestUtility.sendMessage(context.socket, TestUtility.createGetFriendRangeRequest(context.user.id, context.user.token, 0));
                }
            }

            if (ResponseType.fromString(type) == ResponseType.GET_FRIEND_RANGE_RESPONSE) {
                var range = msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getJSONArray(FieldNames.RANGE);
                assertEquals(1, range.length());

                var friend = range.getJSONObject(0);

                assertEquals(FriendStatus.PENDING.toInt(), friend.getInt(FieldNames.STATUS));
                context.running.set(false);
            }
        });
    }

    @Test
    public void cannotUnfriendYourselfAwayFromIgnore() {
        var lars = LARS_TRYING_TO_STOP_BEING_IGNORED_BY_RALF;
        var ralf = RALF_IGNORING_LARS;

        // Lars Unfriends Ralf
        TestUtility.runTestWithExistingUser(lars, (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createFriendRelationshipRequest(RequestType.UNFRIEND_REQUEST, context.user.id, ralf.id, context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.UNFRIEND_RESPONSE) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                TestUtility.sendMessage(context.socket, TestUtility.createFriendRelationshipRequest(RequestType.FRIEND_REQUEST, context.user.id, ralf.id, context.user.token));
            }

            // Lars Friends Ralf
            if (ResponseType.fromString(type) == ResponseType.FRIEND_RESPONSE) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                context.running.set(false);
            }
        });

        TestUtility.runTestWithExistingUser(RALF_IGNORING_LARS, (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createGetFriendRangeRequest(context.user.id, context.user.token, 0));
            }

            // Ralf still don't get Lars as pending on his list
            if (ResponseType.fromString(type) == ResponseType.GET_FRIEND_RANGE_RESPONSE) {
                var range = msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getJSONArray(FieldNames.RANGE);
                assertEquals(1, range.length());
                assertEquals(FriendStatus.IGNORED.toInt(), range.getJSONObject(0).getInt(FieldNames.STATUS));
                context.running.set(false);
            }
        });
    }

    @Test
    public void dontShowUpOnListOfPeopleYouIgnoreWithoutKnowingThem() {
        TestUtility.runTestWithExistingUser(ARN_IGNORING_FRANK_WITHOUT_KNOWHING_HIM, (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);

            // Arn ignores Frank
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createFriendRelationshipRequest(RequestType.IGNORE_REQUEST, context.user.id, FRANK.id, context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.IGNORE_RESPONSE) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                TestUtility.sendMessage(context.socket, TestUtility.createGetFriendRangeRequest(context.user.id, context.user.token, 0));
            }

            // Ensure that Frank shows up as ignored in Arns list
            if (ResponseType.fromString(type) == ResponseType.GET_FRIEND_RANGE_RESPONSE) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                assertEquals(FriendStatus.IGNORED.toInt(), msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getJSONArray(FieldNames.RANGE).getJSONObject(0).getInt(FieldNames.STATUS));
                context.running.set(false);
            }
        });

        var frank = FRANK;
        // Ensure that Arn does not show up in Franks list
        TestUtility.runTestWithExistingUser(FRANK, (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);

            // Arn ignores Frank
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createGetFriendRangeRequest(context.user.id, context.user.token, 0));
            }

            if (ResponseType.fromString(type) == ResponseType.GET_FRIEND_RANGE_RESPONSE) {
                assertEquals(0, msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getJSONArray(FieldNames.RANGE).length());
                context.running.set(false);
            }
        });
    }
}
