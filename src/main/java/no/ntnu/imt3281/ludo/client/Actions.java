package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.api.FieldNames;
import no.ntnu.imt3281.ludo.api.FriendStatus;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;
import no.ntnu.imt3281.ludo.gui.Transitions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static no.ntnu.imt3281.ludo.api.RequestType.*;

public class Actions {

    private Transitions mTransitions;
    private API mAPI;
    private StateManager mStateManager;
    private final RequestFactory mRequests = new RequestFactory();

    /**
     * Bind dependencies of Actions
     *
     * @param transitions to feed mutations
     * @param API         to push requests
     */
    void bind(Transitions transitions, API API, StateManager stateManager) {
        mTransitions = transitions;
        mStateManager = stateManager;
        mAPI = API;
    }

    void gotoLogin() {
        this.startAction("gotoLogin");

        mStateManager.commit(gState -> {
            gState.userId = -1;
            gState.email = "";
            gState.authToken = "";
            gState.username = "";
            gState.avatarURI = "";
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
     * @param email    valid email
     * @param password valid password
     */
    public void login(String email, String password) {
        this.startAction("login");

        var payload = new JSONObject();
        payload.put(FieldNames.EMAIL, email);
        payload.put(FieldNames.PASSWORD, password);

        mAPI.sendNoToken(mRequests.make(LOGIN_REQUEST, payload, "", success -> {

            mStateManager.commit(gState -> {
                gState.userId = success.getInt(FieldNames.USER_ID);
                gState.authToken = success.getString(FieldNames.AUTH_TOKEN);
            });

            this.gotoUser();
        },
        this::logError));
    }

    /**
     * login user with username and password
     *
     * @param email    valid email
     * @param username valid username
     * @param password valid password
     */
    public void createUser(String email, String password, String username) {
        this.startAction("createUser");

        mTransitions.renderLogin();

        var item = new JSONObject();
        item.put(FieldNames.EMAIL, email);
        item.put(FieldNames.PASSWORD, password);
        item.put(FieldNames.USERNAME, username);

        mAPI.sendNoToken(mRequests.make(CREATE_USER_REQUEST, item, "",
            success -> {
                mStateManager.commit(gState -> {
                    gState.userId = success.getInt(FieldNames.USER_ID);
                    gState.email = email;
                    gState.username = username;
                });

                mTransitions.renderLogin();
            },
            this::logError));

    }

    /**
     * Logout the user with user_id stored in state. Remove token and other userdata from state
     */
    public void logout() {
        var state = this.startAction("logout");

        var item = new JSONObject();
        item.put(FieldNames.USER_ID, state.userId);

        mAPI.send(mRequests.make(LOGOUT_REQUEST, item, state.authToken,
            success -> {},
            this::logError));

        this.gotoLogin();
    }

    /**
     *
     */
    public void updateUser(String username, String email, String password, String avatarURI) {
        var state = this.startAction("updateUser");

        var payload = new JSONObject();
        payload.put(FieldNames.USER_ID, state.userId);
        payload.put(FieldNames.USERNAME, username);
        payload.put(FieldNames.EMAIL, email);
        payload.put(FieldNames.PASSWORD, password);
        payload.put(FieldNames.AVATAR_URI, avatarURI);

        mAPI.send(mRequests.make(UPDATE_USER_REQUEST, payload, state.authToken, success -> {

            mStateManager.commit(gState -> {
                gState.username = username;
                gState.email = email;
                gState.avatarURI = avatarURI;
            });

            this.gotoUser();

        }, this::logError));
    }

    /**
     *
     */
    public void deleteUser() {
        var state = this.startAction("deleteUser");

        var payload = new JSONObject();
        payload.put(FieldNames.USER_ID, state.userId);

        mAPI.send(mRequests.make(DELETE_USER_REQUEST, payload, state.authToken, success -> {
            this.logout();
        }, this::logError));
    }

    /**
     * Goto the user scene
     */
    public void gotoUser() {
        var state = this.startAction("gotoUser");

        var payload = new JSONObject();
        payload.put(FieldNames.USER_ID, state.userId);

        mAPI.send(mRequests.make(GET_USER_REQUEST, payload, state.authToken, success -> {
            Logger.log(Level.DEBUG, "Get_user_success");

            var user = new User();
            user.json = success;

            mStateManager.commit(gState -> {
                gState.username = success.getString(FieldNames.USERNAME);
                gState.avatarURI = success.getString(FieldNames.AVATAR_URI);
                // TODO Email does not exist in get_user_request.

            });

            mTransitions.renderUser(user);
        },
        this::logError));
    }

    /**
     * Goto the live scene
     */
    public void gotoLive() {
        mTransitions.renderLive();
        mTransitions.renderGameTabs();
        mTransitions.renderChatTabs();
    }

    /**
     * Goto the overview scene
     */
    public void gotoOverview() {
        var state = this.startAction("gotoOverview");

        mTransitions.renderOverview();
        mTransitions.renderGamesList(state.activeGames, state.gameInvites);
        mTransitions.renderChatsList(state.activeChats, state.chatInvites);

        // Get friends range
        final var payload = new JSONObject();
        payload.put(FieldNames.PAGE_INDEX, 0);
        payload.put(FieldNames.USER_ID, state.userId);

        mAPI.send(mRequests.make(GET_FRIEND_RANGE_REQUEST, payload, state.authToken, successFriends -> {

            var friendsRange = successFriends.getJSONArray(FieldNames.RANGE);
            var friendsList = new ArrayList<JSONObject>();

            friendsRange.forEach(friend -> {
                friendsList.add((JSONObject)friend);
            });
            mTransitions.renderFriendList(friendsList);

            mAPI.send(mRequests.make(GET_USER_RANGE_REQUEST, payload, state.authToken, successUsers -> {

                var usersRange = successUsers.getJSONArray(FieldNames.RANGE);
                var usersList = new ArrayList<JSONObject>();
                usersRange.forEach(user -> {

                    var userjson = (JSONObject)user;
                    int userId = userjson.getInt(FieldNames.USER_ID);

                    // Only add users who are not in the friendslist
                    boolean found = false;
                    for (var friend: friendsList) {
                        var friendId = friend.getInt(FieldNames.USER_ID);
                        if (friendId == userId || userId == state.userId) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        usersList.add((JSONObject)user);
                    }
                });
                mTransitions.renderUserList(usersList, friendsList);
            },
            this::logError));
        },
        this::logError));
    }

    /**
     * Create game and add as active
     */
    public void createGame(/* TODO pass name*/) {
        var state = this.startAction("createGame");

        var game = makeGame();
        var gameId = game.getInt(FieldNames.GAME_ID);

        var payload = new JSONObject();
        payload.put(FieldNames.USER_ID, state.userId);
        payload.put(FieldNames.NAME, "Game " + gameId);

        mAPI.send(mRequests.make(CREATE_GAME_REQUEST, payload, state.authToken, success -> {
            mStateManager.commit(gState -> {
                gState.activeGames.put(gameId, game);
            });

            mTransitions.newGame(gameId, game);
        },
        this::logError));
    }


    /**
     * Create chat and add as active
     */
    public void createChat(/* TODO pass name*/) {
        var state = this.startAction("createChat");

        var payload = new JSONObject();
        var chat = makeChat();
        var chatId = chat.getInt(FieldNames.CHAT_ID);
        payload.put(FieldNames.USER_ID, state.userId);
        payload.put(FieldNames.NAME, "Chat " + chatId);

        mAPI.send(mRequests.make(CREATE_CHAT_REQUEST, payload, state.authToken, success -> {
            mStateManager.commit(gState -> {
                gState.activeChats.put(chatId, chat);
            });
            mTransitions.newChat(chatId, chat);
        },
        this::logError));
    }


    /**
     *
     */
    public void friend(HashSet<Integer> friendsId) {
        var state = this.startAction("friend");

        var payload = new ArrayList<JSONObject>();
        friendsId.forEach(friendId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, state.userId);
            item.put(FieldNames.OTHER_ID, friendId);
            payload.add(item);
        });

        mAPI.send(mRequests.make(FRIEND_REQUEST, payload, state.authToken,
            success -> {
                this.gotoOverview();
            },
            this::logError));
    }

    /**
     *
     */
    public void unfriend(HashSet<Integer> friendsId) {
        var state = this.startAction("unfriend");

        var payload = new ArrayList<JSONObject>();
        friendsId.forEach(friendId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, state.userId);
            item.put(FieldNames.OTHER_ID, friendId);
            payload.add(item);
        });

