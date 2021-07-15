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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.bidmachine.core.Logger;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.utils.BMError;

class NetworkRegistry {

    @VisibleForTesting
    static final Map<String, NetworkConfig> pendingNetworks = new ConcurrentHashMap<>();
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
        try {
            if (pendingNetworks.get(name) != null) {
                return true;
            }
            if (isNetworkInitialized(name, null)) {
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
                NetworkConfig networkConfig = NetworkConfigFactory.create(context, jsonObject);
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
        pendingNetworks.put(networkConfig.getKey(), networkConfig);
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
                for (NetworkConfig networkConfig : pendingNetworks.values()) {
                    loadTasks.add(new NetworkLoadTask(contextProvider,
                                                      unifiedAdRequestParams,
                                                      networkConfig));
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

    static void setLoggingEnabled(boolean enabled) {
        for (Map.Entry<String, NetworkConfig> entry : cache.entrySet()) {
            try {
                entry.getValue().obtainNetworkAdapter().setLogging(enabled);
            } catch (Throwable t) {
                Logger.log(t);
            }
        }
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
            String networkKey = networkConfig.getKey();
            TrackingObject trackingObject = new SimpleTrackingObject(networkKey + "_initialize");
            Logger.log(String.format("[%s] Start loading network from config", networkKey));
            try {
                BidMachineEvents.eventStart(trackingObject,
                                            TrackEventType.HeaderBiddingNetworkInitialize,
                                            new TrackEventInfo()
                                                    .withParameter("HB_NETWORK", networkKey));
                NetworkAdapter networkAdapter = networkConfig.obtainNetworkAdapter();
                networkAdapter.setLogging(Logger.isLoggingEnabled());
                networkAdapter.initialize(contextProvider,
                                          adRequestParams,
                                          networkConfig.getNetworkConfigParams());

                if (!cache.containsKey(networkKey)) {
                    cache.put(networkKey, networkConfig);
                    pendingNetworks.remove(networkKey);
                }
                for (AdsType type : networkConfig.getSupportedAdsTypes()) {
                    type.addNetworkConfig(networkKey, networkConfig);
                }
                Logger.log(
                        String.format(
                                "[%s] Finished loading network from config: %s, %s. Register source - %s",
                                networkKey,
                                networkAdapter.getVersion(),
                                networkAdapter.getAdapterVersion(),
                                networkConfig.getRegisterSource()));
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
                Logger.log(String.format("[%s] Failed to load network", networkKey));
                Logger.log(throwable);
                BidMachineEvents.eventFinish(trackingObject,
                                             TrackEventType.HeaderBiddingNetworkInitialize,
                                             null,
                                             BMError.internal("Exception when loading network"));
            }
        }

        void execute() {
            executor.execute(this);
        }


        interface NetworkLoadCallback {
            void onNetworkLoadingFinished();
        }

    }

    interface NetworksInitializeCallback {
        void onNetworksInitialized();
    }

}