package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.api.Response;
import no.ntnu.imt3281.ludo.api.ResponseFactory;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.Logger.Level;
import no.ntnu.imt3281.ludo.gui.MutationConsumer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ArrayBlockingQueue;


public class ResponseConsumer {

    private MutationConsumer mMutationConsumer;
    private final ArrayBlockingQueue<JSONObject> mIncommingRequests = new ArrayBlockingQueue<JSONObject>(1);
    private final ResponseFactory mResponseFactory = new ResponseFactory();

    void bind(MutationConsumer mutationConsumer, SocketManager socketManager) {
        mMutationConsumer = mutationConsumer;
        socketManager.setOnReceiveCallback(this::feedMessage);
    }

    public void feedRequest(JSONObject request) {
        try {
            mIncommingRequests.put(request);
        } catch (InterruptedException e) {
            Logger.log(Logger.Level.INFO, "InterruptedException when feeding request");
        }
    }

    public void feedMessage(String message) {
        Logger.log(Level.DEBUG, message);

        Response response = new Response();
        try {
            response = mResponseFactory.makeResponse(message);
        } catch (JSONException e) {
            Logger.log(Level.WARN, "JSONException Response not JSON: " );
            return;
        }

        JSONObject request = mIncommingRequests.poll();
        if (request == null) {
            Logger.log(Level.ERROR, "No request found when processing response in ResponseConsumer");
        }

        switch (response.type) {
            case LoginResponse: {

            }break;
            case LogoutResponse: {

            }break;
            case GetUserResponse: {

            }break;
            case CreateUserResponse: {

            }break;
            case UpdateUserResponse: {

            }break;
            case DeleteUserResponse: {

            }break;
            case GetFriendResponse: {

            }break;
            case FriendResponse: {

            }break;
            case UnfriendResponse: {

            }break;
            case JoinChatResponse: {

            }break;
            case LeaveChatResponse: {

            }break;
            case GetChatResponse: {

            }break;
            case CreateChatResponse: {

            }break;
            case SendChatMessageResponse: {

            }break;
            case SendChatInviteResponse: {

            }break;
            case CreateGameResponse: {

            }break;
            case JoinGameResponse: {

            }break;
            case LeaveGameResponse: {

            }break;
            case SendGameInviteResponse: {

            }break;
            case DeclineGameInviteResponse: {

            }break;
            case StartGameResponse: {

            }break;
            case GetGameHeaderResponse: {

            }break;
            case GetGameStateResponse: {

            }break;
            case SendRollDiceResponse: {

            }break;
            case MovePieceResponse: {

            }break;
            default: {
                Logger.log(Level.WARN, "Unknown type. Response not handled: " + message);
            }
        }
    }
}