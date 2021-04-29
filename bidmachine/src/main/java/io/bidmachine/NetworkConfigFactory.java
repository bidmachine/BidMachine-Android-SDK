package io.bidmachine;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;
import io.bidmachine.protobuf.AdNetwork;

class NetworkConfigFactory {

    private static final String KEY_NETWORK = "network";
    private static final String KEY_FORMAT = "format";
    private static final String KEY_AD_UNITS = "ad_units";
    private static final String[] PRIVATE_FIELDS = new String[]{
            KEY_NETWORK,
            KEY_FORMAT,
            KEY_AD_UNITS
    };

    @Nullable
    static NetworkConfig create(Context context, @NonNull AdNetwork adNetwork) {
        if (context == null) {
            return null;
        }
        String networkName = adNetwork.getName();
        if (TextUtils.isEmpty(networkName)) {
            return null;
        }
        NetworkConfig networkConfig = create(context,
                                             networkName,
                                             adNetwork.getCustomParamsMap());
        if (networkConfig == null) {
            return null;
        }
        for (AdNetwork.AdUnit adUnit : adNetwork.getAdUnitsList()) {
            AdsFormat adsFormat = AdsFormat.byRemoteName(adUnit.getAdFormat());
            if (adsFormat != null) {
                networkConfig.withMediationConfig(adsFormat,
                                                  adUnit.getCustomParamsMap());
            } else {
                Logger.log(String.format("Network (%s) adUnit register fail: %s not provided",
                                         networkName,
                                         KEY_FORMAT));
            }
        }
        return networkConfig;
    }

    @Nullable
    static NetworkConfig create(Context context, @NonNull JSONObject networkConfigJson) {
        if (context == null) {
            return null;
        }
        String networkName = null;
        try {
            networkName = networkConfigJson.getString(KEY_NETWORK);
            NetworkConfig networkConfig = create(context,
                                                 networkName,
                                                 Utils.toMap(networkConfigJson));
            if (networkConfig == null) {
                return null;
            }
            JSONArray params = networkConfigJson.getJSONArray(KEY_AD_UNITS);
            for (int i = 0; i < params.length(); i++) {
                JSONObject mediationConfig = params.getJSONObject(i);
                AdsFormat format = AdsFormat.byRemoteName(mediationConfig.getString(KEY_FORMAT));
                if (format != null) {
                    networkConfig.withMediationConfig(format,
                                                      filterParams(Utils.toMap(mediationConfig)));
                } else {
                    Logger.log(String.format("Network (%s) adUnit register fail: %s not provided",
                                             networkName,
                                             KEY_FORMAT));
                }
            }
            Logger.log(String.format(
                    "Load network from json config completed successfully: %s, %s",
                    networkConfig.getKey(),
                    networkConfig.getVersion()));
            return networkConfig;
        } catch (Throwable t) {
            Logger.log(String.format("Network (%s) load fail!", networkName));
            Logger.log(t);
        }
        return null;
    }

    @Nullable
    private static NetworkConfig create(@NonNull Context context,
                                        @NonNull String networkName,
                                        @Nullable Map<String, String> networkParams) {
        NetworkAssetParams networkAssetParams = NetworkAssetManager.getNetworkAssetParams(context,
                                                                                          networkName);
        if (networkAssetParams == null) {
            return null;
        }
        try {
            return (NetworkConfig) Class.forName(networkAssetParams.getClasspath())
                    .getConstructor(Map.class)
                    .newInstance(filterParams(networkParams));
        } catch (Throwable t) {
            Logger.log(String.format("Network (%s) load fail!", networkName));
            Logger.log(t);
        }
        return null;
    }

    private static Map<String, String> filterParams(@Nullable Map<String, String> params) {
        if (params != null) {
            try {
                for (String privateField : PRIVATE_FIELDS) {
                    params.remove(privateField);
                }
            } catch (Exception ignore) {
            }
        }
        return params;
    }

}