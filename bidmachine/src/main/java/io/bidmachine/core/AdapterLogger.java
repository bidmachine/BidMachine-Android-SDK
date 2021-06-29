package io.bidmachine.core;

public class AdapterLogger {

    public static void logMessage(String tag, String message) {
        Logger.log(tag, message);
    }

    public static void logError(String tag, String message) {
        Logger.logError(tag, message);
    }

    public static void logThrowable(Throwable t) {
        Logger.log(t);
    }

}