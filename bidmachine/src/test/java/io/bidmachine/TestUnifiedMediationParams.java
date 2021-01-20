package io.bidmachine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import io.bidmachine.unified.UnifiedMediationParams;

public class TestUnifiedMediationParams extends UnifiedMediationParams {

    private final Map<String, Object> map = new HashMap<>();
    private final MappedUnifiedMediationParams.DataProvider dataProvider =
            new MappedUnifiedMediationParams.DataProvider() {
                @NonNull
                @Override
                public Map<String, Object> getData() {
                    return map;
                }
            };
    private final UnifiedMediationParams.MappedUnifiedMediationParams mappedUnifiedMediationParams =
            new MappedUnifiedMediationParams(dataProvider);

    public void put(@NonNull String key, @NonNull Object value) {
        map.put(key, value);
    }

    public void clear() {
        map.clear();
    }

    @Nullable
    @Override
    public String getString(@Nullable String key, String fallback) {
        return mappedUnifiedMediationParams.getString(key, fallback);
    }

    @Override
    public int getInt(@Nullable String key, int fallback) {
        return mappedUnifiedMediationParams.getInt(key, fallback);
    }

    @Nullable
    @Override
    public Integer getInteger(@Nullable String key, @Nullable Integer fallback) {
        return mappedUnifiedMediationParams.getInteger(key, fallback);
    }

    @Override
    public boolean getBool(@Nullable String key, boolean fallback) {
        return mappedUnifiedMediationParams.getBool(key, fallback);
    }

    @Override
    public double getDouble(@Nullable String key, double fallback) {
        return mappedUnifiedMediationParams.getDouble(key, fallback);
    }

    @Override
    public float getFloat(@Nullable String key, float fallback) {
        return mappedUnifiedMediationParams.getFloat(key, fallback);
    }

    @Override
    public <T> T getObject(@Nullable String key, T fallback) {
        return mappedUnifiedMediationParams.getObject(key, fallback);
    }

    @Override
    public boolean contains(@Nullable String key) {
        return mappedUnifiedMediationParams.contains(key);
    }

}