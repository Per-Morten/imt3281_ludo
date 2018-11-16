package no.ntnu.imt3281.ludo.api;

public enum RequestType {
    LOGIN_REQUEST,
    LOGOUT_REQUEST,
    GET_USER_REQUEST,
    GET_USER_RANGE_REQUEST,
    CREATE_USER_REQUEST,
    UPDATE_USER_REQUEST,
    DELETE_USER_REQUEST,
    GET_FRIEND_REQUEST,
    GET_FRIEND_RANGE_REQUEST,
    FRIEND_REQUEST,
    UNFRIEND_REQUEST,
    JOIN_CHAT_REQUEST,
    LEAVE_CHAT_REQUEST,
    GET_CHAT_REQUEST,
    GET_CHAT_RANGE_REQUEST,
    CREATE_CHAT_REQUEST,
    SEND_CHAT_MESSAGE_REQUEST,
    SEND_CHAT_INVITE_REQUEST,
    CREATE_GAME_REQUEST,
    JOIN_GAME_REQUEST,
    LEAVE_GAME_REQUEST,
    SEND_GAME_INVITE_REQUEST,
    DECLINE_GAME_INVITE_REQUEST,
    START_GAME_REQUEST,
    GET_GAME_REQUEST,
    GET_GAME_STATE_REQUEST,
    ROLL_DICE_REQUEST,
    MOVE_PIECE_REQUEST,
    GET_GAME_RANGE_REQUEST,
    ;

    public static RequestType fromString(String value) {
        var val = value.toUpperCase();
        try {
            return RequestType.valueOf(val);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String toLowerCaseString() {
        return this.toString().toLowerCase();
    }

    @Deprecated
    public static String toLowerCaseString(RequestType value) {
        return value.toString().toLowerCase();
    }
}
