package no.ntnu.imt3281.ludo.api;

public enum EventType {
    FRIEND_UPDATE,
    CHAT_UPDATE,
    CHAT_INVITE,
    CHAT_MESSAGE,
    GAME_UPDATE,
    GAME_INVITE,
    FORCE_LOGOUT,
    ;

    public static EventType fromString(String value) {
        var val = value.toUpperCase();
        try {
            return EventType.valueOf(val);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String toLowerCaseString(EventType value) {
        return value.toString().toLowerCase();
    }
}
