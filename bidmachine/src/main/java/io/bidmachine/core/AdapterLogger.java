package io.bidmachine.core;

public class AdapterLogger {

    public static void log(String tag, String message) {
        Logger.log(tag, message);
    }

    public static void log(Throwable t) {
        Logger.log(t);
    }

}