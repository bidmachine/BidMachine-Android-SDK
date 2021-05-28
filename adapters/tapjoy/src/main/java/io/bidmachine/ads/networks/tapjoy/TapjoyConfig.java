package io.bidmachine.ads.networks.tapjoy;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import io.bidmachine.AdsFormat;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfig;

public class TapjoyConfig extends NetworkConfig {

    static final String KEY_SDK = "sdk_key";
    static final String KEY_PLACEMENT_NAME = "placement_name";
    static final String KEY_TOKEN = "token";

    public TapjoyConfig(@NonNull final String sdkKey) {
        super(new HashMap<String, String>() {{
            put(KEY_SDK, sdkKey);
        }});
    }

    @SuppressWarnings("unused")
    public TapjoyConfig(@Nullable Map<String, String> networkConfig) {
        super(networkConfig);
    }

    @NonNull
    @Override
    protected NetworkAdapter createNetworkAdapter() {
        return new TapjoyAdapter();
    }

    public TapjoyConfig withMediationConfig(@NonNull AdsFormat format,
                                            @NonNull String placementName) {
        return withMediationConfig(format, placementName, null);
    }

    @SuppressWarnings("WeakerAccess")
    public TapjoyConfig withMediationConfig(@NonNull AdsFormat format,
                                            @NonNull final String placementName,
                                            @Nullable final String sdkKey) {
        return withMediationConfig(format, new HashMap<String, String>() {{
            put(KEY_PLACEMENT_NAME, placementName);
            if (!TextUtils.isEmpty(sdkKey)) {
                put(KEY_SDK, sdkKey);
            }
        }});
    }

}