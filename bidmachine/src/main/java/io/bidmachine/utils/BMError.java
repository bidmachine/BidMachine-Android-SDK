package io.bidmachine.utils;

import androidx.annotation.NonNull;

import io.bidmachine.protobuf.ErrorReason;

public class BMError {

    public static final int NOT_SET = -1;
    public static final int NO_CONNECTION = ErrorReason.ERROR_REASON_NO_CONNECTION_VALUE;
    public static final int TIMEOUT = ErrorReason.ERROR_REASON_TIMEOUT_VALUE;
    public static final int HTTP_BAD_REQUEST = ErrorReason.ERROR_REASON_HTTP_BAD_REQUEST_VALUE;
    public static final int SERVER = ErrorReason.ERROR_REASON_HTTP_SERVER_ERROR_VALUE;
    public static final int NO_CONTENT = ErrorReason.ERROR_REASON_NO_CONTENT_VALUE;
    public static final int BAD_CONTENT = ErrorReason.ERROR_REASON_BAD_CONTENT_VALUE;
    public static final int HB_NETWORK = ErrorReason.ERROR_REASON_HB_NETWORK_VALUE;
    public static final int EXPIRED = ErrorReason.ERROR_REASON_WAS_EXPIRED_VALUE;
    public static final int DESTROYED = ErrorReason.ERROR_REASON_WAS_DESTROYED_VALUE;
    public static final int INTERNAL = ErrorReason.ERROR_REASON_INTERNAL_VALUE;


    public static final BMError NoConnection =
            new BMError(NO_CONNECTION,
                        "Can't connect to server");

    public static final BMError TimeoutError =
            new BMError(TIMEOUT,
                        "Timeout reached");

    public static final BMError Request =
            new BMError(HTTP_BAD_REQUEST,
                        "Request contains bad syntax or cannot be fulfilled");

    public static final BMError Server =
            new BMError(SERVER,
                        "Server failed to fulfil an apparently valid request");

    public static final BMError RequestAlreadyShown =
            new BMError(NOT_SET,
                        "AdRequest that related with ad has already been shown, load new AdRequest please");

    public static final BMError RequestExpired =
            new BMError(EXPIRED,
                        "AdRequest expired, load new one please");

    public static final BMError RequestDestroyed =
            new BMError(DESTROYED,
                        "AdRequest destroyed, create new one please");

    public static final BMError AlreadyShown =
            new BMError(NOT_SET,
                        "Ads was already shown, load new one please");

    public static final BMError Expired =
            new BMError(EXPIRED,
                        "Ads was expired, load new one please");

    public static final BMError Destroyed =
            new BMError(DESTROYED,
                        "Ads destroyed, load new one please");


    public static BMError noFill() {
        return new BMError(NO_CONTENT, "No ads fill");
    }

    public static BMError notFound(@NonNull String name) {
        return new BMError(NO_CONTENT,
                           String.format("%s not found", name));
    }

    public static BMError incorrectContent(@NonNull String message) {
        return new BMError(BAD_CONTENT, message);
    }


    public static BMError adapter(@NonNull String message) {
        return new BMError(HB_NETWORK, message);
    }

    public static BMError adapterNotInitialized() {
        return adapter("Adapter SDK not initialized");
    }

    public static BMError adapterInitialization() {
        return adapter("Adapter SDK initialization error");
    }

    public static BMError adapterGetsParameter(@NonNull String parameterName) {
        return adapter(String.format("%s not found", parameterName));
    }


    public static BMError internal(@NonNull String message) {
        return new BMError(INTERNAL, message);
    }


    private final int code;
    private final String message;

    private boolean trackError = true;

    private BMError(int code, @NonNull String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public boolean isTrackError() {
        return trackError;
    }

    public void setTrackError(boolean trackError) {
        this.trackError = trackError;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BMError bmError = (BMError) o;
        if (code != bmError.code) {
            return false;
        }
        return message.equals(bmError.message);
    }

    @Override
    public int hashCode() {
        int result = code;
        result = 31 * result + message.hashCode();
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("(%s) %s", code, message);
    }

}