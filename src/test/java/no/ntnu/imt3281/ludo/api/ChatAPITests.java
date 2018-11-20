package no.ntnu.imt3281.ludo.api;

import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.server.Database;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ChatAPITests {

    // Tests aren't guaranteed to be run after each other, and doing that would also lead to hard to maintain tests
    // I could have created the users within the tests as well, but that would quickly be a lot of boiler plate.
    // I rather want to have the database set up in the order I need it to be to test the edge cases.
    // Friend Group
    private static final Database.User KARL = new Database.User(1, "Karl", null, "Karl@mail.com", null, null, "KarlPassword");
    private static final Database.User FRIDA = new Database.User(2, "Frida", null, "Frida@mail.com", null, null, "FridaPassword");
    private static final Database.User HENRIK = new Database.User(3, "Henrik", null, "Henrik@mail.com", null, null, "HenrikPassword");

    // Loner who cannot be invited by others.
    private static final Database.User ARIN_LONER = new Database.User(4, "Arin", null, "Arin@mail.com", null, null, "ArinPassword");

    // People who are ignored
    private static final Database.User RALF_IGNORED_BY_KARL = new Database.User(4, "Ralf", null, "Ralf@mail.com", null, null, "RalfPassword");


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Logger.setLogLevel(Logger.Level.DEBUG);

        TestServer.start();

        var users = new Database.User[]{
                KARL, FRIDA, HENRIK, ARIN_LONER, RALF_IGNORED_BY_KARL
        };

        // Create existing friendships
        var existingFriends = new Database.User[][]{
                {KARL, FRIDA},
                {HENRIK, KARL},
                {FRIDA, HENRIK},
        };

        // Create existing requests
        var existingFriendRequests = new Database.User[][]{
                {RALF_IGNORED_BY_KARL, KARL},
        };

        // Create existing "breakups"
        var existingBreakups = new Database.User[][]{

        };

        var existingIgnores = new Database.User[][]{

        };

        TestUtility.setupRelationshipEnvironment(users, existingFriends, existingFriendRequests, existingBreakups, existingIgnores);

        // Setup existing chats
        // Setup a global chat
    }


    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        TestServer.stop();
    }

    ///////////////////////////////////////////////////////
    /// Chat Basic Sunshine tests
    ///////////////////////////////////////////////////////
    @Test
    public static void canCreateChat() {
        TestUtility.runTestWithExistingUser(KARL, (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createNewChatRequest(context.user.id, "KARL's chat", context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.CREATE_CHAT_RESPONSE) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                var id = msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getInt(FieldNames.CHAT_ID);
                assertEquals(0, msg.getJSONArray(FieldNames.ERROR).length());
                context.running.set(false);
            }
        });
    }

    // TODO: Setup global chat, log people in there, and check that this works!
    @Test
    public static void canGetChat() {
        fail();
    }

    @Test
    public static void canGetChatRange() {
        fail();
    }

    @Test
    public static void canJoinChat() {
        final var chatID = new AtomicInteger();

        TestUtility.runTestWithExistingUser(KARL, (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createNewChatRequest(context.user.id, "KARL's chat that FRIDA can join", context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.CREATE_CHAT_RESPONSE) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                chatID.set(msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getInt(FieldNames.CHAT_ID));
                assertEquals(0, msg.getJSONArray(FieldNames.ERROR).length());
                context.running.set(false);
            }
        });

        TestUtility.runTestWithExistingUser(FRIDA, (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createChatJoinRequest(context.user.id, chatID.get(), context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.JOIN_CHAT_RESPONSE) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                assertEquals(0, msg.getJSONArray(FieldNames.ERROR).length());
                TestUtility.sendMessage(context.socket, TestUtility.createGetChatRequest(context.user.id, chatID.get(), context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.GET_CHAT_RESPONSE) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getJSONArray(FieldNames.PARTICIPANT_ID).length());
                assertEquals(chatID.get(), msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getInt(FieldNames.CHAT_ID));
                context.running.set(false);
            }
        });
    }

    @Test
    public static void canLeaveChat() {
        final var chatID = new AtomicInteger();

        TestUtility.runTestWithExistingUser(KARL, (context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createNewChatRequest(context.user.id, "KARL's chat that FRIDA can join", context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.CREATE_CHAT_RESPONSE) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                chatID.set(msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getInt(FieldNames.CHAT_ID));
                assertEquals(0, msg.getJSONArray(FieldNames.ERROR).length());
                context.running.set(false);
            }
        });
    }


    @Test
    public static void canSendChatMessage() {
        fail();
    }

    @Test
    public static void receivesChatEventOnSomeoneJoining() {
        fail();
    }

    @Test
    public static void userLeavesChatOnDisconnect() {
        fail();
    }

    ///////////////////////////////////////////////////////
    /// Chat Failing tests
    ///////////////////////////////////////////////////////
    @Test
    public static void chatOutOfRangeReturnsError() {
        fail();
    }

    @Test
    public static void sendMessageToChatYouAreNotInReturnsError() {
        fail();
    }

    // TODO: Figure out if you should be able to join all chats, or can you mark chats as private? If they are marked private, they STAY private
//    @Test
//    public static void joiningAChatYouAreNotInvitedToReturnsError() {
//        fail();
//    }

    @Test
    public static void sendingMessageOnBehalfOfOthersReturnsError() {
        fail();
    }

    @Test
    public static void sendingInviteOnBehalfOfOthersReturnsError() {
        fail();
    }

    ///////////////////////////////////////////////////////
    /// Chat Friends interaction
    ///////////////////////////////////////////////////////
    @Test
    public static void canSendChatInvite() {
        fail();
    }

    @Test
    public static void receivesChatInviteEvent() {
        fail();
    }

    @Test
    public static void cannotInviteIgnoredFriendsToChat() {
        fail();
    }

    @Test
    public static void cannotInviteRandoms() {
        fail();
    }

    ///////////////////////////////////////////////////////
    /// Chat and Logging
    ///////////////////////////////////////////////////////
    @Test
    public static void chatMessagesAreLogged() {
        fail();
    }

    @Test
    public static void chatMessagesAreRemovedOnDeleteUser() {
        fail();
    }

}
