package io.bidmachine.ads.networks;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import io.bidmachine.AdsFormat;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfig;

public class AmazonConfig extends NetworkConfig {

    static final String APP_KEY = "app_key";
    static final String SLOT_UUID = "slot_uuid";

    public AmazonConfig(@NonNull final String appKey) {
        this(new HashMap<String, String>() {{
            put(APP_KEY, appKey);
        }});
    }

    @SuppressWarnings("WeakerAccess")
    public AmazonConfig(@Nullable Map<String, String> networkParams) {
        super(networkParams);
    }

    @NonNull
    @Override
    protected NetworkAdapter createNetworkAdapter() {
        return new AmazonAdapter();
    }

    public AmazonConfig withMediationConfig(@NonNull AdsFormat adsFormat,
                                            @NonNull String slotUuid) {
        return withMediationConfig(adsFormat, slotUuid, null);
    }

    @SuppressWarnings("WeakerAccess")
    public AmazonConfig withMediationConfig(@NonNull AdsFormat adsFormat,
                                            @NonNull final String slotUuid,
                                            @Nullable final String appKey) {
        return withMediationConfig(adsFormat, new HashMap<String, String>() {{
            put(SLOT_UUID, slotUuid);
            if (!TextUtils.isEmpty(appKey)) {
                put(APP_KEY, appKey);
            }
        }});
    }

}
