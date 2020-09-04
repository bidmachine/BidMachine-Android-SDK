package io.bidmachine;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Base64;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.bidmachine.core.AdvertisingIdClientInfo;
import io.bidmachine.core.Logger;
import io.bidmachine.core.NetworkRequest;
import io.bidmachine.core.Utils;
import io.bidmachine.models.DataRestrictions;
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
    private EnumMap<AdsType, SessionAdParams> sessionAdParamsMap = new EnumMap<>(AdsType.class);
    @NonNull
    private ExtraParams extraParams = new ExtraParams();
    @NonNull
    private UserRestrictionParams userRestrictionParams = new UserRestrictionParams();
    @NonNull
    private PriceFloorParams priceFloorParams =
            new PriceFloorParams()
                    .addPriceFloor(UUID.randomUUID().toString(), 0.01);
    @NonNull
    private DeviceParams deviceParams = new DeviceParams();
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
    private Map<TrackEventType, List<String>> trackingEventTypes =
            new EnumMap<>(TrackEventType.class);

    private long initRequestDelayMs = 0;
    private static final long MIN_INIT_REQUEST_DELAY_MS = TimeUnit.SECONDS.toMillis(2);
    private static final long MAX_INIT_REQUEST_DELAY_MS = TimeUnit.SECONDS.toMillis(128);

    Activity topActivity;

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

    private List<AdRequest.AdRequestListener> adRequestListeners = new ArrayList<>();

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
        AdvertisingIdClientInfo.executeTask(context, new AdvertisingIdClientInfo.Closure() {
            @Override
            public void executed(@NonNull AdvertisingIdClientInfo.AdvertisingProfile advertisingProfile) {
                AdvertisingPersonalData.setLimitAdTrackingEnabled(advertisingProfile.isLimitAdTrackingEnabled());
                AdvertisingPersonalData.setDeviceAdvertisingId(advertisingProfile.getId());
                final TargetingParams targetingParams = getTargetingParams();
                final DataRestrictions dataRestrictions = getUserRestrictionParams();
                NetworkRegistry.initializeNetworks(
                        new SimpleContextProvider(context),
                        new UnifiedAdRequestParamsImpl(targetingParams, dataRestrictions),
                        new NetworkRegistry.NetworksInitializeCallback() {
                            @Override
                            public void onNetworksInitialized() {
                                AdRequestExecutor.get().enable();
                            }
                        });
                requestInitData(context, sellerId, callback);
            }
        });
        topActivity = ActivityHelper.getTopActivity();
        ((Application) context.getApplicationContext())
                .registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks());
        SessionManager.get().resume();
        BluetoothUtils.register(context);
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
        if (currentInitRequest != null) return;
        BidMachineEvents.eventStart(trackingObject, TrackEventType.InitLoading, null);
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
                            handleInitResponse(context, result);
                            storeInitResponse(context, result);
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
                        Logger.log("reschedule init request (" + initRequestDelayMs + ")");
                        Utils.onBackgroundThread(rescheduleInitRunnable, initRequestDelayMs);
                        // According requirements we should notify that SDK is initialized event if init request fail
                        notifyInitializationFinished(callback);
                        BidMachineEvents.eventFinish(
                                trackingObject,
                                TrackEventType.InitLoading,
                                null,
                                result);
                    }
                })
                .request();
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

    private void handleInitResponse(@NonNull Context context, @NonNull InitResponse response) {
        if (!TextUtils.isEmpty(response.getEndpoint())) {
            currentAuctionUrl = response.getEndpoint();
        }
        trackingEventTypes.clear();
        OrtbUtils.prepareEvents(trackingEventTypes, response.getEventList());
    }

    private void storeInitResponse(@NonNull Context context, @NonNull InitResponse response) {
        SharedPreferences preferences = context.getSharedPreferences(BID_MACHINE_SHARED_PREF,
                                                                     Context.MODE_PRIVATE);
        preferences.edit()
                .putString(PREF_INIT_DATA,
                           Base64.encodeToString(response.toByteArray(), Base64.DEFAULT))
                .apply();
    }

    private void loadStoredInitResponse(@NonNull Context context) {
        SharedPreferences preferences = context.getSharedPreferences(BID_MACHINE_SHARED_PREF,
                                                                     Context.MODE_PRIVATE);
        if (preferences.contains(PREF_INIT_DATA)) {
            try {
                InitResponse initResponse = InitResponse.parseFrom(
                        Base64.decode(preferences.getString(PREF_INIT_DATA, null), Base64.DEFAULT));
                handleInitResponse(context, initResponse);
            } catch (Exception ignore) {
                preferences.edit().remove(PREF_INIT_DATA).apply();
            }
        }
    }

    @Nullable
    List<String> getTrackingUrls(@NonNull TrackEventType eventType) {
        return trackingEventTypes.get(eventType);
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

    @NonNull
    SessionAdParams getSessionAdParams(AdsType adsType) {
        SessionAdParams sessionAdParams = sessionAdParamsMap.get(adsType);
        if (sessionAdParams == null) {
            sessionAdParams = new SessionAdParams()
                    .setIsUserClickedOnLastAd(false);
            sessionAdParamsMap.put(adsType, sessionAdParams);
        }
        return sessionAdParams;
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

    static Activity getTopActivity() {
        return get().topActivity;
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

    List<AdRequest.AdRequestListener> getAdRequestListeners() {
        return adRequestListeners;
    }

}
