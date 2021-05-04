package io.bidmachine;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import io.bidmachine.core.Logger;
import io.bidmachine.core.NetworkRequest;
import io.bidmachine.core.Utils;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.protobuf.AdNetwork;
import io.bidmachine.protobuf.InitRequest;
import io.bidmachine.protobuf.InitResponse;
import io.bidmachine.utils.ActivityHelper;
import io.bidmachine.utils.BMError;
import io.bidmachine.utils.BluetoothUtils;

final class BidMachineImpl {

    @SuppressLint("StaticFieldLeak")
    private static volatile BidMachineImpl instance;

    static BidMachineImpl get() {
        if (instance == null) {
            synchronized (BidMachineImpl.class) {
                if (instance == null) {
                    instance = new BidMachineImpl();
                }
            }
        }
        return instance;
    }

    static {
        Logger.setTag("BidMachine");
        Logger.setMessageBuilder(new Logger.LoggerMessageBuilder() {
            @Override
            public String buildMessage(String origin) {
                if (get().isTestMode()) {
                    return "(TEST MODE) " + origin;
                }
                return origin;
            }
        });
    }

    private static final String BID_MACHINE_SHARED_PREF = "BidMachinePref";
    @VisibleForTesting
    static String DEF_INIT_URL = BuildConfig.BM_API_URL + "/init";
    private static final String DEF_AUCTION_URL = BuildConfig.BM_API_URL + "/openrtb3/auction";
    private static final String PREF_INIT_DATA = "initData";
    private static final String PREF_IFV = "bid_machine_ifv";

    @Nullable
    @VisibleForTesting
    Context appContext;
    @Nullable
    private SessionTracker sessionTracker;
    @Nullable
    private String sellerId;
    @NonNull
    private TargetingParams targetingParams = new TargetingParams();
    @NonNull
    private ExtraParams extraParams = new ExtraParams();
    @NonNull
    private final UserRestrictionParams userRestrictionParams = new UserRestrictionParams();
    @NonNull
    private final PriceFloorParams priceFloorParams =
            new PriceFloorParams()
                    .addPriceFloor(UUID.randomUUID().toString(), 0.01);
    @NonNull
    private final DeviceParams deviceParams = new DeviceParams();
    @NonNull
    private final IABSharedPreference iabSharedPreference = new IABSharedPreference();

    private Publisher publisher;

    private boolean isTestMode;
    private boolean isInitialized;

    ApiRequest<InitRequest, InitResponse> currentInitRequest;

    @VisibleForTesting
    String currentInitUrl = DEF_INIT_URL;
    @VisibleForTesting
    String currentAuctionUrl = DEF_AUCTION_URL;
    @VisibleForTesting
    private final Map<TrackEventType, List<String>> trackingEventTypes =
            new EnumMap<>(TrackEventType.class);
    private final List<NetworkConfig> initNetworkConfigList = new ArrayList<>();

    private long initRequestDelayMs = 0;
    private static final long MIN_INIT_REQUEST_DELAY_MS = TimeUnit.SECONDS.toMillis(2);
    private static final long MAX_INIT_REQUEST_DELAY_MS = TimeUnit.SECONDS.toMillis(128);

    @Nullable
    private WeakReference<Activity> weakTopActivity;

    private final Runnable rescheduleInitRunnable = new Runnable() {
        @Override
        public void run() {
            requestInitData(appContext, sellerId, null);
        }
    };

    private final TrackingObject trackingObject = new TrackingObject() {
        @Override
        public Object getTrackingKey() {
            return BidMachineImpl.class.getSimpleName();
        }
    };

    private final List<AdRequest.AdRequestListener> adRequestListeners = new CopyOnWriteArrayList<>();

    @VisibleForTesting
    String ifv;

