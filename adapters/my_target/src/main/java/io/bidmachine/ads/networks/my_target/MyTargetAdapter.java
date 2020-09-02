package io.bidmachine.ads.networks.my_target;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.my.target.common.CustomParams;
import com.my.target.common.MyTargetPrivacy;
import com.my.target.common.MyTargetVersion;

import java.util.Collections;
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
        return new MyTargetFullscreenAd();
    }

    @Override
    public UnifiedFullscreenAd createRewarded() {
        return new MyTargetFullscreenAd();
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
                                           @NonNull HeaderBiddingCollectParamsCallback collectCallback,
                                           @NonNull Map<String, String> mediationConfig) {
        String slotId = mediationConfig.get(MyTargetConfig.KEY_SLOT_ID);
        if (TextUtils.isEmpty(slotId)) {
            collectCallback.onCollectFail(BMError.requestError("slot_id not provided"));
            return;
        }
        updateRestrictions(adRequestParams);
        Map<String, String> params = Collections.singletonMap(MyTargetConfig.KEY_SLOT_ID, slotId);
        collectCallback.onCollectFinished(params);
    }

    private void updateRestrictions(@NonNull UnifiedAdRequestParams adRequestParams) {
        DataRestrictions dataRestrictions = adRequestParams.getDataRestrictions();
        if (dataRestrictions.isUserInGdprScope()) {
            MyTargetPrivacy.setUserConsent(dataRestrictions.isUserHasConsent());
        }
        MyTargetPrivacy.setUserAgeRestricted(dataRestrictions.isUserAgeRestricted());
    }

    static void updateTargeting(@NonNull UnifiedAdRequestParams adRequestParams, @NonNull CustomParams customParams) {
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

}
