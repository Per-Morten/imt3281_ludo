package no.ntnu.imt3281.ludo.api;

import no.ntnu.imt3281.ludo.common.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for validating the JSON messages that are sent through the API.
 * NOTE: Currently only validation of requests is supported!
 */
public class JSONValidator {
    /**
     * Checks that the message contains all the fields needed as indicated by the api.
     * Returns true in the case where the message is valid, false otherwise.
     *
     * Currently only Request types are supported, as this is most important on the server.
     *
     * @param message The message to check the validity of.
     * @return True of the message is valid, false otherwise.
     */
    public static boolean isValidMessage(String message) {
        JSONObject json;
        try {
            json = new JSONObject(message);
        } catch (JSONException e) {
            Logger.log(Logger.Level.DEBUG, "JSON object threw exception upon parsing");
            return false;
        }

        if (!hasInt(FieldNames.ID, json) || !hasString(FieldNames.TYPE, json)) {
            Logger.log(Logger.Level.DEBUG, String.format("Object missing: %s or %s", FieldNames.ID, FieldNames.TYPE));
            return false;
        }

        var type = json.getString(FieldNames.TYPE);
        if (RequestType.fromString(type) != null) {
            return verifyRequest(json);
        }
        else {
            Logger.log(Logger.Level.DEBUG, String.format("Object RequestType Invalid, %s", type));
        }

        return false;
    }