    synchronized void initialize(@NonNull final Context context,
                                 @NonNull final String sellerId,
                                 @Nullable final InitializationCallback callback) {
        if (isInitialized) {
            return;
        }
        if (context == null) {
            Logger.log("Initialization fail: Context is not provided");
            return;
        }
        if (TextUtils.isEmpty(sellerId)) {
            Logger.log("Initialization fail: Seller id is not provided");
            return;
        }
        this.sellerId = sellerId;
        appContext = context.getApplicationContext();
        sessionTracker = new SessionTrackerImpl();
        loadStoredInitResponse(context);
        iabSharedPreference.initialize(context);
        setTopActivity(ActivityHelper.getTopActivity());
        ((Application) context.getApplicationContext())
                .registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks());
        SessionManager.get().resume();
        BluetoothUtils.register(context);
        requestInitData(context, sellerId, callback);
        isInitialized = true;
    }

    @Nullable
    Context getAppContext() {
        return appContext;
    }

    @NonNull
    String obtainIFV(@NonNull Context context) {
        if (!TextUtils.isEmpty(ifv)) {
            return ifv;
        }
        SharedPreferences preferences = context.getSharedPreferences(BID_MACHINE_SHARED_PREF,
                                                                     Context.MODE_PRIVATE);
        ifv = preferences.getString(PREF_IFV, null);
        if (!TextUtils.isEmpty(ifv)) {
            return ifv;
        }
        ifv = UUID.randomUUID().toString();
        preferences.edit()
                .putString(PREF_IFV, ifv)
                .apply();
        return ifv;
    }

    private void requestInitData(@NonNull final Context context,
                                 @NonNull final String sellerId,
                                 @Nullable final InitializationCallback callback) {
        if (currentInitRequest != null) {
            return;
        }
        BidMachineEvents.eventStart(trackingObject, TrackEventType.InitLoading, null);
        Utils.onBackgroundThread(new Runnable() {
            @Override
            public void run() {
                currentInitRequest = new ApiRequest.Builder<InitRequest, InitResponse>()
                        .url(currentInitUrl)
                        .setDataBinder(new ApiRequest.ApiInitDataBinder())
                        .setRequestData(OrtbUtils.obtainInitRequest(context,
                                                                    sellerId,
                                                                    targetingParams,
                                                                    userRestrictionParams))
                        .setCallback(new NetworkRequest.Callback<InitResponse, BMError>() {
                            @Override
                            public void onSuccess(@Nullable InitResponse result) {
                                currentInitRequest = null;
                                if (result != null) {
                                    handleInitResponse(result);
                                    storeInitResponse(context, result);

                                    initializeNetworks(context, result.getAdNetworksList());
                                }
                                initRequestDelayMs = 0;
                                Utils.cancelBackgroundThreadTask(rescheduleInitRunnable);
                                notifyInitializationFinished(callback);
                                BidMachineEvents.eventFinish(trackingObject,
                                                             TrackEventType.InitLoading,
                                                             null,
                                                             null);
                            }

                            @Override
                            public void onFail(@Nullable BMError result) {
                                currentInitRequest = null;
                                if (initRequestDelayMs <= 0) {
                                    initRequestDelayMs = MIN_INIT_REQUEST_DELAY_MS;
                                } else {
                                    initRequestDelayMs *= 2;
                                    if (initRequestDelayMs >= MAX_INIT_REQUEST_DELAY_MS) {
                                        initRequestDelayMs = MAX_INIT_REQUEST_DELAY_MS;
                                    }
                                }
                                if (!NetworkRegistry.isNetworksInitialized()) {
                                    InitResponse initResponse = getInitResponseFromPref(context);
                                    if (initResponse != null) {
                                        initializeNetworks(context,
                                                           initResponse.getAdNetworksList());
                                    }
                                }
                                Logger.log("reschedule init request (" + initRequestDelayMs + ")");
                                Utils.onBackgroundThread(rescheduleInitRunnable,
                                                         initRequestDelayMs);
                                // According requirements we should notify that SDK is initialized event if init request fail
                                notifyInitializationFinished(callback);
                                BidMachineEvents.eventFinish(trackingObject,
                                                             TrackEventType.InitLoading,
                                                             null,
                                                             result);
                            }
                        })
                        .request();
            }
        });
    }

    private void notifyInitializationFinished(@Nullable final InitializationCallback callback) {
        if (callback != null) {
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.onInitialized();
                }
            });
        }
    }

    private void handleInitResponse(@NonNull InitResponse response) {
        if (!TextUtils.isEmpty(response.getEndpoint())) {
            currentAuctionUrl = response.getEndpoint();
        }
        trackingEventTypes.clear();
        OrtbUtils.prepareEvents(trackingEventTypes, response.getEventList());
        SessionManager.get().setSessionResetAfter(response.getSessionResetAfter());
    }

    private void initializeNetworks(@NonNull Context context,
                                    @Nullable List<AdNetwork> networkList) {
        if (NetworkRegistry.isNetworksInitialized()) {
            return;
        }
        final TargetingParams targetingParams = getTargetingParams();
        final DataRestrictions dataRestrictions = getUserRestrictionParams();
        if (networkList != null) {
            for (AdNetwork adNetwork : networkList) {
                if (NetworkRegistry.isNetworkRegistered(adNetwork.getName())) {
                    continue;
                }
                NetworkConfig networkConfig = NetworkConfigFactory.create(context, adNetwork);
                if (networkConfig != null) {
                    networkConfig.setRegisterSource(RegisterSource.Init);

                    NetworkRegistry.registerNetwork(networkConfig);
                    initNetworkConfigList.add(networkConfig);
                }
            }
        }
        NetworkRegistry.initializeNetworks(new SimpleContextProvider(context),
                                           new UnifiedAdRequestParamsImpl(targetingParams,
                                                                          dataRestrictions),
                                           new NetworkRegistry.NetworksInitializeCallback() {
                                               @Override
                                               public void onNetworksInitialized() {
                                                   AdRequestExecutor.get().enable();
                                               }
                                           });
    }

    private void storeInitResponse(@NonNull Context context, @NonNull InitResponse response) {
        SharedPreferences preferences = context.getSharedPreferences(BID_MACHINE_SHARED_PREF,
                                                                     Context.MODE_PRIVATE);
        try {
            String initResponse = Base64.encodeToString(response.toByteArray(), Base64.DEFAULT);
            preferences.edit()
                    .putString(PREF_INIT_DATA, initResponse)
                    .apply();
        } catch (Exception ignore) {
        }
    }

    private void loadStoredInitResponse(@NonNull Context context) {
        InitResponse initResponse = getInitResponseFromPref(context);
        if (initResponse != null) {
            handleInitResponse(initResponse);
        }
    }

    @Nullable
    private InitResponse getInitResponseFromPref(@NonNull Context context) {
        SharedPreferences preferences = context.getSharedPreferences(BID_MACHINE_SHARED_PREF,
                                                                     Context.MODE_PRIVATE);
        if (preferences.contains(PREF_INIT_DATA)) {
            try {
                String initResponse = preferences.getString(PREF_INIT_DATA, null);
                return InitResponse.parseFrom(Base64.decode(initResponse, Base64.DEFAULT));
            } catch (Exception ignore) {
                preferences.edit().remove(PREF_INIT_DATA).apply();
            }
        }
        return null;
    }

    @Nullable
    List<String> getTrackingUrls(@NonNull TrackEventType eventType) {
        return trackingEventTypes.get(eventType);
    }

    @NonNull
    List<NetworkConfig> getInitNetworkConfigList() {
        return initNetworkConfigList;
    }

    boolean isInitialized() {
        return isInitialized;
    }

    void setTestMode(boolean testMode) {
        isTestMode = testMode;
    }

    boolean isTestMode() {
        return isTestMode;
    }

    @Nullable
    SessionTracker getSessionTracker() {
        return sessionTracker;
    }

    @Nullable
    String getSellerId() {
        return sellerId;
    }

    void setTargetingParams(@Nullable TargetingParams targetingParams) {
        this.targetingParams = targetingParams != null ? targetingParams : new TargetingParams();
    }

    @NonNull
    TargetingParams getTargetingParams() {
        return targetingParams;
    }

    @Nullable
    Publisher getPublisher() {
        return publisher;
    }

    void setPublisher(@Nullable Publisher publisher) {
        this.publisher = publisher;
    }

    void setExtraParams(@Nullable ExtraParams extraParams) {
        this.extraParams = extraParams != null ? extraParams : new ExtraParams();
    }

    @NonNull
    ExtraParams getExtraParams() {
        return extraParams;
    }

    @NonNull
    UserRestrictionParams getUserRestrictionParams() {
        return userRestrictionParams;
    }

    @NonNull
    PriceFloorParams getPriceFloorParams() {
        return priceFloorParams;
    }

    @NonNull
    DeviceParams getDeviceParams() {
        return deviceParams;
    }

    @NonNull
    IABSharedPreference getIabSharedPreference() {
        return iabSharedPreference;
    }

    void setEndpoint(@NonNull String url) {
        if (isInitialized) {
            Logger.log("Can't change endpoint url after initialization");
            return;
        }
        if (TextUtils.isEmpty(url)) {
            Logger.log("Endpoint is empty or null, skipping setting new endpoint...");
            return;
        }
        currentInitUrl = url + "/init";
        currentAuctionUrl = url + "/openrtb3/auction";
    }

    String getAuctionUrl() {
        return currentAuctionUrl;
    }

    @Nullable
    Activity getTopActivity() {
        return weakTopActivity != null ? weakTopActivity.get() : null;
    }

    void setTopActivity(@Nullable Activity activity) {
        if (activity != null) {
            weakTopActivity = new WeakReference<>(activity);
        }
    }

    void registerAdRequestListener(@Nullable AdRequest.AdRequestListener adRequestListener) {
        if (adRequestListener == null) {
            return;
        }
        adRequestListeners.add(adRequestListener);
    }

    void unregisterAdRequestListener(@Nullable AdRequest.AdRequestListener adRequestListener) {
        if (adRequestListener == null) {
            return;
        }
        adRequestListeners.remove(adRequestListener);
    }

    @NonNull
    List<AdRequest.AdRequestListener> getAdRequestListeners() {
        return adRequestListeners;
    }

}