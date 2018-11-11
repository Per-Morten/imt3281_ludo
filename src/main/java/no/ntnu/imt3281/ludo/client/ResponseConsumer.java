package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.api.APIFunctions;
import no.ntnu.imt3281.ludo.api.Response;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;
import no.ntnu.imt3281.ludo.gui.MutationConsumer;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ArrayBlockingQueue;


public class ResponseConsumer {

    private MutationConsumer mMutationConsumer;
    private final ArrayBlockingQueue<JSONObject> mIncommingRequests = new ArrayBlockingQueue<JSONObject>(1);

    void bind(MutationConsumer mutationConsumer) {
        mMutationConsumer = mutationConsumer;
    }

    public void feedRequest(JSONObject request) {
        try {
            mIncommingRequests.put(request);
        } catch (InterruptedException e) {
            Logger.log(Logger.Level.INFO, "InterruptedException when feeding request" + e.toString());
        }
    }

    public void feedMessage(String message) {
        Logger.log(Level.INFO, "Got a response: " + message);

        JSONObject request = mIncommingRequests.poll();
        if (request == null) {
            Logger.log(Level.WARN, "No request found when processing response in ResponseConsumer");
            return;
        }

        var response = new Response();
        try {
            response = APIFunctions.makeResponse(message);
        } catch (JSONException e) {
            Logger.log(Level.WARN, "Exception when parsing JSON" + e.toString());
            return;
        } catch (IllegalArgumentException e) {
            Logger.log(Level.WARN, "Response type is invalid" + e.toString());
            return;
        } catch (ClassCastException e) {
            Logger.log(Level.WARN, "Woot" + e.toString());
            return;
        } catch (Exception e) {
            Logger.log(Level.WARN,  e.toString());
            return;
        }


        switch (response.type) {
            case LoginResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.loginSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Login error")));
            } break;

            case LogoutResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.logoutSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Logout error")));
            } break;

            case GetUserResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.getUserSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Get user error")));
            } break;

            case CreateUserResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.createUserSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Create user error")));
            } break;

            case UpdateUserResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.updateUserSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Update user error")));
            } break;

            case DeleteUserResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.deleteUserSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Delete user error")));
            } break;

            case GetFriendResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.getFriendSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Get friend error")));
            } break;

            case FriendResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.friendSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Friend error")));
            } break;

            case UnfriendResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.unfriendSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Unfriend error")));
            } break;

            case JoinChatResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.joinChatSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Join chat error")));
            } break;

            case LeaveChatResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.leaveChatSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Leave Chat error")));
            } break;

            case GetChatResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.getChatSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Get Chat error")));
            } break;

            case CreateChatResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.createChatSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Creat Chat error")));
            } break;

            case SendChatMessageResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.sendChatMessageSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Send Chat Message error")));
            } break;

            case SendChatInviteResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.sendChatInviteSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Send Chat Invite error")));
            } break;

            case CreateGameResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.createGameSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Create Game error")));
            } break;

            case JoinGameResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.joinGameSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Join Game error")));
            } break;

            case LeaveGameResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.leaveGameSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Leave Game error")));
            } break;

            case SendGameInviteResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.sendGameInviteSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Send Game Invite error")));
            } break;

            case DeclineGameInviteResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.declineGameInviteSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Decline Game Invite error")));
            } break;

            case StartGameResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.startGameSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Start Game error")));
            } break;

            case GetGameResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.getGameSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Get Game error")));
            } break;

            case GetGameStateResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.getGameStateSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Get Game State error")));
            } break;

            case SendRollDiceResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.sendRollDiceSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Send Roll Dice error")));
            } break;

            case MovePieceResponse: {
                response.success.forEach(success -> mMutationConsumer.feed(m -> m.movePieceSuccess()));
                response.error.forEach(error -> mMutationConsumer.feed(m -> m.error("Move Piece error")));
            } break;

            default: {
                Logger.log(Level.WARN, "Unknown type. Response not handled: " + message);
            }
        }
    }
}