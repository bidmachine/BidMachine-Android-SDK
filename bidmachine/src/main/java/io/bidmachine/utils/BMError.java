package io.bidmachine.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import io.bidmachine.protobuf.ErrorReason;

public class BMError {

    public static final int NOT_SET = -1;

    @VisibleForTesting
    public static final int ERROR_NOT_LOADED = 98;

    @VisibleForTesting
    public static final int ERROR_NOT_INITIALIZED = 99;

    public static final BMError NotInitialized = new BMError(ERROR_NOT_INITIALIZED,
            "SDK not initialized", "Sdk not initialized properly, see docs: https://wiki.appodeal.com/display/BID/BidMachine+Android+SDK+Documentation");
    public static final BMError Connection = new BMError(ErrorReason.ERROR_REASON_NO_CONNECTION_VALUE,
            "Connection error", "BidMachine can't connect to server");
    public static final BMError TimeoutError = new BMError(ErrorReason.ERROR_REASON_TIMEOUT_VALUE,
            "Timeout error", "BidMachine timeout reached");
    public static final BMError NoContent = new BMError(ErrorReason.ERROR_REASON_NO_CONTENT_VALUE,
            "No content", "No content");
    public static final BMError IncorrectAdUnit = new BMError(ErrorReason.ERROR_REASON_BAD_CONTENT_VALUE,
            "Incorrect ad unit", "Incorrect ad unit");
    public static final BMError Internal = new BMError(ErrorReason.ERROR_REASON_INTERNAL_VALUE,
            "Internal error", "internal error acquired");
    public static final BMError Server = new BMError(ErrorReason.ERROR_REASON_HTTP_SERVER_ERROR_VALUE,
            "Server error", "server error, please contact support");
    public static final BMError NotLoaded = new BMError(ERROR_NOT_LOADED,
            "Ads not loaded", "Ads not loaded");
    public static final BMError AlreadyShown = new BMError(-1,
            "Ads already shown", "Ads was already shown, load new one please");
    public static final BMError RequestAlreadyShown = new BMError(-1,
            "AdRequest already shown", "AdRequest that related with ad has already been shown, load new AdRequest please");
    public static final BMError RequestDestroyed = new BMError(ErrorReason.ERROR_REASON_WAS_DESTROYED_VALUE,
            "AdRequest destroyed", "AdRequest destroyed, create new one please");
    public static final BMError RequestExpired = new BMError(ErrorReason.ERROR_REASON_WAS_EXPIRED_VALUE,
            "AdRequest expired", "AdRequest expired, load new one please");
    public static final BMError Destroyed = new BMError(ErrorReason.ERROR_REASON_WAS_DESTROYED_VALUE,
            "Ads destroyed", "Ads destroyed, load new one please");
    public static final BMError Expired = new BMError(ErrorReason.ERROR_REASON_WAS_EXPIRED_VALUE,
            "Ads expired", "Ads was expired, load new one please");

    public static BMError noFillError(BMError origin) {
        if (origin != null && origin.getCode() != ErrorReason.ERROR_REASON_NO_CONTENT_VALUE) {
            return new BMError(ErrorReason.ERROR_REASON_NO_CONTENT_VALUE,
                    "No fill (" + origin.getBrief() + ")",
                    "No ads fill (" + origin.getMessage() + ")",
                    origin);
        }
        return new BMError(ErrorReason.ERROR_REASON_NO_CONTENT_VALUE,
                "No fill", "No ads fill", origin);
    }

    public static BMError paramError(String message) {
        return new BMError(ErrorReason.ERROR_REASON_HTTP_BAD_REQUEST_VALUE,
                "Param error", "Param error: " + message);
    }

    public static BMError adapterNotFoundError(String adapterName) {
        return new BMError(ErrorReason.ERROR_REASON_BAD_CONTENT_VALUE,
                "Adapter not found", "Adapter not found (" + adapterName + ")");
    }

    public static BMError requestError(String message) {
        return new BMError(ErrorReason.ERROR_REASON_HTTP_BAD_REQUEST_VALUE,
                "Request Error", "Request error (" + message + ")");
    }

    public static BMError catchError(String message) {
        return new BMError(ErrorReason.ERROR_REASON_HANDLED_EXCEPTION_VALUE,
                           "Catch error",
                           message);
    }

    private int code;
    private String brief;
    private String message;
    private boolean trackError = true;

    @Nullable
    private BMError originError;

    private BMError(int code, String brief, String message) {
        this(code, brief, message, null);
    }

    private BMError(int code, String brief, String message, @Nullable BMError originError) {
        this.code = code;
        this.brief = brief;
        this.message = message;
        this.originError = originError;
    }

    public int getCode() {
        return code;
    }

    public String getBrief() {
        return brief;
    }

    public String getMessage() {
        return message;
    }

    @Nullable
    public BMError getOriginError() {
        return originError;
    }

    public boolean isTrackError() {
        return trackError;
    }

    public void setTrackError(boolean trackError) {
        this.trackError = trackError;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BMError bmError = (BMError) o;

        if (code != bmError.code) return false;
        if (brief != null ? !brief.equals(bmError.brief) : bmError.brief != null) return false;
        return message != null ? message.equals(bmError.message) : bmError.message == null;
    }

    @Override
    public int hashCode() {
        int result = code;
        result = 31 * result + (brief != null ? brief.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "(" + code + ") " + brief + " - " + message;
    }
}
