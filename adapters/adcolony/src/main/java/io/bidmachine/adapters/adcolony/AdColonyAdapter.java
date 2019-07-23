package io.bidmachine.adapters.adcolony;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import com.adcolony.sdk.*;
import io.bidmachine.AdsType;
import io.bidmachine.BidMachineAdapter;
import io.bidmachine.HeaderBiddingAdapter;
import io.bidmachine.HeaderBiddingCollectParamsCallback;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.TargetingInfo;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.utils.BMError;
import io.bidmachine.utils.Gender;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AdColonyAdapter extends BidMachineAdapter implements HeaderBiddingAdapter {

    private static HashSet<String> zonesCache = new HashSet<>();

    public AdColonyAdapter() {
        super("adcolony", AdColony.getSDKVersion(), new AdsType[]{AdsType.Interstitial, AdsType.Rewarded});
    }

    @Override
    public UnifiedFullscreenAd createInterstitial() {
        return new AdColonyFullscreenAd(false);
    }

    @Override
    public UnifiedFullscreenAd createRewarded() {
        return new AdColonyFullscreenAd(true);
    }

    @Override
    public void collectHeaderBiddingParams(@NonNull Context context,
                                           @NonNull UnifiedAdRequestParams requestParams,
                                           @NonNull final HeaderBiddingCollectParamsCallback callback,
                                           @NonNull final Map<String, Object> config) {
        final long startTime = System.currentTimeMillis();
        String appId = (String) config.get("app_id");
        if (TextUtils.isEmpty(appId)) {
            callback.onCollectFail(BMError.requestError("App id not provided"));
            return;
        }
        assert appId != null;
        String zoneId = (String) config.get("zone_id");
        if (TextUtils.isEmpty(zoneId)) {
            callback.onCollectFail(BMError.requestError("Zone id not provided"));
            return;
        }
        assert zoneId != null;
        String storeId = (String) config.get("store_id");
        if (TextUtils.isEmpty(storeId)) {
            callback.onCollectFail(BMError.requestError("Store id not provided"));
            return;
        }
        assert storeId != null;
        if (zonesCache == null) {
            zonesCache = new HashSet<>();
        }
        zonesCache.add(zoneId);
        AdColony.configure(
                (Application) context.getApplicationContext(),
                createAppOptions(context, requestParams, storeId),
                appId,
                zonesCache.toArray(new String[0]));

        final Map<String, String> params = new HashMap<>();
        params.put("app_id", appId);
        params.put("zone_id", zoneId);

        AdColonyZone zone = AdColony.getZone(zoneId);
        if (zone != null && zone.isValid()) {
            callback.onCollectFinished(params);
        } else {
            AdColony.requestInterstitial(zoneId, new AdColonyInterstitialListener() {
                @Override
                public void onRequestFilled(AdColonyInterstitial adColonyInterstitial) {
                    callback.onCollectFinished(params);
                    Log.e("AdColony", "configureTime (success): " + (System.currentTimeMillis() - startTime));
                }

                @Override
                public void onRequestNotFilled(AdColonyZone zone) {
                    callback.onCollectFail(BMError.NoContent);
                    Log.e("AdColony", "configureTime (fail): " + (System.currentTimeMillis() - startTime));
                }
            }, createAdOptions(requestParams));
        }
    }

    private static AdColonyAppOptions createAppOptions(@NonNull Context context,
                                                       @NonNull UnifiedAdRequestParams adRequestParams,
                                                       @NonNull String storeId) {
        DataRestrictions dataRestrictions = adRequestParams.getDataRestrictions();
        TargetingInfo targetingInfo = adRequestParams.getTargetingParams();
        AdColonyAppOptions options = AdColony.getAppOptions();
        if (options == null) {
            options = new AdColonyAppOptions();
            AdColony.setAppOptions(options);
        }
        String userId = targetingInfo.getUserId();
        if (userId != null) {
            options.setUserID(userId);
        }
        options.setOriginStore(storeId);
        try {
            options.setAppVersion(
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (dataRestrictions.isUserInGdprScope()) {
            options.setOption("explicit_consent_given", true);
            options.setOption("consent_response", dataRestrictions.isUserHasConsent());
        }
        options.setTestModeEnabled(adRequestParams.isTestMode());
        return options;
    }

    static AdColonyAdOptions createAdOptions(UnifiedAdRequestParams adRequestParams) {
        TargetingInfo targetingInfo = adRequestParams.getTargetingParams();
        AdColonyUserMetadata metadata = new AdColonyUserMetadata();
        Integer age = targetingInfo.getUserAge();
        if (age != null) {
            metadata.setUserAge(age);
        }
        Gender gender = targetingInfo.getGender();
        if (gender != null) {
            switch (gender) {
                case Male:
                    metadata.setUserGender(AdColonyUserMetadata.USER_MALE);
                    break;
                case Female:
                    metadata.setUserGender(AdColonyUserMetadata.USER_FEMALE);
                    break;
            }
        }
        String zip = targetingInfo.getZip();
        if (zip != null) {
            metadata.setUserZipCode(zip);
        }
        Location location = targetingInfo.getDeviceLocation();
        if (location != null) {
            metadata.setUserLocation(location);
        }
        return new AdColonyAdOptions().setUserMetadata(metadata);
    }

}
