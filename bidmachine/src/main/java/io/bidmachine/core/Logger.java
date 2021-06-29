package io.bidmachine.core;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.Map;

public class Logger {

    private static final String TAG = "BidMachineLog";
    private static final int MAX_CHAR_COUNT = 1000;

    private static boolean isLoggingEnabled = false;

    @NonNull
    private static LoggerMessageBuilder messageBuilder = new RegularMessageBuilder();

    public static void setLoggingEnabled(boolean enabled) {
        isLoggingEnabled = enabled;
    }

    public static boolean isLoggingEnabled() {
        return isLoggingEnabled;
    }

    public static void setMessageBuilder(@NonNull LoggerMessageBuilder messageBuilder) {
        Logger.messageBuilder = messageBuilder;
    }

    public static void log(Throwable t) {
        if (isLoggingEnabled) {
            sendWarning(t);
        }
    }

    public static void logError(String subTag, String message) {
        logError(String.format("[%s] %s", subTag, message));
    }

    public static void logError(String message) {
        log(message, true);
    }

    public static void log(String subTag, String message) {
        log(String.format("[%s] %s", subTag, message));
    }

    public static void log(String message) {
        log(message, false);
    }

    public static void log(String message, boolean isError) {
        if (isLoggingEnabled) {
            if (message.length() > MAX_CHAR_COUNT) {
                int length = (message.length() + MAX_CHAR_COUNT - 1) / MAX_CHAR_COUNT;
                for (int i = 0, pos = 0; i < length; i++, pos += MAX_CHAR_COUNT) {
                    sendLog(message.substring(pos,
                                              Math.min(message.length(), pos + MAX_CHAR_COUNT)),
                            isError);
                }
            } else {
                sendLog(message, isError);
            }
        }
    }

    public static void log(String key, Map<?, ?> map) {
        if (isLoggingEnabled) {
            StringBuilder builder = new StringBuilder();
            if (map == null || map.isEmpty()) {
                builder.append("Empty");
            } else {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (builder.length() > 0) {
                        builder.append("\n");
                    }
                    String valueString = null;
                    if (entry.getValue() instanceof Collection) {
                        Collection items = (Collection) entry.getValue();
                        StringBuilder valueStringBuilder = new StringBuilder();
                        for (Object object : items) {
                            if (object != null) {
                                if (valueStringBuilder.length() > 0) {
                                    valueStringBuilder.append(",");
                                }
                                valueStringBuilder.append(object.toString());
                            }
                        }
                        if (valueStringBuilder.length() == 0) {
                            valueStringBuilder.append("Empty");
                        }
                        valueString = valueStringBuilder.toString();
                    } else if (entry.getValue() != null) {
                        valueString = entry.getValue().toString();
                    }
                    builder.append(entry.getKey()).append(": ").append(valueString);
                }
            }
            builder.insert(0, "\n").insert(0, key);
            sendLog(builder.toString(), false);
        }
    }

    private static void sendLog(String message, boolean isError) {
        String buildMessage = messageBuilder.buildMessage(message);
        if (isError) {
            Log.e(TAG, buildMessage);
        } else {
            Log.d(TAG, buildMessage);
        }
    }

    private static void sendWarning(Throwable throwable) {
        Log.w(TAG, throwable);
    }


    public interface LoggerMessageBuilder {
        String buildMessage(String origin);
    }

    private static final class RegularMessageBuilder implements LoggerMessageBuilder {

        @Override
        public String buildMessage(String origin) {
            return origin;
        }

    }

}