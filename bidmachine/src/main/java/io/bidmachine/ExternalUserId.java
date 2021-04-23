package io.bidmachine;

public class ExternalUserId {

    private final String sourceId;
    private final String value;

    public ExternalUserId(String sourceId, String value) {
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