package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.api.FieldNames;
import no.ntnu.imt3281.ludo.api.FriendStatus;
import no.ntnu.imt3281.ludo.api.RequestType;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;
import no.ntnu.imt3281.ludo.gui.Transitions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static no.ntnu.imt3281.ludo.api.FieldNames.CHAT_ID;
import static no.ntnu.imt3281.ludo.api.RequestType.*;

public class Actions implements API.Events {

    private Transitions mTransitions;
    private API mAPI;
    private StateManager mState;
    private final RequestFactory mRequests = new RequestFactory();

    /**
     * Bind dependencies of Actions
     *
     * @param transitions to mutate the FXML state
     * @param API to push requests to server, and get responses + events from server
     */
    void bind(Transitions transitions, API API, StateManager stateManager) {
        mTransitions = transitions;
        mState = stateManager;
        mAPI = API;
    }

    /**
     * Goto the login screen
     */
    void gotoLogin() {
        this.logAction("gotoLogin");

        mState.commit(gState -> {
            gState.userId = -1;
            gState.email = "";
            gState.authToken = "";
            gState.username = "";
            gState.avatarURI = "";

            gState.searchUsers = "";
            gState.searchChats = "";
            gState.searchFriends = "";
            gState.searchGames = "";

            gState.activeChats.clear();
            gState.activeGames.clear();
            gState.chatInvites.clear();
            gState.gameInvites.clear();
        });

        mTransitions.renderLogin();
    }

    /**
     * login user with username and password
     *
     * @param email    user provided email
     * @param username user provided username
     * @param password user provided password
     */
    public void createUser(String email, String password, String username) {
        this.logAction("createUser");

        mTransitions.renderLogin();

        var payload = new JSONObject();
        payload.put(FieldNames.EMAIL, email);
        payload.put(FieldNames.PASSWORD, password);
        payload.put(FieldNames.USERNAME, username);

        send(CREATE_USER_REQUEST, payload, success -> {

            mState.commit(state -> {
                state.userId = success.getInt(FieldNames.USER_ID);
                state.email = email;
                state.username = username;
            });

            mTransitions.renderLogin();
        });
    }

    /**
     * Login user with username and password.
     *
     * @param email    valid email
     * @param password valid password
     */
    public void login(String email, String password) {
        this.logAction("login");

        var payload = new JSONObject();
        payload.put(FieldNames.EMAIL, email);
        payload.put(FieldNames.PASSWORD, password);

        send(LOGIN_REQUEST, payload, success -> {

            mState.commit(state -> {
                state.userId = success.getInt(FieldNames.USER_ID);
                state.authToken = success.getString(FieldNames.AUTH_TOKEN);
            });
            this.gotoUser();
        
        });
    }


    /**
     * Logout user. Return to login screen
     */
    public void logout() {
        this.logAction("logout");

        var payload = new JSONObject();
        payload.put(FieldNames.USER_ID, mState.getUserId());

        send(LOGOUT_REQUEST, payload, success -> this.gotoLogin());

    }

    /**
     * Update currently logged in user
     */
    public void updateUser(String username, String email, String password, String avatarURI) {
        this.logAction("updateUser");

        var payload = new JSONObject();
        payload.put(FieldNames.USER_ID, mState.getUserId());
        payload.put(FieldNames.USERNAME, username);
        payload.put(FieldNames.EMAIL, email);
        payload.put(FieldNames.PASSWORD, password);
        payload.put(FieldNames.AVATAR_URI, avatarURI);

        send(UPDATE_USER_REQUEST, payload, success -> {

            mState.commit(state -> {
                state.username = username;
                state.email = email;
                state.avatarURI = avatarURI;
            });

            this.gotoUser();

        });
    }

    /**
     * Delete currently logged in user
     */
    public void deleteUser() {
        this.logAction("deleteUser");

        var payload = new JSONObject();
        payload.put(FieldNames.USER_ID, mState.getUserId());

        send(DELETE_USER_REQUEST, payload, success -> {
            this.logout();
        
        });
    }

