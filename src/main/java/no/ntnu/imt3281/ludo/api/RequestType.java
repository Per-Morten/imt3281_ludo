package no.ntnu.imt3281.ludo.api;

/**
 * RequestType enum contaning the different RequestsTypes that the API can receive.
 */
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


    /**
     * Creates the request from a given string value, returning null if the value is illegal.
     * This is because getting an invalid value from the network is NOT an exceptional situation,
     * and exceptions shouldn't be used for standard controlflow.
     * Additionally, this function is case_insensitive,
     * as we might want to use it with both uppercase and lowercase values.
     *
     * @param value The string value to turn into an RequestType
     * @return The Corresponding Request of the string, null if it doesn't correspond to anything.
     */
    public static RequestType fromString(String value) {
        var val = value.toUpperCase();
        try {
            return RequestType.valueOf(val);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Transforms the enum value to a lowercase representation of its value,
     * as the types in the API are lowercase.
     *
     * @return The RequestType turned into lowercase.
     */
    public String toLowerCaseString() {
        return this.toString().toLowerCase();
    }

    /**
     * Transforms the value parameter to a lowercase representation of its value,
     * as the types in the API are lowercase.
     *
     * @param value The RequestType to turn into lowercase.
     * @return The RequestType turned into lowercase.
     */
    @Deprecated
    public static String toLowerCaseString(RequestType value) {
        return value.toString().toLowerCase();
    }
}
