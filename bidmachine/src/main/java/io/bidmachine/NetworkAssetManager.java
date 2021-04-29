package io.bidmachine;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;

class NetworkAssetManager {

    private static final String BID_MACHINE_ASSET_PATH = "bm_networks";
    private static final String BID_MACHINE_ASSET_FILE_EXTENSION = "bmnetwork";
    private static final String KEY_CLASSPATH = "classpath";
    private static final String KEY_NAME = "name";
    private static final String KEY_VERSION = "version";

    private static final Map<String, NetworkAssetParams> networkAssetParamsMap = new ConcurrentHashMap<>();

    static void findNetworks(@NonNull Context context) {
        if (networkAssetParamsMap.size() > 0) {
            return;
        }
        try {
            AssetManager assetManager = context.getAssets();
            if (assetManager == null) {
                return;
            }
            for (String networkFile : assetManager.list(BID_MACHINE_ASSET_PATH)) {
                findNetwork(assetManager, networkFile);
            }
        } catch (Exception ignore) {
        }
    }

    @Nullable
    static NetworkAssetParams getNetworkAssetParams(@NonNull Context context,
                                                    @NonNull String networkName) {
        if (networkAssetParamsMap.containsKey(networkName)) {
            return networkAssetParamsMap.get(networkName);
        }

        try {
            AssetManager assetManager = context.getAssets();
            if (assetManager == null) {
                return null;
            }
            String networkFile = String.format("%s.%s",
                                               networkName,
                                               BID_MACHINE_ASSET_FILE_EXTENSION);
            return findNetwork(assetManager, networkFile);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static NetworkAssetParams findNetwork(@NonNull AssetManager assetManager,
                                                  @NonNull String networkFile) {
        NetworkAssetParams networkAssetParams = createNetworkParams(assetManager, networkFile);
        if (networkAssetParams != null) {
            networkAssetParamsMap.put(networkAssetParams.getName(), networkAssetParams);
        }
        return networkAssetParams;
    }

    @Nullable
    private static NetworkAssetParams createNetworkParams(@NonNull AssetManager assetManager,
                                                          @NonNull String networkFile) {
        try {
            if (TextUtils.isEmpty(networkFile)) {
                return null;
            }

            String fileContent = readAssetByNetworkName(assetManager, networkFile);
            if (TextUtils.isEmpty(fileContent)) {
                return null;
            }
            assert fileContent != null;

            JSONObject networkAssetConfig = new JSONObject(fileContent);
            String name = networkAssetConfig.optString(KEY_NAME);
            String version = networkAssetConfig.optString(KEY_VERSION);
            String classpath = networkAssetConfig.optString(KEY_CLASSPATH);
            if (TextUtils.isEmpty(name)
                    || TextUtils.isEmpty(version)
                    || TextUtils.isEmpty(classpath)) {
                return null;
            }

            return new NetworkAssetParams(name, version, classpath);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static String readAssetByNetworkName(@NonNull AssetManager assetManager,
                                                 @NonNull String networkFile) {
        InputStream inputStream = null;
        try {
            String networkFilePath = String.format("%s/%s",
                                                   BID_MACHINE_ASSET_PATH,
                                                   networkFile);
            inputStream = assetManager.open(networkFilePath);
            return Utils.streamToString(inputStream);
        } catch (Throwable t) {
            Logger.log(t);
        } finally {
            Utils.close(inputStream);
        }
        return null;
    }

}