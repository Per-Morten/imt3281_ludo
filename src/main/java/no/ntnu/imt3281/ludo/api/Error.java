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
