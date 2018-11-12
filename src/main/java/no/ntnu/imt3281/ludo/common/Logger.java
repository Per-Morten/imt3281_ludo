package no.ntnu.imt3281.ludo.common;

/**
 * Class used for logging, as we didn't really like a lot of the other logger
 * frameworks. The logger is single instance, so no need create it on a pr
 * object basis.
 */
public class Logger {
    /**
     * The different levels that the Logger operates on. They have the following
     * meaning:
     *
     * DEBUG: Used for debug messages, like entering a function. Ideally these
     * should be removed before commit to avoid creating noisy logs.
     *
     * INFO: General use logging, like starting and stopping sub systems.
     *
     * WARN: For recoverable errors, like suppressed exceptions or the like.
     *
     * ERROR: For non recoverable errors. NOTE: Logging with Level.ERROR will also
     * terminate the application.
     */
    public enum Level {
    DEBUG, INFO, WARN, ERROR,
    }

    /**
     * Sets the loggingLevel to the desired value. No messages that has a lower
     * level than the specified logging level will be logged. Highest logLevel
     * possible to set is ERROR, which you cannot turn off.
     *
     * @param level The desired log level.
     */
    public static void setLogLevel(Level level) {
        sLogLevel = level;
    }

    /**
     * Prints the desired message to a loggable stream based on the specified level.
     * This function will not print anything of level is less than the logLevel that
     * has been set by users. A log level of DEBUG or INFO will log to system.out,
     * while a log level of WARN og ERROR will log to system.err
     *
     * Note: When calling this function with level.ERROR the application will
     * terminate.
     *
     * @param level  The desired level to log on.
     * @param string The string that should be logged.
     */
    public static void log(Level level, String string) {
        if (level.ordinal() < sLogLevel.ordinal()) {
            return;
        }

        var out = (level == Level.ERROR || level == Level.WARN) ? System.err : System.out;
        out.println(String.format("%s: %s%n", level.toString(), string));
        out.flush();

        if (level == Level.ERROR) {
            System.exit(0);
        }
    }

    public static void logException(Level level, Exception exception) {
        logException(level, exception, "");
    }

    public static void logException(Level level, Exception exception, String string) {
        if (level.ordinal() < sLogLevel.ordinal()) {
            return;
        }

        var out = (level == Level.ERROR || level == Level.WARN) ? System.err : System.out;
        out.println(String.format("%s: %s, Exception Encountered: %s, trace:%n", level.toString(), string, exception.getClass()));
        exception.printStackTrace(out);
        out.flush();

        if (level == Level.ERROR) {
            System.exit(0);
        }
    }

    private static Level sLogLevel = Level.INFO;
}
