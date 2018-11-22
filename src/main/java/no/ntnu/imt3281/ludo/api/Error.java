package no.ntnu.imt3281.ludo.api;

/**
 * Error enum contaning the different errors that the API can return.
 */
public enum Error {
    UNAUTHORIZED,
    NOT_UNIQUE_EMAIL,
    NOT_UNIQUE_USERNAME,
    INVALID_USERNAME_OR_PASSWORD,
    USER_ID_NOT_FOUND,
    OTHER_ID_NOT_FOUND,
    USER_AND_OTHER_ID_IS_SAME,
    USER_IS_FRIEND,
    CANNOT_LEAVE_GLOBAL_CHAT,
    CHAT_ID_NOT_FOUND,
    USER_IS_NOT_FRIEND,
    GAME_ID_NOT_FOUND,
    USER_IS_NOT_OWNER,
    GAME_ALREADY_STARTED,
    USER_NOT_IN_GAME,
    USER_ALREADY_INVITED_OR_IN_GAME,
    OUT_OF_TURN,
    NOT_TIME_TO_THROW_DICE,
    GAME_NOT_IN_SESSION,
    NOT_TIME_TO_MOVE_PIECE,
    PIECE_INDEX_OUT_OF_BOUNDS,
    GAME_IS_FULL,
    NOT_INVITED_TO_GAME;



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
