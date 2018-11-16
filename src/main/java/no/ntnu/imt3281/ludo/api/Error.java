package no.ntnu.imt3281.ludo.api;

// This shouldn't be enums, but rather numerical constants.
public enum Error {
    UNAUTHORIZED,
    NOT_UNIQUE_EMAIL,
    NOT_UNIQUE_USERNAME,
    INVALID_USERNAME_OR_PASSWORD,
    USER_ID_NOT_FOUND,
    ;

    public int toInt() {
        return this.ordinal();
    }

    public static Error fromInt(int i) {
        var values = Error.values();
        if (i < 0 || i >= values.length) {
            return null;
        }
        return values[i];
    }
}
