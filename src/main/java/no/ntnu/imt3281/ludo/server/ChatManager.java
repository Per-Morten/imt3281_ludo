package no.ntnu.imt3281.ludo.server;

import no.ntnu.imt3281.ludo.api.Error;
import no.ntnu.imt3281.ludo.api.*;
import no.ntnu.imt3281.ludo.common.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

// Chats, cannot join chats you are not invited to. (All chats are private)
// All chats are private (i.e. Invite only)
// Invites are cleared upon logout.

// Make chats open!

public class ChatManager {
    private class Chat {
        public Queue<Integer> participants;
        public Queue<Integer> pending;
        public String name;
        public int id;

        boolean isGameChat = false;

        public Chat(String name, int id) {
            this.name = name;
            participants = new ArrayDeque<>();
            pending = new ArrayDeque<>();
            this.id = id;
        }
    }

    private static final int PAGE_SIZE = 100;

    private Database mDB;
    private UserManager mUserManager;

    private ReentrantLock mLock = new ReentrantLock();

    // Mapping of ChatID and Chats
    HashMap<Integer, Chat> mChats = new HashMap<>();

    public ChatManager(Database db, UserManager userManager) {
        mDB = db;
        mUserManager = userManager;

        mChats.put(GlobalChat.ID, new Chat(GlobalChat.NAME, GlobalChat.ID));
    }

    public void createChat(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        try (var lock = new LockGuard(mLock)) {
            MessageUtility.each(requests, (requestID, request) -> {
                try {
                    var name = request.getString(FieldNames.NAME);
                    var chatID = mDB.createChat(name);

                    mChats.put(chatID, new Chat(name, chatID));
                    var item = mChats.get(chatID);
                    item.participants.add(request.getInt(FieldNames.USER_ID));

                    var success = new JSONObject();
                    success.put(FieldNames.CHAT_ID, chatID);
                    MessageUtility.appendSuccess(successes, requestID, success);

                } catch (Exception e) {
                    Logger.logException(Logger.Level.WARN, e, "Encountered exception when creating chat:");
                }
            });
        }
    }

    public void getChat(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        try (var lock = new LockGuard(mLock)) {
            MessageUtility.each(requests, (requestID, request) -> {
                var chatID = request.getInt(FieldNames.CHAT_ID);

                var chat = mChats.get(chatID);

                var success = new JSONObject();
                success.put(FieldNames.CHAT_ID, chatID);
                success.put(FieldNames.NAME, chat.name);
                var participants = new JSONArray();
                chat.participants.forEach(participants::put);
                success.put(FieldNames.PARTICIPANT_ID, participants);

                MessageUtility.appendSuccess(successes, requestID, success);
            });
        }
    }

    public void getChatRange(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        try (var lock = new LockGuard(mLock)) {
            MessageUtility.each(requests, (requestID, request) -> {
                var page = request.getInt(FieldNames.PAGE_INDEX);
                if (page < 0) {
                    Logger.log(Logger.Level.WARN, "Someone tried to get a negative range of chats");
                    return;
                }

                var range = new JSONArray();
                var chats = mChats.values()
                        .stream()
                        .skip(page * PAGE_SIZE)
                        .filter(chat -> !chat.isGameChat)
                        .limit(PAGE_SIZE).collect(Collectors.toList());

                for (var chat : chats) {
                    var chatJSON = new JSONObject();
                    chatJSON.put(FieldNames.CHAT_ID, chat.id);
                    chatJSON.put(FieldNames.NAME, chat.name);
                    var participants = new JSONArray();
                    chat.participants.forEach(participants::put);
                    chatJSON.put(FieldNames.PARTICIPANT_ID, participants);
                    range.put(chatJSON);
                }


                var success = new JSONObject();
                success.put(FieldNames.ID, requestID);
                success.put(FieldNames.RANGE, range);
                MessageUtility.appendSuccess(successes, requestID, success);
            });
        }
    }

    public void leaveChat(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        try (var lock = new LockGuard(mLock)) {
            MessageUtility.each(requests, (requestID, request) -> {
                var chatID = request.getInt(FieldNames.CHAT_ID);

                var chat = mChats.get(chatID);

                if (chat.id == GlobalChat.ID) {
                    MessageUtility.appendError(errors, requestID, Error.CANNOT_LEAVE_GLOBAL_CHAT);
                }

                chat.participants.remove(request.getInt(FieldNames.USER_ID));
                chat.pending.remove(request.getInt(FieldNames.USER_ID));

                MessageUtility.appendSuccess(successes, requestID, new JSONObject());

                if (chat.participants.isEmpty() && chat.id != GlobalChat.ID) {
                    mChats.remove(chat.id);
                    return;
                }

                events.add(createChatUpdateMessage(chatID, chat.participants));
            });
        }
    }

    public void inviteToChat(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        try (var lock = new LockGuard(mLock)) {
            MessageUtility.each(requests, (requestID, request) -> {
                var chatID = request.getInt(FieldNames.CHAT_ID);

                var chat = mChats.get(chatID);

                var userID = request.getInt(FieldNames.USER_ID);
                var otherID = request.getInt(FieldNames.OTHER_ID);

                if (!mUserManager.areUsersFriends(userID, otherID)) {
                    MessageUtility.appendError(errors, requestID, Error.USER_IS_NOT_FRIEND);
                    return;
                }

                if (chat.pending.contains(otherID) || chat.participants.contains(otherID)) {
                    // We silently "fail" here.
                    return;
                }

                chat.pending.add(otherID);
                MessageUtility.appendSuccess(successes, requestID, new JSONObject());

                var event = MessageUtility.createEvent(EventType.CHAT_INVITE);
                var payload = event.getJSONArray(FieldNames.PAYLOAD);
                var object = new JSONObject();
                object.put(FieldNames.USER_ID, userID);
                object.put(FieldNames.CHAT_ID, chatID);
                payload.put(object);

                events.add(new Message(event, List.of(otherID)));
            });
        }
    }

