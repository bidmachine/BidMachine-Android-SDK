package io.bidmachine;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import io.bidmachine.models.IExtraParams;
import io.bidmachine.models.RequestParams;

public class ExtraParams extends RequestParams<ExtraParams> implements IExtraParams<ExtraParams> {

    private Map<String, String> extrasMap;

    Map<String, String> getExtrasMap() {
        return extrasMap;
    }

    @Override
    public ExtraParams addExtra(String key, String value) {
        if (extrasMap == null) {
            extrasMap = new HashMap<>();
        }
        extrasMap.put(key, value);
        return this;
    }

    @Override
    public void merge(@NonNull ExtraParams instance) {
        if (instance.extrasMap != null) {
            if (extrasMap == null) {
                extrasMap = new HashMap<>(instance.extrasMap);
            } else {
                extrasMap.putAll(instance.extrasMap);
            }
        }
    }
}
