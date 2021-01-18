package io.bidmachine;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.bidmachine.ads.networks.mraid.MraidAdapter;
import io.bidmachine.ads.networks.nast.NastAdapter;
import io.bidmachine.ads.networks.vast.VastAdapter;
import io.bidmachine.core.Logger;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.utils.BMError;

class NetworkRegistry {

    static {
        NetworkRegistry.registerNetworks(
                new NetworkConfig(null) {
                    @NonNull
                    @Override
                    protected NetworkAdapter createNetworkAdapter() {
                        return new MraidAdapter();
                    }
                },
                new NetworkConfig(null) {
                    @NonNull
                    @Override
                    protected NetworkAdapter createNetworkAdapter() {
                        return new VastAdapter();
                    }
                },
                new NetworkConfig(null) {
                    @NonNull
                    @Override
                    protected NetworkAdapter createNetworkAdapter() {
                        return new NastAdapter();
                    }
                });
    }

    static final String Mraid = "mraid";
    static final String Vast = "vast";
    static final String Nast = "nast";

    @VisibleForTesting
    static Set<NetworkConfig> pendingNetworks;
    @VisibleForTesting
    static final Map<String, NetworkConfig> cache = new ConcurrentHashMap<>();

    private static boolean isNetworksInitialized = false;

    @Nullable
    static NetworkConfig getConfig(String key) {
        if (key == null) {
            return null;
        }
        return cache.get(key);
    }

    static boolean isNetworkRegistered(@NonNull String name) {
        return isNetworkRegistered(name, null);
    }

    static boolean isNetworkRegistered(@NonNull String name,
                                       @Nullable RegisterSource registerSource) {
        try {
            if (pendingNetworks != null) {
                for (NetworkConfig networkConfig : pendingNetworks) {
                    if (isNetworkConfigEquals(networkConfig, name, registerSource)) {
                        return true;
                    }
                }
            }
            if (isNetworkInitialized(name, registerSource)) {
                return true;
            }
        } catch (Throwable ignore) {
        }
        return false;
    }

    static boolean isNetworkInitialized(@NonNull String name,
                                        @Nullable RegisterSource registerSource) {
        NetworkConfig networkConfig = getConfig(name);
        return isNetworkConfigEquals(networkConfig, name, registerSource);
    }

    static boolean isNetworkConfigEquals(NetworkConfig networkConfig,
                                         @NonNull String networkKey,
                                         @Nullable RegisterSource registerSource) {
        return networkConfig != null
                && networkKey.equals(networkConfig.getKey())
                && checkRegisterSource(networkConfig.getRegisterSource(), registerSource);
    }

    static boolean checkRegisterSource(@Nullable RegisterSource networkRegisterSource,
                                       @Nullable RegisterSource registerSource) {
        return registerSource == null || networkRegisterSource == registerSource;
    }

    static void registerNetworks(@Nullable NetworkConfig... networkConfigs) {
        if (networkConfigs != null && networkConfigs.length > 0) {
            for (NetworkConfig networkConfig : networkConfigs) {
                registerNetwork(networkConfig);
            }
        }
    }