    /**
     * Goto the user screen. Get details about logged in user
     */
    public void gotoUser() {
        this.logAction("gotoUser");

        var payload = new JSONObject();
        payload.put(FieldNames.USER_ID, mState.getUserId());

        send(GET_USER_REQUEST, payload, success -> {

            var user = new User();
            user.json = success;

            mState.commit(state -> {
                state.username = success.getString(FieldNames.USERNAME);
                state.avatarURI = success.getString(FieldNames.AVATAR_URI);
                // TODO Email does not exist in get_user_request.
            });
            mTransitions.renderUser(user);
        
        });
    }

    /**
     * Goto the live screen
     */
    public void gotoLive() {
        this.logAction("gotoLive");

        var state = mState.copy();
        mTransitions.renderLive();
        mTransitions.renderGameTabs(state.activeGames);
        mTransitions.renderChatTabs(state.activeChats);
    }

    /**
     * Goto the overview screen.
     * Display list of games,chats,friends and users.
     * Filter the aforementioned lists by the value of each correponding search field.
     */
    public void gotoOverview() {
        this.logAction("gotoOverview");

        var stateCopy = mState.copy();

        // Filter based on search
        var filteredGames = stateCopy.activeGames.values().stream()
                .filter(game -> game.getString(FieldNames.NAME).toUpperCase()
                        .contains(stateCopy.searchGames.toUpperCase()))
                .collect(Collectors.toList());

        var filteredGameInvites = stateCopy.gameInvites.values().stream()
                .filter(gameInv -> gameInv.getString(FieldNames.NAME).toUpperCase()
                            .contains(stateCopy.searchGames.toUpperCase()))
                .collect(Collectors.toList());

        var filteredChats = stateCopy.activeChats.values().stream()
                .filter(chat -> chat.json.getString(FieldNames.NAME).toUpperCase()
                            .contains(stateCopy.searchChats.toUpperCase()))
                .collect(Collectors.toList());

        var filteredChatInvites = stateCopy.chatInvites.values().stream()
                .filter(chatInv -> chatInv.getString(FieldNames.NAME).toUpperCase()
                        .contains(stateCopy.searchChats.toUpperCase()))
                .collect(Collectors.toList());


        mTransitions.renderOverview();
        mTransitions.renderGamesList(filteredGames, filteredGameInvites);
        mTransitions.renderChatsList(filteredChats, filteredChatInvites);

        // Get friends range
        final var payload = new JSONObject();
        payload.put(FieldNames.PAGE_INDEX, 0);
        payload.put(FieldNames.USER_ID, stateCopy.userId);

        send(GET_FRIEND_RANGE_REQUEST, payload, successFriends -> {

            var friendsRange = successFriends.getJSONArray(FieldNames.RANGE);
            var friendsList = new ArrayList<JSONObject>();

            friendsRange.forEach(friend -> {
                friendsList.add((JSONObject)friend);
            });

            // Filter friends by search
            var filteredFriendsList = friendsList.stream()
                    .filter(friend -> friend.getInt(FieldNames.STATUS) != FriendStatus.IGNORED.toInt())
                    .filter(friend -> friend.getString(FieldNames.USERNAME).toUpperCase()
                            .contains(stateCopy.searchFriends.toUpperCase()))
                    .collect(Collectors.toList());


            mTransitions.renderFriendList(filteredFriendsList);


            // Get user range
            send(GET_USER_RANGE_REQUEST, payload, successUsers -> {

                var usersRange = successUsers.getJSONArray(FieldNames.RANGE);
                var usersList = new ArrayList<JSONObject>();
                usersRange.forEach(user -> {

                    var userjson = (JSONObject)user;
                    int userId = userjson.getInt(FieldNames.USER_ID);

                    // Only add users who are not in the friends list
                    boolean found = false;
                    for (var friend: friendsList) {
                        var friendId = friend.getInt(FieldNames.USER_ID);
                        if (friendId == userId || userId == stateCopy.userId) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        usersList.add((JSONObject)user);
                    }
                });

                // Filter user by search
                var filteredUserList = usersList.stream()
                        .filter(user -> user.getString(FieldNames.USERNAME).toUpperCase()
                                .contains(stateCopy.searchUsers.toUpperCase()))
                        .collect(Collectors.toList());

                // Filter ignored by search
                var filteredIgnoredList = friendsList.stream()
                        .filter(friend -> friend.getInt(FieldNames.STATUS) == FriendStatus.IGNORED.toInt())
                        .filter(ignored -> ignored.getString(FieldNames.USERNAME).toUpperCase()
                                .contains(stateCopy.searchUsers.toUpperCase()))
                        .collect(Collectors.toList());

                mTransitions.renderUserList(filteredUserList, filteredIgnoredList);
            
            });
        });
    }

