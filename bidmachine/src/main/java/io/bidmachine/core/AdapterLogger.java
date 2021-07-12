package io.bidmachine.core;

/**
 * Adapter helper providing methods for logging.
 */
public class AdapterLogger {

    /**
     * Send general message to log.
     *
     * @param tag     Source identifier of the log message.
     * @param message The message to be logged.
     */
    public static void logMessage(String tag, String message) {
        Logger.log(tag, message);
    }

    /**
     * Send error message to log.
     *
     * @param tag     Source identifier of the log message.
     * @param message The message to be logged.
     */
    public static void logError(String tag, String message) {
        Logger.logError(tag, message);
    }

    /**
     * Send exception to log.
     *
     * @param t The exception to be logged.
     */
    public static void logThrowable(Throwable t) {
        Logger.log(t);
    }

}