    static void registerNetworks(@NonNull Context context, @Nullable final String jsonData) {
        if (TextUtils.isEmpty(jsonData)) {
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                NetworkConfig networkConfig = NetworkConfig.create(context, jsonObject);
                if (networkConfig != null) {
                    registerNetwork(networkConfig);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static void registerNetwork(@Nullable NetworkConfig networkConfig) {
        if (networkConfig == null) {
            return;
        }
        if (pendingNetworks == null) {
            pendingNetworks = new HashSet<>();
        }
        pendingNetworks.add(networkConfig);
    }

    static boolean isNetworksInitialized() {
        return isNetworksInitialized;
    }

    static void initializeNetworks(@NonNull final ContextProvider contextProvider,
                                   @NonNull final UnifiedAdRequestParams unifiedAdRequestParams,
                                   @Nullable final NetworksInitializeCallback initializeCallback) {
        if (isNetworksInitialized) {
            return;
        }
        isNetworksInitialized = true;
        new Thread() {
            @Override
            public void run() {
                super.run();
                final List<NetworkLoadTask> loadTasks = new ArrayList<>();
                if (pendingNetworks != null) {
                    for (NetworkConfig networkConfig : pendingNetworks) {
                        loadTasks.add(new NetworkLoadTask(contextProvider,
                                                          unifiedAdRequestParams,
                                                          networkConfig));
                    }
                }
                if (loadTasks.size() > 0) {
                    final CountDownLatch latch = new CountDownLatch(loadTasks.size());
                    final NetworkLoadTask.NetworkLoadCallback loadTaskCallback = new NetworkLoadTask.NetworkLoadCallback() {
                        @Override
                        public void onNetworkLoadingFinished() {
                            latch.countDown();
                        }
                    };
                    for (NetworkLoadTask task : loadTasks) {
                        task.withCallback(loadTaskCallback).execute();
                    }
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (initializeCallback != null) {
                    initializeCallback.onNetworksInitialized();
                }
            }
        }.start();
    }

    private static final class NetworkLoadTask implements Runnable {

        private static final Executor executor = Executors.newFixedThreadPool(
                Math.max(8, Runtime.getRuntime().availableProcessors() * 4));

        @NonNull
        private final ContextProvider contextProvider;
        @NonNull
        private final UnifiedAdRequestParams adRequestParams;
        @NonNull
        private final NetworkConfig networkConfig;

        @Nullable
        private NetworkLoadCallback callback;

        private NetworkLoadTask(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParams adRequestParams,
                                @NonNull NetworkConfig networkConfig) {
            this.contextProvider = contextProvider;
            this.adRequestParams = adRequestParams;
            this.networkConfig = networkConfig;
        }

        NetworkLoadTask withCallback(@Nullable NetworkLoadCallback callback) {
            this.callback = callback;
            return this;
        }

        @Override
        public void run() {
            process();
            if (callback != null) {
                callback.onNetworkLoadingFinished();
            }
        }

        private void process() {
            String networkName = networkConfig.getKey();
            TrackingObject trackingObject = new TrackingObject() {
                @Override
                public Object getTrackingKey() {
                    return networkConfig.getKey() + "_initialize";
                }
            };
            Logger.log(String.format("Load network from config start: %s", networkName));
            try {
                BidMachineEvents.eventStart(
                        trackingObject,
                        TrackEventType.HeaderBiddingNetworkInitialize,
                        new TrackEventInfo()
                                .withParameter("HB_NETWORK", networkName),
                        null);
                NetworkAdapter networkAdapter = networkConfig.obtainNetworkAdapter();
                networkAdapter.setLogging(Logger.isLoggingEnabled());
                networkAdapter.initialize(contextProvider,
                                          adRequestParams,
                                          networkConfig.getNetworkConfigParams());

                String key = networkConfig.getKey();
                if (!cache.containsKey(key)) {
                    cache.put(key, networkConfig);
                    pendingNetworks.remove(networkConfig);
                }
                for (AdsType type : networkConfig.getSupportedAdsTypes()) {
                    type.addNetworkConfig(key, networkConfig);
                }
                Logger.log(
                        String.format("Load network from config finish: %s, %s, %s",
                                      networkName,
                                      networkAdapter.getVersion(),
                                      networkAdapter.getAdapterVersion()));
                if (networkAdapter instanceof HeaderBiddingAdapter) {
                    BidMachineEvents.eventFinish(trackingObject,
                                                 TrackEventType.HeaderBiddingNetworkInitialize,
                                                 null,
                                                 null);
                } else {
                    BidMachineEvents.clearEvent(trackingObject,
                                                TrackEventType.HeaderBiddingNetworkInitialize);
                }
            } catch (Throwable throwable) {
                Logger.log(String.format("Network (%s) load fail!", networkName));
                Logger.log(throwable);
                BidMachineEvents.eventFinish(trackingObject,
                                             TrackEventType.HeaderBiddingNetworkInitialize,
                                             null,
                                             BMError.Internal);
            }
        }

        void execute() {
            executor.execute(this);
        }

        interface NetworkLoadCallback {
            void onNetworkLoadingFinished();
        }

    }

    static void setLoggingEnabled(boolean enabled) {
        for (Map.Entry<String, NetworkConfig> entry : cache.entrySet()) {
            entry.getValue().obtainNetworkAdapter().setLogging(enabled);
        }
    }

    interface NetworksInitializeCallback {
        void onNetworksInitialized();
    }

}