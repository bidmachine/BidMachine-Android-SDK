package io.bidmachine;

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

    private static Set<NetworkConfig> pendingNetworks;
    private static Set<JSONObject> pendingNetworksJson;

    @VisibleForTesting
    static final Map<String, NetworkConfig> cache = new ConcurrentHashMap<>();

    private static boolean isNetworksInitialized = false;

    @Nullable
    static NetworkConfig getConfig(String key) {
        return cache.get(key);
    }

    static void registerNetworks(@Nullable NetworkConfig... networkConfigs) {
        if (networkConfigs != null && networkConfigs.length > 0) {
            for (NetworkConfig config : networkConfigs) {
                if (pendingNetworks == null) {
                    pendingNetworks = new HashSet<>();
                }
                pendingNetworks.add(config);
            }
        }
    }

    static void registerNetworks(@Nullable final String jsonData) {
        if (TextUtils.isEmpty(jsonData)) {
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                if (pendingNetworksJson == null) {
                    pendingNetworksJson = new HashSet<>();
                }
                pendingNetworksJson.add(jsonArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                if (pendingNetworksJson != null) {
                    for (JSONObject networkConfig : pendingNetworksJson) {
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

        private static Executor executor = Executors.newFixedThreadPool(
                Math.max(8, Runtime.getRuntime().availableProcessors() * 4));

        @NonNull
        private ContextProvider contextProvider;
        @NonNull
        private UnifiedAdRequestParams adRequestParams;
        @Nullable
        private JSONObject jsonConfig;
        @Nullable
        private NetworkConfig networkConfig;
        @Nullable
        private NetworkLoadCallback callback;

        private NetworkLoadTask(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParams adRequestParams) {
            this.contextProvider = contextProvider;
            this.adRequestParams = adRequestParams;
        }

        private NetworkLoadTask(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParams adRequestParams,
                                @NonNull NetworkConfig networkConfig) {
            this(contextProvider, adRequestParams);
            this.networkConfig = networkConfig;
        }

        private NetworkLoadTask(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParams adRequestParams,
                                @NonNull JSONObject jsonConfig) {
            this(contextProvider, adRequestParams);
            this.jsonConfig = jsonConfig;
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
            if (jsonConfig != null) {
                networkConfig = NetworkConfig.create(contextProvider.getContext(), jsonConfig);
            }
            if (networkConfig != null) {
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