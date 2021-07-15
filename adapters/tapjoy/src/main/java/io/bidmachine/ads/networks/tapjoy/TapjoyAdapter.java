package io.bidmachine.ads.networks.tapjoy;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tapjoy.TJConnectListener;
import com.tapjoy.TJPrivacyPolicy;
import com.tapjoy.Tapjoy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import io.bidmachine.AdsType;
import io.bidmachine.ContextProvider;
import io.bidmachine.HeaderBiddingAdRequestParams;
import io.bidmachine.HeaderBiddingAdapter;
import io.bidmachine.HeaderBiddingCollectParamsCallback;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfigParams;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.utils.BMError;

class TapjoyAdapter extends NetworkAdapter implements HeaderBiddingAdapter {

    TapjoyAdapter() {
        super("tapjoy",
              BuildConfig.ADAPTER_SDK_VERSION_NAME,
              BuildConfig.ADAPTER_VERSION_NAME,
              new AdsType[]{AdsType.Interstitial, AdsType.Rewarded});
    }

    @Override
    public UnifiedFullscreenAd createInterstitial() {
        return new TapjoyFullscreenAd();
    }

    @Override
    public UnifiedFullscreenAd createRewarded() {
        return new TapjoyFullscreenAd();
    }

    @Override
    protected void onInitialize(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParams adRequestParams,
                                @NonNull NetworkConfigParams networkConfig) throws Throwable {
        super.onInitialize(contextProvider, adRequestParams, networkConfig);

        configure(adRequestParams);
        Map<String, String> networkParams = networkConfig.obtainNetworkParams();
        if (networkParams != null) {
            final String sdkKey = networkParams.get(TapjoyConfig.KEY_SDK);
            if (!TextUtils.isEmpty(sdkKey)) {
                assert sdkKey != null;
                initializeTapjoy(contextProvider, sdkKey, null);
            }
        }
    }

    @Override
    public void collectHeaderBiddingParams(@NonNull ContextProvider contextProvider,
                                           @NonNull UnifiedAdRequestParams adRequestParams,
                                           @NonNull HeaderBiddingAdRequestParams hbAdRequestParams,
                                           @NonNull final HeaderBiddingCollectParamsCallback collectCallback,
                                           @NonNull Map<String, String> mediationConfig) throws Throwable {
        final String sdkKey = mediationConfig.get(TapjoyConfig.KEY_SDK);
        if (TextUtils.isEmpty(sdkKey)) {
            collectCallback.onCollectFail(BMError.adapterGetsParameter(TapjoyConfig.KEY_SDK));
            return;
        }
        assert sdkKey != null;
        final String placementName = mediationConfig.get(TapjoyConfig.KEY_PLACEMENT_NAME);
        if (TextUtils.isEmpty(placementName)) {
            collectCallback.onCollectFail(BMError.adapterGetsParameter(TapjoyConfig.KEY_PLACEMENT_NAME));
            return;
        }
        assert placementName != null;
        configure(adRequestParams);
        initializeTapjoy(contextProvider, sdkKey, new TapjoyInitializeListener() {
            @Override
            public void onInitialized() {
                Map<String, String> params = new HashMap<>();
                params.put(TapjoyConfig.KEY_SDK, sdkKey);
                params.put(TapjoyConfig.KEY_PLACEMENT_NAME, placementName);
                params.put(TapjoyConfig.KEY_TOKEN, Tapjoy.getUserToken());
                collectCallback.onCollectFinished(params);
            }

            @Override
            public void onInitializationFail() {
                collectCallback.onCollectFail(BMError.adapterInitialization());
            }
        });
    }

    private static void configure(@NonNull UnifiedAdRequestParams adRequestParams) {
        Tapjoy.setDebugEnabled(adRequestParams.isTestMode());

        DataRestrictions dataRestrictions = adRequestParams.getDataRestrictions();
        TJPrivacyPolicy tjPrivacyPolicy = TJPrivacyPolicy.getInstance();
        tjPrivacyPolicy.setBelowConsentAge(dataRestrictions.isUserAgeRestricted());
        tjPrivacyPolicy.setSubjectToGDPR(dataRestrictions.isUserInGdprScope());
        tjPrivacyPolicy.setUserConsent(dataRestrictions.getIABGDPRString());
        tjPrivacyPolicy.setUSPrivacy(dataRestrictions.getUSPrivacyString());
    }

    private static synchronized void initializeTapjoy(@NonNull final ContextProvider contextProvider,
                                                      @NonNull final String sdkKey,
                                                      @Nullable final TapjoyInitializeListener listener) {
        if (finalizeInitialization(listener)) {
            return;
        }
        final Context applicationContext = contextProvider.getApplicationContext();
        new Thread() {
            @Override
            public void run() {
                super.run();

                synchronized (TapjoyAdapter.class) {
                    if (finalizeInitialization(listener)) {
                        return;
                    }
                    final CountDownLatch syncLock = new CountDownLatch(1);
                    Tapjoy.limitedConnect(applicationContext, sdkKey, new TJConnectListener() {
                        @Override
                        public void onConnectSuccess() {
                            if (listener != null) {
                                listener.onInitialized();
                            }
                            syncLock.countDown();
                        }

                        @Override
                        public void onConnectFailure() {
                            if (listener != null) {
                                listener.onInitializationFail();
                            }
                            syncLock.countDown();
                        }
                    });
                    try {
                        syncLock.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private static boolean finalizeInitialization(@Nullable TapjoyInitializeListener listener) {
        if (Tapjoy.isLimitedConnected()) {
            if (listener != null) {
                listener.onInitialized();
            }
            return true;
        }
        return false;
    }


    private interface TapjoyInitializeListener {

        void onInitialized();

        void onInitializationFail();

    }

}