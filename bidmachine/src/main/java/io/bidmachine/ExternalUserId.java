package io.bidmachine;

import androidx.annotation.NonNull;

public class ExternalUserId {

    private final String sourceId;
    private final String value;

    /**
     * Initializing a new object.
     *
     * @param sourceId Third party service identifier.
     * @param value    The value of a third party service.
     */
    public ExternalUserId(@NonNull String sourceId, @NonNull String value) {
        this.sourceId = sourceId;
        this.value = value;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getValue() {
        return value;
    }

}