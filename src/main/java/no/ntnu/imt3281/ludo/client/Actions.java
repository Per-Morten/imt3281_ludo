package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.api.FieldNames;
import no.ntnu.imt3281.ludo.api.FriendStatus;
import no.ntnu.imt3281.ludo.api.GlobalChat;
import no.ntnu.imt3281.ludo.api.RequestType;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;
import no.ntnu.imt3281.ludo.gui.Transitions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static no.ntnu.imt3281.ludo.api.FieldNames.*;
import static no.ntnu.imt3281.ludo.api.RequestType.*;

public class Actions implements API.Events {

    private Transitions mTransitions;
    private API mAPI;
    private StateManager mState;
    private final RequestFactory mRequests = new RequestFactory();
    private Scene mCurrentScene;
    /**
     * Bind dependencies of Actions
     *
     * @param transitions to mutate the FXML state
     * @param API         to push requests to server, and get responses + events
     *                    from server
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

        mState.reset();

        mTransitions.renderLogin();
        mCurrentScene = Scene.LOGIN;
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

            mTransitions.renderLogin(email);
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
                state.password = password;
                state.email = email;
            });

            // Join global chat
            var payloadGlobalChat = new JSONObject();
            payloadGlobalChat.put(CHAT_ID, GlobalChat.ID);
            payloadGlobalChat.put(USER_ID, mState.getUserId());

            send(JOIN_CHAT_REQUEST, payloadGlobalChat, successJoin -> {
                send(GET_CHAT_REQUEST, payloadGlobalChat, successGetChat -> {

                    var globalChat = new Chat(successGetChat);
                    mState.commit(state -> {
                        state.activeChats.put(GlobalChat.ID, globalChat);
                    });
                });
            });

            mTransitions.toastInfo("Successfully logged in"); // TODO i18n
            this.gotoUser();

        }, error ->  {
            this.logError(error);
            this.gotoLogin();
        });
    }

    /**
     * Logout user. Return to login screen
     */
    public void logout() {
        this.logAction("logout");

        var payload = new JSONObject();
        payload.put(FieldNames.USER_ID, mState.getUserId());

        send(LOGOUT_REQUEST, payload, success -> this.gotoLogin(), error -> {
            this.logError(error);
            this.gotoLogin();
        });

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

            var user = new User(success);

            mState.commit(state -> {
                state.username = user.username;
                state.avatarURI = user.avatarURL;
                // TODO Email does not exist in get_user_request.
            });
            mTransitions.renderUser(user);
            mCurrentScene = Scene.USER;
        }, error -> {
            gotoLogin();
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

        mCurrentScene = Scene.LIVE;
    }