    /**
     * Create game and add as active
     */
    public void createGame(String gameName) {
        this.logAction("createGame");

        var game = makeGame();
        var clientGameId = game.getInt(FieldNames.GAME_ID);

        var payload = new JSONObject();
        payload.put(FieldNames.USER_ID, mState.getUserId());
        payload.put(FieldNames.NAME, gameName);

        send(CREATE_GAME_REQUEST, payload, success -> {
            // TODO SERVER UNIMPLEMENTED
        });

        game.put(FieldNames.NAME, gameName);
        mState.commit(state -> {
            state.activeGames.put(clientGameId, game);
        });
        mTransitions.newGame(clientGameId, game);
    }

    // TODO REMOVE WHEN SERVER IMPLEMENTS CREATE GAME
    private static JSONObject makeGame() {
        var gameId = randomInt();
        var game = new JSONObject();
        game.put(FieldNames.GAME_ID, gameId);
        game.put(FieldNames.NAME, "Game " + gameId);
        game.put(FieldNames.PLAYER_ID, new JSONArray(new int[]{}));
        return game;
    }


    /**
     * Create chat and add as active
     */
    public void createChat(String chatName) {
        this.logAction("createChat");

        var chatJSON = makeChat();
        var clientChatId = chatJSON.getInt(CHAT_ID);

        var payload = new JSONObject();
        payload.put(FieldNames.USER_ID, mState.getUserId());
        payload.put(FieldNames.NAME, chatName);

        send(CREATE_CHAT_REQUEST, payload, success -> {
            // TODO SERVER UNIMPLEMENTED
        });

        var chat = new Chat();
        chat.json = chatJSON;
        chat.json.put(FieldNames.NAME, chatName);
        mState.commit(state -> {
            state.activeChats.put(clientChatId , chat);
        });
        mTransitions.newChat(clientChatId, chat.json);
    }

    // TODO REMOVE WHEN SERVER IMPLEMENTS CREATE CHAT
    private static JSONObject makeChat() {
        var chatId = randomInt();
        var chat = new JSONObject();
        chat.put(CHAT_ID, chatId);
        chat.put(FieldNames.NAME, "Chat " + chatId);
        chat.put(FieldNames.PARTICIPANT_ID, new JSONArray(new int[]{}));
        return chat;
    }


