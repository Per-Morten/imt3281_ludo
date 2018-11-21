package no.ntnu.imt3281.ludo.api;

import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.server.Database;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

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
    private static final Database.User RALF_IGNORED_BY_KARL = new Database.User(5, "Ralf", null, "Ralf@mail.com", null, null, "RalfPassword");

    private static final Database.User USER_TO_BE_DELETED = new Database.User(6, "UserToBeDeleted", null, "UserToBeDeleted@mail.com", null, null, "password");


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Logger.setLogLevel(Logger.Level.DEBUG);

        TestServer.start();

        var users = new Database.User[]{
                KARL, FRIDA, HENRIK, ARIN_LONER, RALF_IGNORED_BY_KARL, USER_TO_BE_DELETED
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
    }


    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        TestServer.stop();
    }

    ///////////////////////////////////////////////////////
    /// Chat Basic Sunshine tests
    ///////////////////////////////////////////////////////
    @Test
    public void canCreateChat() {
        TestUtility.testWithLoggedInUsers(List.of(KARL), List.of((context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createNewChatRequest(context.user.id, "KARL's chat", context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.CREATE_CHAT_RESPONSE) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                var id = msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getInt(FieldNames.CHAT_ID);
                assertEquals(0, msg.getJSONArray(FieldNames.ERROR).length());
                context.finishedThreads.incrementAndGet();
            }
        }));
    }

    @Test
    public void canGetChat() {
        TestUtility.testWithLoggedInUsers(List.of(FRIDA), List.of((context, msg) -> {
            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {
                TestUtility.sendMessage(context.socket, TestUtility.createGetChatRequest(context.user.id, GlobalChat.ID, context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.GET_CHAT_RESPONSE, msg)) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                var globalChat = msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0);
                assertEquals(GlobalChat.ID, globalChat.getInt(FieldNames.CHAT_ID));
                assertEquals(GlobalChat.NAME, globalChat.getString(FieldNames.NAME));
                assertEquals(0, globalChat.getJSONArray(FieldNames.PARTICIPANT_ID).length());
                context.finishedThreads.incrementAndGet();
            }
        }));
    }

    @Test
    public void canJoinChat() {
        TestUtility.testWithLoggedInUsers(List.of(FRIDA), List.of((context, msg) -> {
            var type = msg.getString(FieldNames.TYPE);
            if (ResponseType.fromString(type) == ResponseType.LOGIN_RESPONSE) {
                TestUtility.sendMessage(context.socket, TestUtility.createChatJoinRequest(context.user.id, GlobalChat.ID, context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.JOIN_CHAT_RESPONSE) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                assertEquals(0, msg.getJSONArray(FieldNames.ERROR).length());
                TestUtility.sendMessage(context.socket, TestUtility.createGetChatRequest(context.user.id, GlobalChat.ID, context.user.token));
            }

            if (ResponseType.fromString(type) == ResponseType.GET_CHAT_RESPONSE) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getJSONArray(FieldNames.PARTICIPANT_ID).length());
                assertEquals(GlobalChat.ID, msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getInt(FieldNames.CHAT_ID));
                context.finishedThreads.incrementAndGet();
            }
        }));
    }

    @Test
    public void canLeaveChat() {
        final var chatID = new AtomicInteger();

        var karlCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {
                TestUtility.sendMessage(context.socket, TestUtility.createNewChatRequest(context.user.id, "KARL's chat that FRIDA can join", context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.CREATE_CHAT_RESPONSE, msg)) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                chatID.set(msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getInt(FieldNames.CHAT_ID));
                assertEquals(0, msg.getJSONArray(FieldNames.ERROR).length());
                TestUtility.sendMessage(context.socket, TestUtility.createChatInviteRequest(context.user.id, FRIDA.id, chatID.get(), context.user.token));
            }

            // Frida joined
            if (TestUtility.isOfType(EventType.CHAT_UPDATE, msg)) {
                context.finishedThreads.incrementAndGet();
            }
        });

        var fridaCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(EventType.CHAT_INVITE, msg)) {
                TestUtility.sendMessage(context.socket, TestUtility.createChatJoinRequest(context.user.id, msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.CHAT_ID), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.JOIN_CHAT_RESPONSE, msg)) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                assertEquals(0, msg.getJSONArray(FieldNames.ERROR).length());
                TestUtility.sendMessage(context.socket, TestUtility.createLeaveChatRequest(context.user.id, chatID.get(), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.LEAVE_CHAT_RESPONSE, msg)) {
                TestUtility.sendMessage(context.socket, TestUtility.createGetChatRequest(context.user.id, chatID.get(), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.GET_CHAT_RESPONSE, msg)) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());

                // Karl is still present
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getJSONArray(FieldNames.PARTICIPANT_ID).length());
                context.finishedThreads.incrementAndGet();
            }
        });

        TestUtility.testWithLoggedInUsers(List.of(FRIDA, KARL), List.of(fridaCallback, karlCallback));
    }

    @Test
    public void cannotLeaveGlobalChat() {
        TestUtility.testWithLoggedInUsers(List.of(FRIDA), List.of((context, msg) -> {
            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {
                TestUtility.sendMessage(context.socket, TestUtility.createChatJoinRequest(context.user.id, GlobalChat.ID, context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.JOIN_CHAT_RESPONSE, msg)) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                assertEquals(0, msg.getJSONArray(FieldNames.ERROR).length());
                TestUtility.sendMessage(context.socket, TestUtility.createLeaveChatRequest(context.user.id, GlobalChat.ID, context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.LEAVE_CHAT_RESPONSE, msg)) {
                TestUtility.assertError(Error.CANNOT_LEAVE_GLOBAL_CHAT, msg);
                context.finishedThreads.incrementAndGet();
            }
        }));
    }

    @Test
    public void canSendChatMessage() {
        TestUtility.testWithLoggedInUsers(List.of(FRIDA), List.of((context, msg) -> {
            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {
                TestUtility.sendMessage(context.socket, TestUtility.createChatJoinRequest(context.user.id, GlobalChat.ID, context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.JOIN_CHAT_RESPONSE, msg)) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                assertEquals(0, msg.getJSONArray(FieldNames.ERROR).length());
                TestUtility.sendMessage(context.socket, TestUtility.createSendChatMessageRequest(context.user.id, GlobalChat.ID, context.user.token, "MOCK message"));
            }

            if (TestUtility.isOfType(ResponseType.SEND_CHAT_MESSAGE_RESPONSE, msg)) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                context.count.incrementAndGet();
            }

            if (TestUtility.isOfType(EventType.CHAT_MESSAGE, msg)) {
                assertEquals(1, msg.getJSONArray(FieldNames.PAYLOAD).length());
                var first = msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0);
                assertEquals("MOCK message", first.getString(FieldNames.MESSAGE));
                assertEquals(GlobalChat.ID, first.getInt(FieldNames.CHAT_ID));
                assertEquals(context.user.id, first.getInt(FieldNames.USER_ID));
                context.count.incrementAndGet();
            }

            if (context.count.get() >= 2) {
                context.finishedThreads.incrementAndGet();
            }
        }));
    }

    @Test
    public void receivesChatEventOnSomeoneJoining() {
        // Log in both FRIDA and KARL
        final var karlInGlobalChat = new AtomicBoolean();

        var karlCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {
                TestUtility.sendMessage(context.socket, TestUtility.createChatJoinRequest(context.user.id, GlobalChat.ID, context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.JOIN_CHAT_RESPONSE, msg)) {
                karlInGlobalChat.set(true);
            }

            if (TestUtility.isOfType(EventType.CHAT_UPDATE, msg)) {
                assertEquals(1, msg.getJSONArray(FieldNames.PAYLOAD).length());
                assertEquals(GlobalChat.ID, msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.CHAT_ID));
                context.count.incrementAndGet();
            }

            if (context.count.get() >= 2) {
                context.running.set(false);
                context.finishedThreads.incrementAndGet();
            }
        });

        var fridaCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {
                while (!karlInGlobalChat.get()) {

                }
                TestUtility.sendMessage(context.socket, TestUtility.createChatJoinRequest(context.user.id, GlobalChat.ID, context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.JOIN_CHAT_RESPONSE, msg)) {
                context.count.incrementAndGet();
            }

            if (TestUtility.isOfType(EventType.CHAT_UPDATE, msg)) {
                context.count.incrementAndGet();
            }

            if (context.count.get() >= 2) {
                context.finishedThreads.incrementAndGet();
            }
        });

        TestUtility.testWithLoggedInUsers(List.of(FRIDA, KARL), List.of(fridaCallback, karlCallback));
    }

    @Test
    public void userLeavesChatOnDisconnect() {
        final var numberInChat = new AtomicInteger();
        final var chatID = new AtomicInteger();

        var karlCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {
                // Create new chat
                TestUtility.sendMessage(context.socket, TestUtility.createNewChatRequest(context.user.id, "UserLeavesOnDisconnectChat", context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.CREATE_CHAT_RESPONSE, msg)) {
                TestUtility.sendMessage(context.socket, TestUtility.createChatInviteRequest(context.user.id, FRIDA.id, msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getInt(FieldNames.CHAT_ID), context.user.token));
            }

            // Frida has joined
            if (TestUtility.isOfType(EventType.CHAT_UPDATE, msg)) {
                // Just disconnect.
                context.running.set(false);
                context.finishedThreads.incrementAndGet();

            }
        });

        var fridaCallback = TestUtility.createJSONCallback((context, msg) -> {
            // Join newly created chat
            if (TestUtility.isOfType(EventType.CHAT_INVITE, msg)) {
                // Need to check that it is the correct chat.
                TestUtility.sendMessage(context.socket, TestUtility.createChatJoinRequest(context.user.id, msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.CHAT_ID), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.JOIN_CHAT_RESPONSE, msg)) {
                numberInChat.getAndIncrement();
                context.count.incrementAndGet();
            }

            if (TestUtility.isOfType(EventType.CHAT_UPDATE, msg)) {
                assertEquals(1, msg.getJSONArray(FieldNames.PAYLOAD).length());
                context.count.incrementAndGet();
            }

            // Should get exactly 3 messages: JOIN_CHAT_RESPONSE, CHAT_UPDATE (for me joining), CHAT_UPDATE(for karl leaving)
            if (context.count.get() >= 3) {
                context.finishedThreads.incrementAndGet();
            }
        });

        TestUtility.testWithLoggedInUsers(List.of(FRIDA, KARL), List.of(fridaCallback, karlCallback));
    }

    @Test
    public void chatsGetRemovedOnEmpty() {
        final var chatID = new AtomicInteger();

        var karlCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {
                // Create new chat
                TestUtility.sendMessage(context.socket, TestUtility.createNewChatRequest(context.user.id, "ChatIsRemovedOnEmptyChat", context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.CREATE_CHAT_RESPONSE, msg)) {
                chatID.set(msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getInt(FieldNames.CHAT_ID));
                TestUtility.sendMessage(context.socket, TestUtility.createLeaveChatRequest(context.user.id, chatID.get(), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.LEAVE_CHAT_RESPONSE, msg)) {
                Logger.log(Logger.Level.DEBUG, "Sending get chat request");
                TestUtility.sendMessage(context.socket, TestUtility.createGetChatRequest(context.user.id, chatID.get(), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.GET_CHAT_RESPONSE, msg)) {
                TestUtility.assertError(Error.CHAT_ID_NOT_FOUND, msg);
                context.finishedThreads.incrementAndGet();
            }
        });

        TestUtility.testWithLoggedInUsers(List.of(KARL), List.of(karlCallback));
    }

    ///////////////////////////////////////////////////////
    /// Chat Failing tests
    ///////////////////////////////////////////////////////
    @Test
    public void chatOutOfRangeReturnsError() {
        final var chatID = new AtomicInteger();

        var karlCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {
                TestUtility.sendMessage(context.socket, TestUtility.createGetChatRequest(context.user.id, chatID.get(), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.GET_CHAT_RESPONSE, msg)) {
                TestUtility.assertError(Error.CHAT_ID_NOT_FOUND, msg);
                context.finishedThreads.incrementAndGet();
            }
        });

        TestUtility.testWithLoggedInUsers(List.of(KARL), List.of(karlCallback));
    }

    @Test
    public void sendMessageToChatYouAreNotInReturnsError() {
        final var chatID = new AtomicInteger();
        final var fridaFinished = new AtomicBoolean(false);

        var karlCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {
                // Create new chat
                TestUtility.sendMessage(context.socket, TestUtility.createNewChatRequest(context.user.id, "ChatFridaIsNoTIn", context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.CREATE_CHAT_RESPONSE, msg)) {
                chatID.set(msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getInt(FieldNames.CHAT_ID));

                while (!fridaFinished.get()) {

                }

                context.finishedThreads.incrementAndGet();
            }
        });

        var fridaCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {
                // Wait until chat has been created.
                while (chatID.get() == 0) {

                }

                // Send message to chat you are not a part in.
                TestUtility.sendMessage(context.socket, TestUtility.createSendChatMessageRequest(context.user.id, chatID.get(), context.user.token, "Illegal message"));
            }

            if (TestUtility.isOfType(ResponseType.SEND_CHAT_MESSAGE_RESPONSE, msg)) {
                TestUtility.assertError(Error.UNAUTHORIZED, msg);
                fridaFinished.set(true);
                context.finishedThreads.incrementAndGet();
            }
        });

        TestUtility.testWithLoggedInUsers(List.of(FRIDA, KARL), List.of(fridaCallback, karlCallback));
    }

    // We are going for all chats being private
