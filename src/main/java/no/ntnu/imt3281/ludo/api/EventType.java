package no.ntnu.imt3281.ludo.api;

/**
 * Enum containing all the different event types that are supported in the API
 */
public enum EventType {
    FRIEND_UPDATE,
    CHAT_UPDATE,
    CHAT_INVITE,
    CHAT_MESSAGE,
    GAME_UPDATE,
    GAME_INVITE,
    FORCE_LOGOUT,
    GAME_STATE_UPDATE;

    /**
     * Creates the event from a given string value, returning null if the value is illegal.
     * This is because getting an invalid value from the network is NOT an exceptional situation,
     * and exceptions shouldn't be used for standard controlflow.
     * Additionally, this function is case_insensitive,
     * as we might want to use it with both uppercase and lowercase values.
     *
     * @param value The string value to turn into an EventType
     * @return The Corresponding EventType of the string, null if it doesn't correspond to anything.
     */
    public static EventType fromString(String value) {
        var val = value.toUpperCase();
        try {
            return EventType.valueOf(val);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Transforms the value parameter to a lowercase representation of its value,
     * as the types in the API are lowercase.
     *
     * @param value The EventType to turn into lowercase.
     * @return The EventType turned into lowercase.
     */
    @Deprecated
    public static String toLowerCaseString(EventType value) {
        return value.toString().toLowerCase();
    }

    /**
     * Transforms the enum value to a lowercase representation of its value,
     * as the types in the API are lowercase.
     *
     * @return The EventType turned into lowercase.
     */
    public String toLowerCaseString() {
        return this.toString().toLowerCase();
    }
}