    public void joinChat(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        try (var lock = new LockGuard(mLock)) {
            MessageUtility.each(requests, (requestID, request) -> {
                var chatID = request.getInt(FieldNames.CHAT_ID);

                var chat = mChats.get(chatID);

                var userID = request.getInt(FieldNames.USER_ID);
                if (!canJoinChat(userID, chat)) {
                    MessageUtility.appendError(errors, requestID, Error.UNAUTHORIZED);
                }

                if (chat.participants.contains(userID)) {
                    // Silently "fail".
                    Logger.log(Logger.Level.WARN, "User(%d) is already in chat(%d)", userID, chatID);
                    return;
                }

                chat.pending.remove(userID);
                chat.participants.add(userID);

                MessageUtility.appendSuccess(successes, requestID, new JSONObject());

                var event = MessageUtility.createEvent(EventType.CHAT_UPDATE);
                var payload = event.getJSONArray(FieldNames.PAYLOAD);
                var object = new JSONObject();
                object.put(FieldNames.CHAT_ID, chatID);
                payload.put(object);

                events.add(new Message(event, DeepCopy.copy(chat.participants)));
            });
        }
    }

    public void sendChatMessage(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        try (var lock = new LockGuard(mLock)) {
            MessageUtility.each(requests, (requestID, request) -> {

                var chat = mChats.get(request.getInt(FieldNames.CHAT_ID));

                var userID = request.getInt(FieldNames.USER_ID);
                if (!chat.participants.contains(userID)) {
                    MessageUtility.appendError(errors, requestID, Error.UNAUTHORIZED);
                    return;
                }

                var message = request.getString(FieldNames.MESSAGE);

                try {
                    mDB.logChatMessage(userID, chat.id, message);
                } catch (Exception e) {
                    Logger.logException(Logger.Level.WARN, e, "Encountered exception when creating chat:");
                }

                MessageUtility.appendSuccess(successes, requestID, new JSONObject());

                var event = MessageUtility.createEvent(EventType.CHAT_MESSAGE);
                var object = new JSONObject();
                object.put(FieldNames.USER_ID, userID);
                object.put(FieldNames.CHAT_ID, chat.id);
                object.put(FieldNames.MESSAGE, message);
                event.getJSONArray(FieldNames.PAYLOAD).put(object);
                events.add(new Message(event, DeepCopy.copy(chat.participants)));

            });
        }
    }

    public void onLogoutUser(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        try (var lock = new LockGuard(mLock)) {
            MessageUtility.each(requests, (requestID, request) -> {
                removeFromChats(request.getInt(FieldNames.USER_ID), events);
            });
        }
    }

    /**
     * Applied the first order filter in the requests within requests, removes the erroneous ones and
     * appends the errors to the errors array.
     * This filter is just a basic filter to get rid of the following common errors:
     * * Trying to access a chat that does not exist.
     *
     * @param type     The type of request that are stored in requests.
     * @param requests The actual requests themselves
     * @param errors   The JSONArray to put the errors in.
     */
    public void applyFirstOrderFilter(RequestType type, JSONArray requests, JSONArray errors) {
        MessageUtility.applyFilter(requests, (id, request) -> {
            if (JSONValidator.hasInt(FieldNames.CHAT_ID, request) && mChats.get(request.getInt(FieldNames.CHAT_ID)) == null) {
                MessageUtility.appendError(errors, id, Error.CHAT_ID_NOT_FOUND);
                return false;
            }

            return true;
        });
    }

    public int createChatForGame(String gameName) {
        try (var lock = new LockGuard(mLock)) {
            var chatID = mDB.createChat(gameName);

            mChats.put(chatID, new Chat(gameName, chatID));
            var item = mChats.get(chatID);

            return chatID;
        } catch (SQLException e) {
            Logger.logException(Logger.Level.WARN, e, String.format("Exception encountered when creating chat for game: %s", gameName));
        }
        return 0;
    }


    public void removeFromChats(int id, Queue<Message> events) {
        Logger.log(Logger.Level.DEBUG, "Removing %d from chats", id);
        try (var lock = new LockGuard(mLock)) {
            mChats.values().removeIf(chat -> {
                if (chat.participants.remove(id)) {
                    events.add(createChatUpdateMessage(chat.id, chat.participants));
                }
                chat.pending.remove(id);
                return chat.participants.size() == 0 && chat.id != GlobalChat.ID;
            });
        }
    }

    private boolean canJoinChat(int userID, Chat chat) {
        if (chat.id == GlobalChat.ID) {
            return true;
        }

        if (chat.pending.contains(userID)) {
            return true;
        }

        return false;
    }


    private Message createChatUpdateMessage(int chatID, Queue<Integer> receivers) {
        var event = MessageUtility.createEvent(EventType.CHAT_UPDATE);
        var payload = event.getJSONArray(FieldNames.PAYLOAD);
        var object = new JSONObject();
        object.put(FieldNames.CHAT_ID, chatID);
        payload.put(object);

        return new Message(event, DeepCopy.copy(receivers));
    }
}
