package io.bidmachine.ads.networks.mintegral;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import io.bidmachine.AdsFormat;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfig;

public class MintegralConfig extends NetworkConfig {

    static final String KEY_APP_ID = "app_id";
    static final String KEY_API_KEY = "api_key";
    static final String KEY_UNIT_ID = "unit_id";
    static final String KEY_REWARD_ID = "reward_id";
    static final String KEY_BUYER_UID = "buyeruid";
    static final String KEY_BID_TOKEN = "bid_token";

    public MintegralConfig(@NonNull final String appId, @NonNull final String apiKey) {
        super(new HashMap<String, String>() {{
            put(KEY_APP_ID, appId);
            put(KEY_API_KEY, apiKey);
        }});
    }

    @SuppressWarnings("unused")
    public MintegralConfig(@Nullable Map<String, String> networkParams) {
        super(networkParams);
    }

    @NonNull
    @Override
    protected NetworkAdapter createNetworkAdapter() {
        return new MintegralAdapter();
    }

    public MintegralConfig withMediationConfig(@NonNull AdsFormat adsFormat,
                                               @NonNull String unitId) {
        return withMediationConfig(adsFormat, unitId, null, null, null);
    }

    public MintegralConfig withMediationConfig(@NonNull AdsFormat adsFormat,
                                               @NonNull final String unitId,
                                               @Nullable final String rewardId) {
        return withMediationConfig(adsFormat, unitId, rewardId, null, null);
    }

    @SuppressWarnings("WeakerAccess")
    public MintegralConfig withMediationConfig(@NonNull AdsFormat adsFormat,
                                               @NonNull final String unitId,
                                               @Nullable final String rewardId,
                                               @Nullable final String appId,
                                               @Nullable final String apiKey) {
        return withMediationConfig(adsFormat, new HashMap<String, String>() {{
            put(KEY_UNIT_ID, unitId);
            if (!TextUtils.isEmpty(rewardId)) {
                put(KEY_REWARD_ID, rewardId);
            }
            if (!TextUtils.isEmpty(appId)) {
                put(KEY_APP_ID, appId);
            }
            if (!TextUtils.isEmpty(apiKey)) {
                put(KEY_API_KEY, apiKey);
            }
        }});
    }

}
