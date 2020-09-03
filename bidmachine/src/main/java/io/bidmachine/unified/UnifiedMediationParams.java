package io.bidmachine.unified;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

import io.bidmachine.core.Logger;

public abstract class UnifiedMediationParams {

    @Nullable
    public String getString(@Nullable String key) {
        return getString(key, null);
    }

    @Nullable
    public abstract String getString(@Nullable String key, String fallback);

    public int getInt(@Nullable String key) {
        return getInt(key, 0);
    }

    public abstract int getInt(@Nullable String key, int fallback);

    @Nullable
    public Integer getInteger(@Nullable String key) {
        return getInteger(key, null);
    }

    @Nullable
    public abstract Integer getInteger(@Nullable String key, @Nullable Integer fallback);

    public boolean getBool(@Nullable String key) {
        return getBool(key, false);
    }

    public abstract boolean getBool(@Nullable String key, boolean fallback);

    public double getDouble(@Nullable String key) {
        return getDouble(key, 0);
    }

    public abstract double getDouble(@Nullable String key, double fallback);

    public float getFloat(@Nullable String key) {
        return getFloat(key, 0);
    }

    public abstract float getFloat(@Nullable String key, float fallback);

    public abstract boolean contains(@Nullable String key);

    public static class MappedUnifiedMediationParams extends UnifiedMediationParams {

        public interface DataProvider {
            @NonNull
            Map<String, Object> getData();
        }

        @NonNull
        private DataProvider dataProvider;

        public MappedUnifiedMediationParams(@NonNull DataProvider dataProvider) {
            this.dataProvider = dataProvider;
        }

        @Nullable
        @Override
        public String getString(@Nullable String key, String fallback) {
            return resolve(key, fallback);
        }

        @Override
        public int getInt(@Nullable String key, int fallback) {
            return resolve(key, fallback);
        }

        @Override
        public Integer getInteger(@Nullable String key, @Nullable Integer fallback) {
            return resolve(key, fallback);
        }

        @Override
        public boolean getBool(@Nullable String key, boolean fallback) {
            return resolve(key, fallback);
        }

        @Override
        public double getDouble(@Nullable String key, double fallback) {
            return resolve(key, fallback);
        }

        @Override
        public float getFloat(@Nullable String key, float fallback) {
            return resolve(key, fallback);
        }

        @Override
        public boolean contains(@Nullable String key) {
            return dataProvider.getData().containsKey(key);
        }

        @SuppressWarnings("unchecked")
        private <T> T resolve(@Nullable String key, Object fallback) {
            if (key != null) {
                Object value = dataProvider.getData().get(key);
                if (value != null) {
                    try {
                        return (T) value;
                    } catch (Exception e) {
                        Logger.log(e);
                    }
                }
            }
            return (T) fallback;
        }
    }

}
