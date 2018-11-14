package no.ntnu.imt3281.ludo.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JSONValidator {
    /**
     * Checks that the message contains all the fields needed as indicated by the api.
     * Returns true in the case where the message is valid, false otherwise.
     *
     * Currently only Request types are supported, as this is most important on the server.
     *
     * @param message
     * @return
     */
    public static boolean isValidMessage(String message) {
        JSONObject json;
        try {
            json = new JSONObject(message);
        } catch (JSONException e) {
            return false;
        }

        if (!hasInt(FieldNames.ID, json) || !hasJSONArray(FieldNames.PAYLOAD, json) || !hasString(FieldNames.TYPE, json)) {
            return false;
        }

        var type = json.getString(FieldNames.TYPE);
        if (RequestType.fromString(type) != null) {
            return verifyRequest(json);
        }

        return false;
    }

    private static boolean hasInt(String key, JSONObject json) {
        if (json.has(key)) {
            try {
                json.getInt(key);
                return true;
            } catch(JSONException e) {
                return false;
            }
        }
        return false;
    }

    private static boolean hasString(String key, JSONObject json) {
        if (json.has(key)) {
            try {
                json.getString(key);
                return true;
            } catch(JSONException e) {
                return false;
            }
        }
        return false;
    }

    private static boolean hasJSONArray(String key, JSONObject json) {
        if (json.has(key)) {
            try {
                json.getJSONArray(key);
                return true;
            } catch(JSONException e) {
                return false;
            }
        }
        return false;
    }

    private static boolean hasJSONObject(String key, JSONObject json) {
        if (json.has(key)) {
            try {
                json.getJSONObject(key);
                return true;
            } catch(JSONException e) {
                return false;
            }
        }
        return false;
    }

    private static boolean hasJSONObject(int index, JSONArray json) {
        try {
            json.getJSONArray(index);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    // This can be generalized to handle all message types if make it generic and send in the array.
    private static boolean verifyRequest(JSONObject json) {
        if (!hasJSONArray(FieldNames.PAYLOAD, json))
            return false;

        var type = RequestType.fromString(json.getString(FieldNames.TYPE));
        var payload = json.getJSONArray(FieldNames.PAYLOAD);

        for (int i = 0; i < payload.length(); i++) {
            if (!hasJSONObject(i, payload)) {
                return false;
            }

            var item = payload.getJSONObject(i); // Must validate that this is a json object
            if (!hasInt(FieldNames.ID, item)) {
                return false;
            }

            for (var field : sRequestTypes) {
                if (field.messageType.contains(type)) {
                    if (field.type == FieldType.INTEGER && !hasInt(field.fieldName, json))
                        return false;
                    if (field.type == FieldType.STRING && !hasString(field.fieldName, json))
                        return false;
                    if (field.type == FieldType.JSON_ARRAY && !hasJSONArray(field.fieldName, json))
                        return false;
                    if (field.type == FieldType.JSON_OBJECT && !hasJSONObject(field.fieldName, json))
                        return false;
                }
            }
        }

        return true;
    }

    private enum FieldType {
        INTEGER,
        STRING,
        JSON_OBJECT,
        JSON_ARRAY,
    }

    private static class Field<T> {
        String fieldName;
        FieldType type;
        List<T> messageType;

        Field(String fieldname, FieldType type, T[] messageType) {
            this.fieldName = fieldname;
            this.type = type;
            this.messageType = Arrays.asList(messageType);
        }
    }

    // Not Particularly pretty but it gets the job done.
    public static void init() {
        // Requests
        sRequestTypes.add(new Field<>(FieldNames.USERNAME, FieldType.INTEGER, new RequestType[]{
                RequestType.CREATE_USER_REQUEST,
                RequestType.UPDATE_USER_REQUEST,
        }));

        sRequestTypes.add(new Field<>(FieldNames.EMAIL, FieldType.STRING, new RequestType[] {
                RequestType.CREATE_USER_REQUEST,
                RequestType.LOGIN_REQUEST,
                RequestType.UPDATE_USER_REQUEST,
        }));

        sRequestTypes.add(new Field<>(FieldNames.PASSWORD, FieldType.STRING, new RequestType[] {
                RequestType.CREATE_USER_REQUEST,
                RequestType.LOGIN_REQUEST,
                RequestType.UPDATE_USER_REQUEST,
        }));

        sRequestTypes.add(new Field<>(FieldNames.USER_ID, FieldType.INTEGER, new RequestType[] {
                RequestType.LOGOUT_REQUEST,
                RequestType.GET_USER_REQUEST,
                RequestType.UPDATE_USER_REQUEST,
                RequestType.DELETE_USER_REQUEST,
                RequestType.FRIEND_REQUEST,
                RequestType.UNFRIEND_REQUEST,
                RequestType.CREATE_CHAT_REQUEST,
                RequestType.JOIN_CHAT_REQUEST,
                RequestType.LEAVE_CHAT_REQUEST,
                RequestType.SEND_CHAT_MESSAGE_REQUEST,
                RequestType.SEND_CHAT_INVITE_REQUEST,
                RequestType.CREATE_GAME_REQUEST,
                RequestType.SEND_GAME_INVITE_REQUEST,
                RequestType.DECLINE_GAME_INVITE_REQUEST,
                RequestType.JOIN_GAME_REQUEST,
                RequestType.LEAVE_GAME_REQUEST,
                RequestType.ROLL_DICE_REQUEST,
        }));

        sRequestTypes.add(new Field<>(FieldNames.PAGE_INDEX, FieldType.INTEGER,  new RequestType[] {
                RequestType.GET_USER_RANGE_REQUEST,
                RequestType.GET_FRIEND_RANGE_REQUEST,
                RequestType.GET_CHAT_RANGE_REQUEST,
                RequestType.GET_GAME_RANGE_REQUEST,
        }));

        sRequestTypes.add(new Field<>(FieldNames.AVATAR_URI, FieldType.STRING, new RequestType[] {
            RequestType.UPDATE_USER_REQUEST,
        }));

        sRequestTypes.add(new Field<>(FieldNames.OTHER_ID, FieldType.INTEGER, new RequestType[] {
                RequestType.FRIEND_REQUEST,
                RequestType.UNFRIEND_REQUEST,
                RequestType.SEND_CHAT_INVITE_REQUEST,
                RequestType.SEND_GAME_INVITE_REQUEST,
        }));

        sRequestTypes.add(new Field<>(FieldNames.NAME, FieldType.STRING, new RequestType[]{
            RequestType.CREATE_CHAT_REQUEST,
        }));

        sRequestTypes.add(new Field<>(FieldNames.CHAT_ID, FieldType.INTEGER, new RequestType[] {
                RequestType.JOIN_CHAT_REQUEST,
                RequestType.LEAVE_CHAT_REQUEST,
                RequestType.GET_CHAT_REQUEST,
                RequestType.SEND_CHAT_MESSAGE_REQUEST,
                RequestType.SEND_CHAT_INVITE_REQUEST,
        }));

        sRequestTypes.add(new Field<>(FieldNames.MESSAGE, FieldType.STRING, new RequestType[] {
                RequestType.SEND_CHAT_MESSAGE_REQUEST,
        }));

        sRequestTypes.add(new Field<>(FieldNames.GAME_ID, FieldType.INTEGER, new RequestType[] {
                RequestType.SEND_GAME_INVITE_REQUEST,
                RequestType.DECLINE_GAME_INVITE_REQUEST,
                RequestType.JOIN_GAME_REQUEST,
                RequestType.LEAVE_GAME_REQUEST,
                RequestType.START_GAME_REQUEST,
                RequestType.GET_GAME_REQUEST,
                RequestType.GET_GAME_STATE_REQUEST,
                RequestType.ROLL_DICE_REQUEST,
                RequestType.MOVE_PIECE_REQUEST
        }));

        sRequestTypes.add(new Field<>(FieldNames.PIECE_INDEX, FieldType.INTEGER, new RequestType[] {
                RequestType.MOVE_PIECE_REQUEST,
        }));
    }

    private static ArrayList<Field<RequestType>> sRequestTypes = new ArrayList<>();
}