//    @Test
//    public void joiningAChatYouAreNotInvitedToReturnsError() {
//        fail();
//    }

    @Test
    public void sendingMessageOnBehalfOfOthersReturnsError() {
        var fridaCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {
                // Send message to chat you are not a part in.
                TestUtility.sendMessage(context.socket, TestUtility.createSendChatMessageRequest(KARL.id, GlobalChat.ID, context.user.token, "Illegal message"));
            }

            if (TestUtility.isOfType(ResponseType.SEND_CHAT_MESSAGE_RESPONSE, msg)) {
                TestUtility.assertError(Error.UNAUTHORIZED, msg);
                context.finishedThreads.incrementAndGet();
            }
        });

        TestUtility.testWithLoggedInUsers(List.of(FRIDA), List.of(fridaCallback));
    }

    @Test
    public void sendingInviteOnBehalfOfOthersReturnsError() {
        var fridaCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {
                // Send message to chat you are not a part in.
                TestUtility.sendMessage(context.socket, TestUtility.createChatInviteRequest(KARL.id, HENRIK.id, GlobalChat.ID, context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.SEND_CHAT_INVITE_RESPONSE, msg)) {
                TestUtility.assertError(Error.UNAUTHORIZED, msg);
                context.finishedThreads.incrementAndGet();
            }
        });

        TestUtility.testWithLoggedInUsers(List.of(FRIDA), List.of(fridaCallback));
    }

    ///////////////////////////////////////////////////////
    /// Chat Friends interaction
    ///////////////////////////////////////////////////////
    @Test
    public void canJoinChatInvitedTo() {
        final var chatID = new AtomicInteger();

        var karlCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {
                // Create new chat
                TestUtility.sendMessage(context.socket, TestUtility.createNewChatRequest(context.user.id, "ChatFridaIsNoTIn", context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.CREATE_CHAT_RESPONSE, msg)) {
                chatID.set(msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getInt(FieldNames.CHAT_ID));
                Logger.log(Logger.Level.DEBUG, "Created chat, inviting FRIDA: %d, to chat: %d", FRIDA.id, chatID.get());
                TestUtility.sendMessage(context.socket, TestUtility.createChatInviteRequest(context.user.id, FRIDA.id, chatID.get(), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.SEND_CHAT_INVITE_RESPONSE, msg)) {
                context.count.incrementAndGet();
            }

            // Should get update that Frida Joined the chat
            if (TestUtility.isOfType(EventType.CHAT_UPDATE, msg)) {
                assertEquals(chatID.get(), msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.CHAT_ID));
                context.count.incrementAndGet();
            }

            if (context.count.get() >= 2) {
                context.finishedThreads.incrementAndGet();
            }
        });

        var fridaCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(EventType.CHAT_INVITE, msg)) {
                assertEquals(chatID.get(), msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.CHAT_ID));
                TestUtility.sendMessage(context.socket, TestUtility.createChatJoinRequest(context.user.id, chatID.get(), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.JOIN_CHAT_RESPONSE, msg)) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                context.count.incrementAndGet();
            }

            if (TestUtility.isOfType(EventType.CHAT_UPDATE, msg)) {
                assertEquals(chatID.get(), msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.CHAT_ID));
                context.count.incrementAndGet();
            }

            if (context.count.get() >= 2) {
                context.finishedThreads.incrementAndGet();
            }
        });

        TestUtility.testWithLoggedInUsers(List.of(FRIDA, KARL), List.of(fridaCallback, karlCallback));
    }

    @Test
    public void cannotInviteIgnoredUsersToChat() {
        var callback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {

                TestUtility.sendMessage(context.socket, TestUtility.createChatInviteRequest(context.user.id, KARL.id, GlobalChat.ID, context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.SEND_CHAT_INVITE_RESPONSE, msg)) {
                TestUtility.assertError(Error.USER_IS_NOT_FRIEND, msg);
                context.finishedThreads.incrementAndGet();
            }
        });

        TestUtility.testWithLoggedInUsers(List.of(RALF_IGNORED_BY_KARL), List.of(callback));
    }

    @Test
    public void cannotInviteRandoms() {
        var callback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {

                TestUtility.sendMessage(context.socket, TestUtility.createChatInviteRequest(context.user.id, ARIN_LONER.id, GlobalChat.ID, context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.SEND_CHAT_INVITE_RESPONSE, msg)) {
                TestUtility.assertError(Error.USER_IS_NOT_FRIEND, msg);
                context.finishedThreads.incrementAndGet();
            }
        });

        TestUtility.testWithLoggedInUsers(List.of(FRIDA), List.of(callback));
    }

    @Test
    public void inviteToEmptyChatYieldsErrorOnJoin() {
        final var chatID = new AtomicInteger();
        final var karlLeft = new AtomicBoolean(false);

        var karlCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {
                TestUtility.sendMessage(context.socket, TestUtility.createNewChatRequest(context.user.id, "ChatFridaIsNoTIn", context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.CREATE_CHAT_RESPONSE, msg)) {
                chatID.set(msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getInt(FieldNames.CHAT_ID));
                TestUtility.sendMessage(context.socket, TestUtility.createChatInviteRequest(context.user.id, FRIDA.id, chatID.get(), context.user.token));
                TestUtility.sendMessage(context.socket, TestUtility.createLeaveChatRequest(context.user.id, chatID.get(), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.LEAVE_CHAT_RESPONSE, msg)) {
                karlLeft.set(true);
                context.finishedThreads.incrementAndGet();
            }
        });

        var fridaCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(EventType.CHAT_INVITE, msg)) {
                assertEquals(chatID.get(), msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.CHAT_ID));
                while (!karlLeft.get()) {

                }

                TestUtility.sendMessage(context.socket, TestUtility.createChatJoinRequest(context.user.id, chatID.get(), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.JOIN_CHAT_RESPONSE, msg)) {
                TestUtility.assertError(Error.CHAT_ID_NOT_FOUND, msg);
                context.finishedThreads.incrementAndGet();
            }
        });

        TestUtility.testWithLoggedInUsers(List.of(FRIDA, KARL), List.of(fridaCallback, karlCallback));
    }

    @Test
    public void cannotJoinInvitedChatAfterLogout() {
        final var fridaIsFinished = new AtomicBoolean(false);

        var karlCallback = TestUtility.createJSONCallback((context, msg) -> {
            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {
                // Create new chat
                TestUtility.sendMessage(context.socket, TestUtility.createNewChatRequest(context.user.id, "ChatToInviteFridaTo", context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.CREATE_CHAT_RESPONSE, msg)) {
                TestUtility.sendMessage(context.socket, TestUtility.createChatInviteRequest(context.user.id, FRIDA.id, TestUtility.getChatID(msg), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.SEND_CHAT_INVITE_RESPONSE, msg)) {
                context.count.incrementAndGet();
                while (!fridaIsFinished.get()) {

                }
                Logger.log(Logger.Level.DEBUG, "******** ***** KARL IS FINISHED");
                context.finishedThreads.incrementAndGet();
            }
        });

        var chatID = new AtomicInteger();
        var fridaCallback1 = TestUtility.createJSONCallback((context, msg) -> {
            Logger.log(Logger.Level.DEBUG, "FRIDA RECEIVED: %s", msg);

            if (TestUtility.isOfType(EventType.CHAT_INVITE, msg)) {
                chatID.set(msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.CHAT_ID));

                //chatID.set(TestUtility.getChatID(msg));
                var payload = new JSONArray();
                var request = new JSONObject();
                request.put(FieldNames.ID, 0);
                request.put(FieldNames.USER_ID, context.user.id);
                payload.put(0, request);
                TestUtility.sendMessage(context.socket, TestUtility.createRequest(RequestType.LOGOUT_REQUEST, payload, context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.LOGOUT_RESPONSE, msg)) {
                TestUtility.sendMessage(context.socket, TestUtility.createLoginRequest(context.user.email, context.user.password));
            }

            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg) && context.count.getAndIncrement() >= 1) {
                context.user.token = msg.getJSONArray(FieldNames.SUCCESS).getJSONObject(0).getString(FieldNames.AUTH_TOKEN);
                TestUtility.sendMessage(context.socket, TestUtility.createChatJoinRequest(context.user.id, chatID.get(), context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.JOIN_CHAT_RESPONSE, msg)) {
                Logger.log(Logger.Level.DEBUG, "FRIDA: Join Chat Response Received: %s", msg);
                TestUtility.assertError(Error.UNAUTHORIZED, msg);
                fridaIsFinished.set(true);
                context.finishedThreads.getAndIncrement();
            }

            if (TestUtility.isOfType(EventType.CHAT_UPDATE, msg)) {
                assertEquals(chatID.get(), msg.getJSONArray(FieldNames.PAYLOAD).getJSONObject(0).getInt(FieldNames.CHAT_ID));
                context.count.incrementAndGet();
            }

//            if (context.count.get() >= 2) {
//                context.finishedThreads.incrementAndGet();
//            }
        });


        TestUtility.testWithLoggedInUsers(List.of(FRIDA, KARL), List.of(fridaCallback1, karlCallback));
    }

    ///////////////////////////////////////////////////////
    /// Chat and Logging
    ///////////////////////////////////////////////////////
    @Test
    public void chatMessagesAreNotRemovedOnDeleteUser() throws SQLException {
        final var message = "Message to remain";

        // Log someone in an write a message.
        TestUtility.testWithLoggedInUsers(List.of(USER_TO_BE_DELETED), List.of((context, msg) -> {
            if (TestUtility.isOfType(ResponseType.LOGIN_RESPONSE, msg)) {
                TestUtility.sendMessage(context.socket, TestUtility.createChatJoinRequest(context.user.id, GlobalChat.ID, context.user.token));
            }
            if (TestUtility.isOfType(ResponseType.JOIN_CHAT_RESPONSE, msg)) {
                TestUtility.sendMessage(context.socket, TestUtility.createSendChatMessageRequest(context.user.id, GlobalChat.ID, context.user.token, message));

            }
            if (TestUtility.isOfType(ResponseType.SEND_CHAT_MESSAGE_RESPONSE, msg)) {
                var payload = new JSONArray();
                var request = new JSONObject();
                request.put(FieldNames.ID, 0);
                request.put(FieldNames.USER_ID, context.user.id);
                payload.put(0, request);
                TestUtility.sendMessage(context.socket, TestUtility.createRequest(RequestType.DELETE_USER_REQUEST, payload, context.user.token));
            }

            if (TestUtility.isOfType(ResponseType.DELETE_USER_RESPONSE, msg)) {
                assertEquals(1, msg.getJSONArray(FieldNames.SUCCESS).length());
                context.finishedThreads.incrementAndGet();
            }
        }));

        String url = "jdbc:sqlite:" + TestServer.DATABASE_NAME;
        try (var connection = DriverManager.getConnection(url)) {
            try (var query = connection.createStatement()) {
                var res = query.executeQuery(String.format("SELECT %s, %s, %s FROM %s WHERE %s = %d AND %s = %d",
                        Database.ChatMessageFields.ChatID,
                        Database.ChatMessageFields.UserID,
                        Database.ChatMessageFields.Message,
                        Database.ChatMessageFields.DBName,
                        Database.ChatMessageFields.UserID,
                        USER_TO_BE_DELETED.id,
                        Database.ChatMessageFields.ChatID,
                        GlobalChat.ID));

                res.next();
                assertEquals(GlobalChat.ID, res.getInt(Database.ChatMessageFields.ChatID));
                assertEquals(USER_TO_BE_DELETED.id, res.getInt(Database.ChatMessageFields.UserID));
                assertEquals(message, res.getString(Database.ChatMessageFields.Message));
            }
        }
    }

}
