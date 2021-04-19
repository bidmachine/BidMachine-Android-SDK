package io.bidmachine.ads.networks.my_target;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.my.target.common.CustomParams;
import com.my.target.common.MyTargetManager;
import com.my.target.common.MyTargetPrivacy;
import com.my.target.common.MyTargetVersion;

import java.util.HashMap;
import java.util.Map;

import io.bidmachine.AdsType;
import io.bidmachine.ContextProvider;
import io.bidmachine.HeaderBiddingAdRequestParams;
import io.bidmachine.HeaderBiddingAdapter;
import io.bidmachine.HeaderBiddingCollectParamsCallback;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfigParams;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.TargetingInfo;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.utils.BMError;
import io.bidmachine.utils.Gender;

class MyTargetAdapter extends NetworkAdapter implements HeaderBiddingAdapter {

    @Nullable
    private static String bidderToken = null;

    MyTargetAdapter() {
        super("my_target",
              MyTargetVersion.VERSION,
              BuildConfig.VERSION_NAME,
              new AdsType[]{AdsType.Banner, AdsType.Interstitial, AdsType.Rewarded});
    }

    @Override
    public UnifiedBannerAd createBanner() {
        return new MyTargetBanner();
    }

    @Override
    public UnifiedFullscreenAd createInterstitial() {
        return new MyTargetInterstitial();
    }

    @Override
    public UnifiedFullscreenAd createRewarded() {
        return new MyTargetRewarded();
    }

    @Override
    protected void onInitialize(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParams adRequestParams,
                                @NonNull NetworkConfigParams networkConfig) {
        updateRestrictions(adRequestParams);
    }

    @Override
    public void collectHeaderBiddingParams(@NonNull ContextProvider contextProvider,
                                           @NonNull UnifiedAdRequestParams adRequestParams,
                                           @NonNull HeaderBiddingAdRequestParams hbAdRequestParams,
                                           @NonNull final HeaderBiddingCollectParamsCallback collectCallback,
                                           @NonNull Map<String, String> mediationConfig) throws Exception {
        final String slotId = mediationConfig.get(MyTargetConfig.KEY_SLOT_ID);
        if (TextUtils.isEmpty(slotId)) {
            collectCallback.onCollectFail(BMError.requestError("slot_id not provided"));
            return;
        }
        MyTargetManager.setDebugMode(adRequestParams.isTestMode());
        updateRestrictions(adRequestParams);
        obtainBidderToken(contextProvider.getContext(), new TokenListener() {
            @Override
            public void onInitialized(@NonNull String bidderToken) {
                Map<String, String> params = new HashMap<>();
                params.put(MyTargetConfig.KEY_BIDDER_TOKEN, bidderToken);
                params.put(MyTargetConfig.KEY_SLOT_ID, slotId);
                collectCallback.onCollectFinished(params);
            }

            @Override
            public void onInitializationFailed() {
                collectCallback.onCollectFail(BMError.Internal);
            }
        });
    }

    private void updateRestrictions(@NonNull UnifiedAdRequestParams adRequestParams) {
        DataRestrictions dataRestrictions = adRequestParams.getDataRestrictions();
        if (dataRestrictions.isUserInGdprScope()) {
            MyTargetPrivacy.setUserConsent(dataRestrictions.isUserHasConsent());
        }
        if (dataRestrictions.isUserInCcpaScope()) {
            MyTargetPrivacy.setCcpaUserConsent(dataRestrictions.isUserHasCcpaConsent());
        }
        MyTargetPrivacy.setUserAgeRestricted(dataRestrictions.isUserAgeRestricted());
    }

    static void updateTargeting(@NonNull UnifiedAdRequestParams adRequestParams,
                                @Nullable CustomParams customParams) {
        if (customParams == null) {
            return;
        }
        TargetingInfo targetingInfo = adRequestParams.getTargetingParams();
        Integer age = targetingInfo.getUserAge();
        if (age != null) {
            customParams.setAge(age);
        }
        Gender gender = targetingInfo.getGender();
        if (gender != null) {
            customParams.setGender(transformGender(gender));
        }
    }

    private static int transformGender(@NonNull Gender gender) {
        switch (gender) {
            case Female:
                return CustomParams.Gender.FEMALE;
            case Male:
                return CustomParams.Gender.MALE;
            default:
                return CustomParams.Gender.UNKNOWN;
        }
    }

    private static synchronized void obtainBidderToken(@NonNull final Context context,
                                                       @Nullable final TokenListener listener) {
        if (!TextUtils.isEmpty(bidderToken)) {
            if (listener != null) {
                assert bidderToken != null;
                listener.onInitialized(bidderToken);
            }
        } else {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    synchronized (MyTargetAdapter.class) {
                        if (!TextUtils.isEmpty(bidderToken)) {
                            if (listener != null) {
                                assert bidderToken != null;
                                listener.onInitialized(bidderToken);
                            }
                            return;
                        }
                        if (TextUtils.isEmpty(bidderToken)) {
                            try {
                                bidderToken = MyTargetManager.getBidderToken(context);
                            } catch (Throwable ignore) {
                            }
                        }
                        if (listener != null) {
                            if (TextUtils.isEmpty(bidderToken)) {
                                listener.onInitializationFailed();
                            } else {
                                assert bidderToken != null;
                                listener.onInitialized(bidderToken);
                            }
                        }
                    }
                }
            }.start();
        }
    }

    private interface TokenListener {
        void onInitialized(@NonNull String bidderToken);

        void onInitializationFailed();
    }

}
