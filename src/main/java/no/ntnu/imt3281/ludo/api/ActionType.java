package no.ntnu.imt3281.ludo.api;

public enum ActionType {
    THROW_DICE,
    MOVE_PIECE,
    ;

    /**
     * Utility function for going to integer, which we considered more readable than ordinal.
     * @return this.ordinal();
     */
    public int toInt() {
        return this.ordinal();
    }

    /**
     * Creates the friend from a given numerical value, returning null if the value is illegal.
     * This is because getting a numerical value out of bounds from the network is NOT an exceptional situation,
     * and exceptions shouldn't be used for standard controlflow.
     *
     * @param i The value to turn into the friend status.
     * @return The friend status corresponding to the integer value, null if it is not within the valid range.
     */
    public static ActionType fromInt(int i) {
        var values = ActionType.values();
        if (i < 0 || i >= values.length) {
            return null;
        }
        return values[i];
    }
}