    /**
     * Send friend request to a set of users
     *
     * @param usersId the set of users ids which will receive the friend request
     */
    public void friend(HashSet<Integer> usersId) {
        this.logAction("friend");

        var payload = new ArrayList<JSONObject>();
        usersId.forEach(friendId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, mState.getUserId());
            item.put(FieldNames.OTHER_ID, friendId);
            payload.add(item);
        });

        send(FRIEND_REQUEST, payload, success -> {
            this.gotoOverview();
    
        });
    }

    /**
     * Remove friends, pending friends and ignored users
     *
     * @param usersId the set of users ids which will be unfriended
     */
    public void unfriend(HashSet<Integer> usersId) {
        this.logAction("unfriend");

        var payload = new ArrayList<JSONObject>();
        usersId.forEach(friendId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, mState.getUserId());
            item.put(FieldNames.OTHER_ID, friendId);
            payload.add(item);
        });

        send(UNFRIEND_REQUEST, payload, success -> {
            this.gotoOverview();

        });
    }

    /**
     * Ignore a set of users
     *
     * @param usersId the users which will be ignored
     */
    public void ignore(HashSet<Integer> usersId) {
        this.logAction("ignore");

        var payload = new ArrayList<JSONObject>();
        usersId.forEach(userId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, mState.getUserId());
            item.put(FieldNames.OTHER_ID, userId);
            payload.add(item);
        });

        send(IGNORE_REQUEST, payload, success -> {
            this.gotoOverview();
        
        });
    }

    /**
     *
     */
    public void joinChat(HashSet<Integer> chatsId) {
        this.logAction("joinChat");

        var payload = new ArrayList<JSONObject>();
        chatsId.forEach(chatId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, mState.getUserId());
            item.put(CHAT_ID, chatId);
            payload.add(item);
        });

        send(JOIN_CHAT_REQUEST, payload, success -> {
            this.gotoOverview();

        });
    }

    /**
     *
     */
    public void leaveChat(HashSet<Integer> chatsId) {
        this.logAction("leaveChat");

        var payload = new ArrayList<JSONObject>();
        chatsId.forEach(chatId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, mState.getUserId());
            item.put(CHAT_ID, chatId);
            payload.add(item);
        });

        send(LEAVE_CHAT_REQUEST, payload, success -> {
            // TODO SERVER UNIMPLEMENTED
        });

        mState.commit(state -> {
            chatsId.forEach(id ->  {
                state.activeChats.remove(id);
            });
        });

        this.gotoOverview();
    }

    /**
     *
     */
    public void sendChatMessage(int chatId, String message) {
        this.logAction("sendChatMessage");

        var payload = new JSONObject();
        payload.put(FieldNames.USER_ID, mState.getUserId());
        payload.put(CHAT_ID, chatId);
        payload.put(FieldNames.MESSAGE, message);

        send(SEND_CHAT_MESSAGE_REQUEST, payload, success -> {
            // TODO SERVER UNIMPLEMENTED

        });

        var stateCopy = mState.copy();
        var messageJSON = new JSONObject();
        messageJSON.put(FieldNames.MESSAGE, message);
        messageJSON.put(FieldNames.USERNAME, stateCopy.username);

        mState.commit(state -> {
            Logger.log(Level.DEBUG, "!!!chatId: " + String.valueOf(chatId));
            state.activeChats.get(chatId).messages.add(messageJSON);
        });
        mTransitions.newMessage(chatId, stateCopy.username, message);
    }

    /**
     *
     */
    public void sendChatInvite(HashSet<Integer> chatsId, HashSet<Integer> friendsId) {
        this.logAction("sendChatInvite");

        var payload = new ArrayList<JSONObject>();
        chatsId.forEach(chatId -> {
            friendsId.forEach(friendId -> {
                var item = new JSONObject();
                item.put(FieldNames.USER_ID, mState.getUserId());
                item.put(FieldNames.OTHER_ID, friendId);
                item.put(CHAT_ID, chatId);
                payload.add(item);
            });
        });

        send(SEND_CHAT_INVITE_REQUEST, payload, success -> {
            this.gotoOverview();
        
        });
    }

    /**
     *
     */
    public void joinGame(HashSet<Integer> gamesId) {
        this.logAction("joinGame");

        var payload = new ArrayList<JSONObject>();
        gamesId.forEach(gameId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, mState.getUserId());
            item.put(FieldNames.GAME_ID, gameId);
            payload.add(item);
        });

        send(JOIN_GAME_REQUEST, payload, success -> {
            this.gotoOverview();
        
        });
    }

    /**
     *
     */
    public void leaveGame(HashSet<Integer> gamesId) {
        this.logAction("leaveGame");

        var payload = new ArrayList<JSONObject>();
        gamesId.forEach(gameId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, mState.getUserId());
            item.put(FieldNames.GAME_ID, gameId);
            payload.add(item);
        });

        send(LEAVE_GAME_REQUEST, payload, success -> {
            // TODO SERVER UNIMPLEMENTED
        
        });

        mState.commit(state -> {
            gamesId.forEach(id ->  {
                state.activeGames.remove(id);
            });
        });
        this.gotoOverview();
    }

    /**
     *
     */
    public void sendGameInvite(HashSet<Integer> gamesId, HashSet<Integer> friendsId) {
        this.logAction("sendGameInvite");

        var payload = new ArrayList<JSONObject>();
        gamesId.forEach(gameId -> {
            friendsId.forEach(friendId -> {
                var item = new JSONObject();
                item.put(FieldNames.USER_ID, mState.getUserId());
                item.put(FieldNames.OTHER_ID, friendId);
                item.put(FieldNames.GAME_ID, gameId);
                payload.add(item);
            });
        });

        send(SEND_GAME_INVITE_REQUEST, payload, success -> {
            this.gotoOverview();
    
        });
    }

    /**
     *
     */
    public void declineGameInvite(HashSet<Integer> gamesId) {
        this.logAction("declineGameInvite");

        var payload = new ArrayList<JSONObject>();
        gamesId.forEach(gameId -> {
            var item = new JSONObject();
            item.put(FieldNames.GAME_ID, gameId);
            payload.add(item);
        });

        send(DECLINE_GAME_INVITE_REQUEST, payload, success -> {
            this.gotoOverview();
       
        });
    }

    /**
     *
     */
    public void startGame() {
        this.logAction("startGame");
    }

    /**
     *
     */
    public void sendRollDice() {
        this.logAction("sendRollDice");
    }

    /**
     *
     */
    public void movePiece() {
        this.logAction("movePiece");
    }





    // ------------------- GET REQUESTS -------------------

    /**
     *
     */
    public void getChat() {
        this.logAction("login");
    }


    /**
     *
     */
    public void getUser(Collection<Integer> userIds) {

        this.logAction("getUser");

    }
    /**
     *
     */
    public void getFriend() {
        this.logAction("getFriend");
    }

    /**
     *
     */
    public void getGame() {
        this.logAction("getGame");
    }

    /**
     *
     */
    public void getGameState() {
        this.logAction("getGameState");
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // API.Events implementation
    //

    /**
     * Something in the friends list has changed
     */
    public void friendUpdate() { }

    /**
     * Notify which chats has changed
     */
    public void chatUpdate(ArrayList<JSONObject> chats) { }

    /**
     * Handle incoming chat notifications
     */
    public void chatInvite(ArrayList<JSONObject> chatInvites) {}

    /**
     * Handle incoming chat messages
     */
    public void chatMessage(ArrayList<JSONObject> messages) {}

    /**
     * Notify which games has been changed
     */
    public void gameUpdate(ArrayList<JSONObject> games) {}

    /**
     * Handle incoming game invites
     */
    public void gameInvite(ArrayList<JSONObject> gameInvites) {}

    /**
     * Server has logged you out. Deal with it.
     */
    public void forceLogout() {
        this.gotoLogin();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Private functions
    //

    /**
     * Do prep-work for each action.
     *
     * @param methodName name of callee
     */
    private void logAction(String methodName) {
        Logger.log(Level.INFO, "Action -> " + methodName);
    }

    private void logError(JSONObject error) {
        var codes = error.getJSONArray(FieldNames.CODE);
        codes.forEach(code -> {
            Logger.log(Level.WARN, "code -> " + no.ntnu.imt3281.ludo.api.Error.fromInt((int)code).toString());
        });
    }

    /**
     * Generate random integer
     *
     * @return random integer
     */
    private static int randomInt() {
        return ThreadLocalRandom.current().nextInt(1000000, 9999999);
    }

    /**
     * Wrapper for the mAPI.send() and RequestFactory.make().
     * Sends all errors to this::logError
     *
     * @param type request type
     * @param payload request payload
     * @param success success callback function
     */
    private void send(RequestType type, JSONObject payload, RequestCallback success) {
        mAPI.send(mRequests.make(type, payload, mState.getAuthToken(), success, this::logError));
    }

    /**
     * Wrapper for the mAPI.send() and RequestFactory.make()
     * Sends all errors to this::logError
     *
     * @param type request type
     * @param payload request payload
     * @param success success callback function
     */
    private void send(RequestType type, ArrayList<JSONObject> payload, RequestCallback success) {
        mAPI.send(mRequests.make(type,payload,mState.getAuthToken(), success, this::logError));
    }
}