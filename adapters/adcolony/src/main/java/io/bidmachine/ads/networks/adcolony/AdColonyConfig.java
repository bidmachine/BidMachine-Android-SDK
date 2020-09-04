package io.bidmachine.ads.networks.adcolony;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import io.bidmachine.AdsFormat;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfig;

public class AdColonyConfig extends NetworkConfig {

    static final String KEY_APP_ID = "app_id";
    static final String KEY_ZONE_ID = "zone_id";
    static final String KEY_STORE_ID = "store_id";

    private static final String DEFAULT_STORE_ID = "google";

    public AdColonyConfig(@NonNull String appId) {
        this(appId, DEFAULT_STORE_ID);
    }

    @SuppressWarnings("WeakerAccess")
    public AdColonyConfig(@NonNull final String appId, @NonNull final String storeId) {
        super(new HashMap<String, String>() {{
            put(KEY_APP_ID, appId);
            put(KEY_STORE_ID, storeId);
        }});
    }

    @SuppressWarnings("unused")
    public AdColonyConfig(@Nullable Map<String, String> networkParams) {
        super(networkParams);
    }

    @NonNull
    @Override
    protected NetworkAdapter createNetworkAdapter() {
        return new AdColonyAdapter();
    }

    public AdColonyConfig withMediationConfig(@NonNull AdsFormat adsFormat,
                                              @NonNull String zoneId) {
        return withMediationConfig(adsFormat, zoneId, null);
    }

    @SuppressWarnings("WeakerAccess")
    public AdColonyConfig withMediationConfig(@NonNull AdsFormat adsFormat,
                                              @NonNull String zoneId,
                                              @Nullable String appId) {
        return withMediationConfig(adsFormat, zoneId, appId, null);
    }

    @SuppressWarnings("WeakerAccess")
    public AdColonyConfig withMediationConfig(@NonNull AdsFormat adsFormat,
                                              @NonNull final String zoneId,
                                              @Nullable final String appId,
                                              @Nullable final String storeId) {
        return withMediationConfig(adsFormat, new HashMap<String, String>() {{
            put(KEY_ZONE_ID, zoneId);
            if (!TextUtils.isEmpty(appId)) {
                put(KEY_APP_ID, appId);
            }
            if (!TextUtils.isEmpty(storeId)) {
                put(KEY_STORE_ID, storeId);
            }
        }});
    }
}
