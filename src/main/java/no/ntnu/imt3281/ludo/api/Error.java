package no.ntnu.imt3281.ludo.api;

/**
 * Error enum contaning the different errors that the API can return.
 */
public enum Error {
    UNAUTHORIZED,                    // 0
    NOT_UNIQUE_EMAIL,                // 1
    NOT_UNIQUE_USERNAME,             // 2
    INVALID_USERNAME_OR_PASSWORD,    // 3
    USER_ID_NOT_FOUND,               // 4
    OTHER_ID_NOT_FOUND,              // 5
    USER_AND_OTHER_ID_IS_SAME,       // 6
    USER_IS_FRIEND,                  // 7
    CANNOT_LEAVE_GLOBAL_CHAT,        // 8
    CHAT_ID_NOT_FOUND,               // 9
    USER_IS_NOT_FRIEND,              // 10
    GAME_ID_NOT_FOUND,               // 11
    USER_IS_NOT_OWNER,               // 12
    GAME_ALREADY_STARTED,            // 13
    USER_NOT_IN_GAME,                // 14
    USER_ALREADY_INVITED_OR_IN_GAME, // 15
    OUT_OF_TURN,                     // 16
    NOT_TIME_TO_THROW_DICE,          // 17
    GAME_NOT_IN_SESSION,             // 18
    NOT_TIME_TO_MOVE_PIECE,          // 19
    PIECE_INDEX_OUT_OF_BOUNDS,       // 20
    GAME_IS_FULL,                    // 21
    NOT_INVITED_TO_GAME,             // 22
    MALFORMED_EMAIL,                 // 23
    MALFORMED_USERNAME,              // 24
    MALFORMED_AVATAR_URI,            // 25
    NOT_ENOUGH_PLAYERS,              // 26
    ;

    /**
     * Utility function for going to integer, which we considered more readable than ordinal.
     * @return this.ordinal();
     */
    public int toInt() {
        return this.ordinal();
    }

    /**
     * Creates the error from a given numerical value, returning null if the value is illegal.
     * This is because getting a numerical value out of bounds from the network is NOT an exceptional situation,
     * and exceptions shouldn't be used for standard controlflow.
     *
     * @param i The value to turn into the error.
     * @return The error corresponding to the integer value, null if it is not within the valid range.
     */
    public static Error fromInt(int i) {
        var values = Error.values();
        if (i < 0 || i >= values.length) {
            return null;
        }
        return values[i];
    }
}
