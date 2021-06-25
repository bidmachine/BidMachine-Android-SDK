package io.bidmachine.core;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.Map;

public class Logger {

    private static final String TAG = "BidMachineLog";

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

    public static void log(String subTag, String message) {
        log(String.format("[%s] %s", subTag, message));
    }

    public static void log(String message) {
        if (isLoggingEnabled) {
            int size = 1000;
            if (message.length() > size) {
                int length = (message.length() + size - 1) / size;
                for (int i = 0, pos = 0; i < length; i++, pos += size) {
                    sendLog(message.substring(pos, Math.min(message.length(), pos + size)));
                }
            } else {
                sendLog(message);
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
            sendLog(builder.toString());
        }
    }

    private static void sendLog(String message) {
        Log.d(TAG, messageBuilder.buildMessage(message));
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