        mAPI.send(mRequests.make(UNFRIEND_REQUEST, payload, state.authToken,
            success -> {
                this.gotoOverview();
            },
            this::logError));
    }

    /**
     *
     */
    public void ignore(HashSet<Integer> usersId) {
        var state = this.startAction("ignore");

        var payload = new ArrayList<JSONObject>();
        usersId.forEach(userId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, state.userId);
            item.put(FieldNames.OTHER_ID, userId);
            payload.add(item);
        });

        mAPI.send(mRequests.make(IGNORE_REQUEST, payload, state.authToken,
            success -> {
                this.gotoOverview();
            },
            this::logError));
    }

    /**
     *
     */
    public void unignore(HashSet<Integer> usersId) {
        var state = this.startAction("unignore");

        var payload = new ArrayList<JSONObject>();
        usersId.forEach(userId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, state.userId);
            item.put(FieldNames.OTHER_ID, userId);
            payload.add(item);
        });

        mAPI.send(mRequests.make(UNFRIEND_REQUEST, payload, state.authToken,
            success -> {
                this.gotoOverview();
            },
            this::logError));
    }

    /**
     *
     */
    public void joinChat(HashSet<Integer> chatsId) {
        var state = this.startAction("joinChat");

        var payload = new ArrayList<JSONObject>();
        chatsId.forEach(chatId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, state.userId);
            item.put(FieldNames.CHAT_ID, chatId);
            payload.add(item);
        });

        mAPI.send(mRequests.make(JOIN_CHAT_REQUEST, payload, state.authToken,
            success -> {
                this.gotoOverview();
            },
            this::logError));
    }

    /**
     *
     */
    public void leaveChat(HashSet<Integer> chatsId) {
        var state = this.startAction("leaveChat");

        var payload = new ArrayList<JSONObject>();
        chatsId.forEach(chatId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, state.userId);
            item.put(FieldNames.CHAT_ID, chatId);
            payload.add(item);
        });

        mAPI.send(mRequests.make(LEAVE_CHAT_REQUEST, payload, state.authToken,
            success -> {
                this.gotoOverview();
            },
            this::logError));
    }

    /**
     *
     */
    public void sendChatMessage(int chatId, String message) {
        var state = this.startAction("sendChatMessage");

        var payload = new JSONObject();
        payload.put(FieldNames.USER_ID, state.userId);
        payload.put(FieldNames.CHAT_ID, chatId);
        payload.put(FieldNames.MESSAGE, message);

        mAPI.send(mRequests.make(SEND_CHAT_MESSAGE_REQUEST, payload, state.authToken,
            success -> {
                mTransitions.newMessage(chatId, state.username, message);
            },
            this::logError));
    }

    /**
     *
     */
    public void sendChatInvite(HashSet<Integer> chatsId, HashSet<Integer> friendsId) {
        var state = this.startAction("sendChatInvite");

        var payload = new ArrayList<JSONObject>();
        chatsId.forEach(chatId -> {
            friendsId.forEach(friendId -> {
                var item = new JSONObject();
                item.put(FieldNames.USER_ID, state.userId);
                item.put(FieldNames.OTHER_ID, friendId);
                item.put(FieldNames.CHAT_ID, chatId);
                payload.add(item);
            });
        });

        mAPI.send(mRequests.make(SEND_CHAT_INVITE_REQUEST, payload, state.authToken,
        success -> {
            this.gotoOverview();
        },
        this::logError));
    }

    /**
     *
     */
    public void joinGame(HashSet<Integer> gamesId) {
        var state = this.startAction("joinGame");

        var payload = new ArrayList<JSONObject>();
        gamesId.forEach(gameId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, state.userId);
            item.put(FieldNames.GAME_ID, gameId);
            payload.add(item);
        });

        mAPI.send(mRequests.make(JOIN_GAME_REQUEST, payload, state.authToken,
        success -> {
            this.gotoOverview();
        },
        this::logError));
    }

    /**
     *
     */
    public void leaveGame(HashSet<Integer> gamesId) {
        var state = this.startAction("leaveGame");

        var payload = new ArrayList<JSONObject>();
        gamesId.forEach(gameId -> {
            var item = new JSONObject();
            item.put(FieldNames.USER_ID, state.userId);
            item.put(FieldNames.GAME_ID, gameId);
            payload.add(item);
        });

        mAPI.send(mRequests.make(LEAVE_GAME_REQUEST, payload, state.authToken,
        success -> {
            this.gotoOverview();
        },
        this::logError));
    }

    /**
     *
     */
    public void sendGameInvite(HashSet<Integer> gamesId, HashSet<Integer> friendsId) {
        var state = this.startAction("sendGameInvite");

        var payload = new ArrayList<JSONObject>();
        gamesId.forEach(gameId -> {
            friendsId.forEach(friendId -> {
                var item = new JSONObject();
                item.put(FieldNames.USER_ID, state.userId);
                item.put(FieldNames.OTHER_ID, friendId);
                item.put(FieldNames.GAME_ID, gameId);
                payload.add(item);
            });
        });

        mAPI.send(mRequests.make(SEND_GAME_INVITE_REQUEST, payload, state.authToken,
        success -> {
            this.gotoOverview();
        },
        this::logError));
    }

    /**
     *
     */
    public void declineGameInvite(HashSet<Integer> gamesId) {
        var state = this.startAction("declineGameInvite");

        var payload = new ArrayList<JSONObject>();
        gamesId.forEach(gameId -> {
            var item = new JSONObject();
            item.put(FieldNames.GAME_ID, gameId);
            payload.add(item);
        });

        mAPI.send(mRequests.make(DECLINE_GAME_INVITE_REQUEST, payload, state.authToken,
        success -> {
            this.gotoOverview();
        },
        this::logError));
    }

    /**
     *
     */
    public void startGame() {
        var state = this.startAction("startGame");
    }

    /**
     *
     */
    public void sendRollDice() {
        var state = this.startAction("sendRollDice");
    }

    /**
     *
     */
    public void movePiece() {
        var state = this.startAction("movePiece");
    }





    // ------------------- GET REQUESTS -------------------

    /**
     *
     */
    public void getChat() {
        var state = this.startAction("login");
    }


    /**
     *
     */
    public void getUser(Collection<Integer> userIds) {

        var state = this.startAction("getUser");

    }
    /**
     *
     */
    public void getFriend() {
        var state = this.startAction("getFriend");
    }

    /**
     *
     */
    public void getGame() {
        var state = this.startAction("getGame");
    }

    /**
     *
     */
    public void getGameState() {
        var state = this.startAction("getGameState");
    }


    void friendUpdate() {}
    void chatUpdate() {}
    void chatInvite(ArrayList<JSONObject> chats) {}
    void chatMessage(ArrayList<JSONObject> messages) {}
    void gameUpdate(ArrayList<JSONObject> games) {}
    void gameInvite(ArrayList<JSONObject> gameInvites) {}
    void forceLogout() {
        this.gotoLogin();
    }


    /**
     * Do prep-work for each action.
     *
     * @param methodName name of callee
     */
    private State startAction(String methodName) {
        Logger.log(Level.INFO, "Action -> " + methodName);
        return mStateManager.copy();
    }

    private void logError(JSONObject error) {
        var codes = error.getJSONArray(FieldNames.CODE);
        codes.forEach(code -> {
            Logger.log(Level.WARN, "code -> " + no.ntnu.imt3281.ludo.api.Error.fromInt((int)code).toString());
        });
    }

    private static int randomId() {
        return ThreadLocalRandom.current().nextInt(1000000, 9999999);
    }

    private static JSONObject makeGame() {
        var gameId = randomId();
        var game = new JSONObject();
        game.put(FieldNames.GAME_ID, gameId);
        game.put(FieldNames.NAME, "Game " + gameId);
        game.put(FieldNames.PLAYER_ID, new JSONArray(new int[]{}));
        return game;
    }

    private static JSONObject makeChat() {
        var chatId = randomId();
        var chat = new JSONObject();
        chat.put(FieldNames.CHAT_ID, chatId);
        chat.put(FieldNames.NAME, "Chat " + chatId);
        chat.put(FieldNames.PARTICIPANT_ID, new JSONArray(new int[]{}));
        return chat;
    }
}