    /**
     * Goto the overview screen. Display list of games,chats,friends and users.
     * Filter the aforementioned lists by the value of each correponding search
     * field.
     */
    public void gotoOverview() {
        this.logAction("gotoOverview");

        var stateCopy = mState.copy();

        mTransitions.renderOverview(stateCopy.searchGames, stateCopy.searchChats, stateCopy.searchFriends, stateCopy.searchUsers);
        mTransitions.renderGamesList(mState.filteredGames(), mState.filteredGameInvites());
        mTransitions.renderChatsList(mState.filteredChats(), mState.filteredChatInvites());

        mCurrentScene = Scene.OVERVIEW;

        // Get friends range
        final var payload = new JSONObject();
        payload.put(FieldNames.PAGE_INDEX, 0);
        payload.put(FieldNames.USER_ID, stateCopy.userId);

        send(GET_FRIEND_RANGE_REQUEST, payload, successFriends -> {

            var friendsRange = successFriends.getJSONArray(FieldNames.RANGE);
            var friendsList = new ArrayList<JSONObject>();

            friendsRange.forEach(friend -> {
                friendsList.add((JSONObject) friend);
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

                    var userjson = (JSONObject) user;
                    int userId = userjson.getInt(FieldNames.USER_ID);

                    // Only add users who are not in the friends list
                    boolean found = false;
                    for (var friend : friendsList) {
                        var friendId = friend.getInt(FieldNames.USER_ID);
                        if (friendId == userId || userId == stateCopy.userId) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        usersList.add((JSONObject) user);
                    }
                });

                // Filter user by search
                var filteredUserList = usersList.stream().filter(user -> user.getString(FieldNames.USERNAME)
                        .toUpperCase().contains(stateCopy.searchUsers.toUpperCase())).collect(Collectors.toList());

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
     * Create game and add to active games list
     */
    public void createGame(String gameName) {
        this.logAction("createGame");

        var payload = new JSONObject();
        payload.put(FieldNames.USER_ID, mState.getUserId());
        payload.put(FieldNames.NAME, gameName);

        send(CREATE_GAME_REQUEST, payload, successCreateGame -> {
            send(GET_GAME_REQUEST, successCreateGame, successGetGame -> {

                var game = new Game(successGetGame);
                mState.commit(state -> {
                    state.activeGames.put(game.id, game);
                });
            });
        });

        var game = new Game();
        game.name = "Test game";
        game.id = 1;
        game.ownerId = mState.getUserId();
        game.playerId.add(mState.getUserId());
        game.status = 0;
        mTransitions.newGame(game.id, game);
    }

    // TODO REMOVE WHEN SERVER IMPLEMENTS CREATE GAME
    private static JSONObject makeGame() {
        var gameId = randomInt();
        var game = new JSONObject();
        game.put(FieldNames.GAME_ID, gameId);
        game.put(FieldNames.NAME, "Game " + gameId);
        game.put(FieldNames.PLAYER_ID, new JSONArray(new int[] {}));
        return game;
    }

    /**
     * Create chat and add to active chat list
     */
    public void createChat(String chatName) {
        this.logAction("createChat");

        var payload = new JSONObject();
        payload.put(FieldNames.USER_ID, mState.getUserId());
        payload.put(FieldNames.NAME, chatName);

        send(CREATE_CHAT_REQUEST, payload, successCreateChat -> {
            send(GET_CHAT_REQUEST, successCreateChat, successGetChat -> {

                var chat = new Chat(successGetChat);

                mState.commit(state -> {
                    state.activeChats.put(chat.id, chat);
                });
                mTransitions.newChat(chat);
            });
        });
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
     * Join chat you have been invited to or is visible to you
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
     * Join chat you have been invited to or active chat
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
            mState.commit(state -> {
                state.activeChats.remove(success.getInt(CHAT_ID));
            });
            this.gotoOverview();
        });
    }

    /**
     * Send a chat message as currently logged in user
     */
    public void sendChatMessage(int chatId, String message) {
        this.logAction("sendChatMessage");

        var payload = new JSONObject();
        payload.put(CHAT_ID, chatId);
        payload.put(FieldNames.USER_ID, mState.getUserId());
        payload.put(FieldNames.MESSAGE, message);

        send(SEND_CHAT_MESSAGE_REQUEST, payload);
    }

    /**
     * Send chat invite as currently logged in user
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
     * Join a collection of games
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
     * Leave a collection of games
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
            gamesId.forEach(id -> {
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
     * Updates the different search filteres
     *
     * @param searchGames search text games
     * @param searchChats search text chats
     * @param searchFriends search text friends
     * @param searchUsers search text users
     */
    public void search(String searchGames, String searchChats, String searchFriends, String searchUsers) {
        mState.commit(state -> {
            state.searchGames = searchGames;
            state.searchChats = searchChats;
            state.searchFriends = searchFriends;
            state.searchUsers = searchUsers;
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
    public void friendUpdate() {
        // TODO if in overview scene update friendlist
        if (mCurrentScene.equals(Scene.OVERVIEW)) {
            this.gotoOverview();
        }
        mTransitions.toastInfo("Friends list updated"); // TODO i18n
    }

    /**
     * Notify which chats has changed
     */
    public void chatUpdate(JSONObject chats) {
        send(GET_CHAT_REQUEST, chats, success -> {
            var chat = new Chat(success);

            mState.commit(state -> {
                state.activeChats.put(chat.id, chat);
            });

            if (mCurrentScene.equals(Scene.OVERVIEW)) {
                this.gotoOverview();
            } else if (mCurrentScene.equals(Scene.LIVE)) {
                this.gotoLive();
            }
        });
    }

    /**
     * Handle incoming chat invites
     */
    public void chatInvite(JSONObject chatInviteJSON) {

        var chatInvite = new ChatInvite(chatInviteJSON);

        var payloadUser = new JSONObject();
        payloadUser.put(USER_ID, chatInvite.userId);
        send(GET_USER_REQUEST,payloadUser, successUser -> {

            var user = new User(successUser);

            var payloadChat = new JSONObject();
            payloadChat.put(CHAT_ID, chatInvite.chatId);
            send(GET_CHAT_REQUEST, payloadChat, successChat -> {

                var chat = new Chat(successChat);
                chatInvite.userName = user.username;
                chatInvite.chatName = chat.name;

                mState.commit(state -> {
                    state.chatInvites.put(chatInvite.chatId, chatInvite);
                });


                if (mCurrentScene.equals(Scene.OVERVIEW)) {
                    this.gotoOverview();
                }
                mTransitions.toastInfo("New chat invite from" + chatInvite.userName + " " + "to" + " " + chatInvite.chatName); // TODO i18n
            });
        });
    }

    /**
     * Handle incoming chat messages. Make sure there is a user matching the user_id in state.
     */
    public void chatMessage(JSONObject messagejson) {

        var payload = new JSONObject();
        payload.put(USER_ID, messagejson.getInt(USER_ID));

        send(GET_USER_REQUEST, payload, success -> {
            var user = new User(success);
            var message = new ChatMessage(messagejson);
            message.username = user.username;

            mState.commit(state -> {
                state.activeChats.get(message.chatId).messages.add(message);
            });

            if (mCurrentScene.equals(Scene.LIVE)) {
                mTransitions.newMessage(message);
            }
            mTransitions.toastInfo(message.username + ": " + message.message);
        });
    }

    /**
     * Notify which games has been changed
     */
    public void gameUpdate(JSONObject games) {
        send(GET_GAME_REQUEST, games, success -> {
            var game = new Game(success);

            mState.commit(state -> {
                state.activeGames.put(game.id, game);
            });

            if(mCurrentScene.equals(Scene.LIVE)) {
                this.gotoLive();
            } else if(mCurrentScene.equals(Scene.OVERVIEW)) {
                this.gotoOverview();
            }
        });
    }

    /**
     * Handle incoming game invites
     */
    public void gameInvite(JSONObject gameInvitesJSON) {
        var gameInvite = new GameInvite(gameInvitesJSON);

        var payloadUser = new JSONObject();
        payloadUser.put(USER_ID, gameInvite.userId);
        send(GET_USER_REQUEST,payloadUser, successUser -> {

            var user = new User(successUser);

            var payloadGame = new JSONObject();
            payloadGame.put(CHAT_ID, gameInvite.gameId);
            send(GET_GAME_REQUEST, payloadGame, successGame -> {

                var game = new Game(successGame);
                gameInvite.userName = user.username;
                gameInvite.gameName = game.name;

                mState.commit(state -> {
                    state.gameInvites.put(gameInvite.gameId, gameInvite);
                });
                if (mCurrentScene.equals(Scene.OVERVIEW)) {
                    this.gotoOverview();
                }
                mTransitions.toastInfo("New game invite from" + " " + gameInvite.userName + " " + "to" + " " + gameInvite.gameName); // TODO i18n
            });
        });
    }

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
            Logger.log(Level.WARN, "code -> " + no.ntnu.imt3281.ludo.api.Error.fromInt((int) code).toString());

            mTransitions.toastError(no.ntnu.imt3281.ludo.api.Error.fromInt((int) code).toString());
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
     * Wrapper for the mAPI.send() and RequestFactory.make()
     *
     * @param type    request type
     * @param payload request payload
     * @param success success callback function
     * @param error   error callback function
     */
    private void send(RequestType type, JSONObject payload, RequestCallback success, RequestCallback error) {
        mAPI.send(mRequests.make(type, payload, mState.getAuthToken(), success, error));
    }

    /**
     * Wrapper for the mAPI.send() and RequestFactory.make(). Sends all errors to
     * this::logError
     *
     * @param type    request type
     * @param payload request payload
     * @param success success callback function
     */
    private void send(RequestType type, JSONObject payload, RequestCallback success) {
        mAPI.send(mRequests.make(type, payload, mState.getAuthToken(), success, this::logError));
    }

    /**
     * Wrapper for the mAPI.send() and RequestFactory.make() Sends all errors to
     * this::logError
     *
     * @param type    request type
     * @param payload request payload
     * @param success success callback function
     */
    private void send(RequestType type, ArrayList<JSONObject> payload, RequestCallback success) {
        mAPI.send(mRequests.make(type, payload, mState.getAuthToken(), success, this::logError));
    }


    /**
     * Wrapper for the mAPI.send() and RequestFactory.make(). Sends all errors to
     * this::logError. Empty success
     *
     * @param type    request type
     * @param payload request payload
     */
    private void send(RequestType type, JSONObject payload) {
        mAPI.send(mRequests.make(type, payload, mState.getAuthToken(), success -> {}, this::logError));
    }

}