package no.ntnu.imt3281.ludo.api;

public enum ResponseType {
    LOGIN_RESPONSE,
    LOGOUT_RESPONSE,
    GET_USER_RESPONSE,
    GET_USER_RANGE_RESPONSE,
    CREATE_USER_RESPONSE,
    UPDATE_USER_RESPONSE,
    DELETE_USER_RESPONSE,
    GET_FRIEND_RESPONSE,
    GET_FRIEND_RANGE_RESPONSE,
    FRIEND_RESPONSE,
    UNFRIEND_RESPONSE,
    JOIN_CHAT_RESPONSE,
    LEAVE_CHAT_RESPONSE,
    GET_CHAT_RESPONSE,
    GET_CHAT_RANGE_RESPONSE,
    CREATE_CHAT_RESPONSE,
    SEND_CHAT_MESSAGE_RESPONSE,
    SEND_CHAT_INVITE_RESPONSE,
    CREATE_GAME_RESPONSE,
    JOIN_GAME_RESPONSE,
    LEAVE_GAME_RESPONSE,
    SEND_GAME_INVITE_RESPONSE,
    DECLINE_GAME_INVITE_RESPONSE,
    START_GAME_RESPONSE,
    GET_GAME_RESPONSE,
    GET_GAME_STATE_RESPONSE,
    SEND_ROLL_DICE_RESPONSE,
    MOVE_PIECE_RESPONSE,
    ;

    public static ResponseType fromString(String value) {
        var val = value.toUpperCase();
        try {
            return ResponseType.valueOf(val);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String toLowerCaseString() {
        return this.toString().toLowerCase();
    }

    @Deprecated
    public static String toLowerCaseString(ResponseType value) {
        return value.toString().toLowerCase();
    }

    public static ResponseType getCorrespondingResponse(RequestType value) {
        return fromString(value.toString().replace("REQUEST", "RESPONSE"));
    }
}