    /**
     * Checks if the JSONObject has the object int type with the key key.
     *
     * The JSON library we use does not support a way to check if a value if of a specific type
     * in addition to it being present. Rather throwing an exception upon getting that value if it is of incorrect type.
     * This leads to using exceptions as control flow, which isn't desirable, therefore we added this method to at encapsulate
     * that controlflow.
     *
     * @param key The key of the value.
     * @param json The JSONObject to check.
     * @return True of it has an integer named key, false otherwise.
     */
    public static boolean hasInt(String key, JSONObject json) {
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

    /**
     * Checks if the JSONObject has the object String type with the key key.
     *
     * The JSON library we use does not support a way to check if a value if of a specific type
     * in addition to it being present. Rather throwing an exception upon getting that value if it is of incorrect type.
     * This leads to using exceptions as control flow, which isn't desirable, therefore we added this method to at encapsulate
     * that controlflow.
     *
     * @param key The key of the value.
     * @param json The JSONObject to check.
     * @return True of it has a String named key, false otherwise.
     */
    public static boolean hasString(String key, JSONObject json) {
        if (json.has(key)) {
            try {
                json.getString(key);
                return true;
            } catch(JSONException e) {
                Logger.logException(Logger.Level.DEBUG, e, "Exception was thrown in hasString");
                return false;
            }
        }
        Logger.log(Logger.Level.DEBUG, String.format("Didn't have string: %s, jsonObject: %s", key, json.toString()));
        return false;
    }

    /**
     * Checks if the JSONObject has the object JSONArray type with the key key.
     *
     * The JSON library we use does not support a way to check if a value if of a specific type
     * in addition to it being present. Rather throwing an exception upon getting that value if it is of incorrect type.
     * This leads to using exceptions as control flow, which isn't desirable, therefore we added this method to at encapsulate
     * that controlflow.
     *
     * @param key The key of the value.
     * @param json The JSONObject to check.
     * @return True of it has a JSONArray named key, false otherwise.
     */
    public static boolean hasJSONArray(String key, JSONObject json) {
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

    /**
     * Checks if the JSONObject has the object JSONObject type with the key key.
     *
     * The JSON library we use does not support a way to check if a value if of a specific type
     * in addition to it being present. Rather throwing an exception upon getting that value if it is of incorrect type.
     * This leads to using exceptions as control flow, which isn't desirable, therefore we added this method to at encapsulate
     * that controlflow.
     *
     * @param key The key of the value.
     * @param json The JSONObject to check.
     * @return True of it has a JSONObject named key, false otherwise.
     */
    public static boolean hasJSONObject(String key, JSONObject json) {
        if (json.has(key)) {
            try {
                json.getJSONObject(key);
                return true;
            } catch(JSONException e) {
                Logger.logException(Logger.Level.DEBUG, e, "Exception was thrown in hasJSONObject");
                return false;
            }
        }
        return false;
    }

    /**
     * Checks if the JSONArray has the object JSONObject type with the at index index.
     *
     * The JSON library we use does not support a way to check if a value if of a specific type
     * in addition to it being present. Rather throwing an exception upon getting that value if it is of incorrect type.
     * This leads to using exceptions as control flow, which isn't desirable, therefore we added this method to at encapsulate
     * that controlflow.
     *
     * @param index The index of the value.
     * @param json The JSONArray to check.
     * @return True of it has a JSONObject at index index, false otherwise.
     */
    public static boolean hasJSONObject(int index, JSONArray json) {
        try {
            json.getJSONObject(index);
            return true;
        } catch (JSONException e) {
            Logger.logException(Logger.Level.DEBUG, e, "Exception was thrown in hasJSONObject");
            return false;
        }
    }

    /**
     * Function for verifying that a request is correct, by ensuring that it has all the fields indicated by the request type.
     * The logic of this is quite ugly, but I think it does work.
     */
    private static boolean verifyRequest(JSONObject json) {
        var type = RequestType.fromString(json.getString(FieldNames.TYPE));
        if (type != RequestType.CREATE_USER_REQUEST && type != RequestType.LOGIN_REQUEST && !hasString(FieldNames.AUTH_TOKEN, json)) {
            return false;
        }

        if (!hasJSONArray(FieldNames.PAYLOAD, json)) {
            Logger.log(Logger.Level.DEBUG, String.format("Object didn't contain %s", FieldNames.PAYLOAD));
            return false;
        }

        var payload = json.getJSONArray(FieldNames.PAYLOAD);
        if (payload.length() == 0) {
            Logger.log(Logger.Level.DEBUG, "Payload was 0");
            return false;
        }

        for (int i = 0; i < payload.length(); i++) {
            if (!hasJSONObject(i, payload)) {
                Logger.log(Logger.Level.DEBUG, String.format("Object didn't contain JSONObject in %s", FieldNames.PAYLOAD));
                return false;
            }

            var item = payload.getJSONObject(i); // Must validate that this is a json object
            if (!hasInt(FieldNames.ID, item)) {
                Logger.log(Logger.Level.DEBUG, String.format("JSONObject in payload doesn't have integer field: %s", FieldNames.ID));
                return false;
            } else {
                Logger.log(Logger.Level.DEBUG, String.format("ID is: %d", item.getInt(FieldNames.ID)));
            }

            for (var field : sRequestTypes) {
                if (field.messageType.contains(type)) {
                    if (field.type == FieldType.INTEGER && !hasInt(field.fieldName, item)) {
                        Logger.log(Logger.Level.DEBUG, String.format("Request was of type: %s, but did not contain integer field: %s",
                                type,
                                field.fieldName));
                        return false;
                    }
                    if (field.type == FieldType.STRING && !hasString(field.fieldName, item)) {
                        Logger.log(Logger.Level.DEBUG, String.format("Request was of type: %s, but did not contain string field: %s",
                                type,
                                field.fieldName));
                        return false;
                    }
                    if (field.type == FieldType.JSON_ARRAY && !hasJSONArray(field.fieldName, item)) {
                        Logger.log(Logger.Level.DEBUG, String.format("Request was of type: %s, but did not contain JSON_ARRAY field: %s",
                                type,
                                field.fieldName));
                        return false;
                    }
                    if (field.type == FieldType.JSON_OBJECT && !hasJSONObject(field.fieldName, item)) {
                        Logger.log(Logger.Level.DEBUG, String.format("Request was of type: %s, but did not contain JSON_OBJECT field: %s",
                                type,
                                field.fieldName));
                        return false;
                    }
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
        sRequestTypes.add(new Field<>(FieldNames.USERNAME, FieldType.STRING, new RequestType[]{
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
