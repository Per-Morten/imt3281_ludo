package no.ntnu.imt3281.ludo.api;

/**
 * ResponseType enum contaning the different ResponseTypes that the API can receive.
 */
public enum ResponseType {
    CREATE_CHAT_RESPONSE,
    CREATE_GAME_RESPONSE,
    CREATE_USER_RESPONSE,
    DECLINE_GAME_INVITE_RESPONSE,
    DELETE_USER_RESPONSE,
    FRIEND_RESPONSE,
    GET_CHAT_RANGE_RESPONSE,
    GET_CHAT_RESPONSE,
    GET_FRIEND_RANGE_RESPONSE,
    GET_GAME_RANGE_RESPONSE,
    GET_GAME_RESPONSE,
    GET_GAME_STATE_RESPONSE,
    GET_USER_RANGE_RESPONSE,
    GET_USER_RESPONSE,
    IGNORE_RESPONSE,
    JOIN_CHAT_RESPONSE,
    JOIN_GAME_RESPONSE,
    JOIN_RANDOM_GAME_RESPONSE,
    LEAVE_CHAT_RESPONSE,
    LEAVE_GAME_RESPONSE,
    LOGIN_RESPONSE,
    LOGOUT_RESPONSE,
    MOVE_PIECE_RESPONSE,
    ROLL_DICE_RESPONSE,
    SEND_CHAT_INVITE_RESPONSE,
    SEND_CHAT_MESSAGE_RESPONSE,
    SEND_GAME_INVITE_RESPONSE,
    START_GAME_RESPONSE,
    UNFRIEND_RESPONSE,
    UPDATE_USER_RESPONSE,
    ;

    /**
     * Creates the response from a given string value, returning null if the value is illegal.
     * This is because getting an invalid value from the network is NOT an exceptional situation,
     * and exceptions shouldn't be used for standard controlflow.
     * Additionally, this function is case_insensitive,
     * as we might want to use it with both uppercase and lowercase values.
     *
     * @param value The string value to turn into an RequestType
     * @return The Corresponding Request of the string, null if it doesn't correspond to anything.
     */
    public static ResponseType fromString(String value) {
        var val = value.toUpperCase();
        try {
            return ResponseType.valueOf(val);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Transforms the enum value to a lowercase representation of its value,
     * as the types in the API are lowercase.
     *
     * @return The ResponseType turned into lowercase.
     */
    public String toLowerCaseString() {
        return this.toString().toLowerCase();
    }

    /**
     * Transforms the value parameter to a lowercase representation of its value,
     * as the types in the API are lowercase.
     *
     * @param value The ResponseType to turn into lowercase.
     * @return The ResponseType turned into lowercase.
     */
    @Deprecated
    public static String toLowerCaseString(ResponseType value) {
        return value.toString().toLowerCase();
    }

    /**
     * Gets the corresponding ResponseType of a Request,
     * For Example supplying a GET_USER_REQUEST will return GET_USER_RESPONSE.
     *
     * @param value The Request to get the corresponding Response for.
     * @return The corresponding response of the request
     */
    public static ResponseType getCorrespondingResponse(RequestType value) {
        return fromString(value.toString().replace("REQUEST", "RESPONSE"));
    }
}
