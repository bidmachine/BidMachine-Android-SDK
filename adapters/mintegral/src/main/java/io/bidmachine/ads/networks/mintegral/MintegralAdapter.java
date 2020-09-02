package io.bidmachine.ads.networks.mintegral;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mintegral.msdk.MIntegralConstans;
import com.mintegral.msdk.MIntegralSDK;
import com.mintegral.msdk.MIntegralUser;
import com.mintegral.msdk.mtgbid.out.BidManager;
import com.mintegral.msdk.out.MIntegralSDKFactory;
import com.mintegral.msdk.out.MTGConfiguration;

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
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.utils.BMError;
import io.bidmachine.utils.Gender;

public class MintegralAdapter extends NetworkAdapter implements HeaderBiddingAdapter {

    static final Handler handler = new Handler(Looper.getMainLooper());

    private boolean isInitialized = false;

    MintegralAdapter() {
        super("mintegral",
              MTGConfiguration.SDK_VERSION,
              BuildConfig.VERSION_NAME,
              new AdsType[]{AdsType.Interstitial, AdsType.Rewarded});
    }

    @Override
    public UnifiedFullscreenAd createInterstitial() {
        return new MintegralVideo();
    }

    @Override
    public UnifiedFullscreenAd createRewarded() {
        return new MintegralRewarded();
    }

    @Override
    protected void onInitialize(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParams adRequestParams,
                                @NonNull NetworkConfigParams networkConfigParams) {
        super.onInitialize(contextProvider, adRequestParams, networkConfigParams);
        syncState(contextProvider, adRequestParams, null);
    }

    @Override
    public void collectHeaderBiddingParams(@NonNull final ContextProvider contextProvider,
                                           @NonNull UnifiedAdRequestParams adRequestParams,
                                           @NonNull HeaderBiddingAdRequestParams hbAdRequestParams,
                                           @NonNull final HeaderBiddingCollectParamsCallback collectCallback,
                                           @NonNull Map<String, String> mediationConfig) {
        final String appId = mediationConfig.get(MintegralConfig.KEY_APP_ID);
        if (TextUtils.isEmpty(appId)) {
            collectCallback.onCollectFail(BMError.requestError("app_id not provided"));
            return;
        }
        final String apiKey = mediationConfig.get(MintegralConfig.KEY_API_KEY);
        if (TextUtils.isEmpty(apiKey)) {
            collectCallback.onCollectFail(BMError.requestError("api_key not provided"));
            return;
        }
        final String unitId = mediationConfig.get(MintegralConfig.KEY_UNIT_ID);
        if (TextUtils.isEmpty(unitId)) {
            collectCallback.onCollectFail(BMError.requestError("unit_id not provided"));
            return;
        }
        syncState(contextProvider, adRequestParams, new SyncCallback() {
            @Override
            public void onSyncFinished() {
                synchronized (MintegralAdapter.class) {
                    if (!isInitialized) {
                        MIntegralSDK sdk = MIntegralSDKFactory.getMIntegralSDK();
                        Map<String, String> map = sdk.getMTGConfigurationMap(appId, apiKey);
                        sdk.init(map, contextProvider.getContext().getApplicationContext());
                        isInitialized = true;
                    }
                }
                // Need use BidManager.getBuyerUid after initialize, because starting from target
                // api 27 or higher ads not load
                final String buyerUid = BidManager.getBuyerUid(contextProvider.getContext());
                if (TextUtils.isEmpty(buyerUid)) {
                    collectCallback.onCollectFail(BMError.requestError("buyerUid getting failed"));
                    return;
                }
                final HashMap<String, String> params = new HashMap<>();
                params.put(MintegralConfig.KEY_APP_ID, appId);
                params.put(MintegralConfig.KEY_API_KEY, apiKey);
                params.put(MintegralConfig.KEY_UNIT_ID, unitId);
                params.put(MintegralConfig.KEY_BUYER_UID, buyerUid);
                collectCallback.onCollectFinished(params);
            }
        });
    }

    private void syncState(@NonNull ContextProvider context,
                           @NonNull UnifiedAdRequestParams adRequestParams,
                           @Nullable final SyncCallback syncCallback) {
        // The flag(INIT_UA_IN) is responsible for Appwall adtype initialization.
        // We don't have implementation. Must be false.
        MIntegralConstans.INIT_UA_IN = false;
        TargetingInfo targetingInfo = adRequestParams.getTargetingParams();
        DataRestrictions dataRestrictions = adRequestParams.getDataRestrictions();
        if (dataRestrictions.isUserInGdprScope()) {
            MIntegralSDKFactory.getMIntegralSDK().setUserPrivateInfoType(
                    context.getContext(),
                    MIntegralConstans.AUTHORITY_ALL_INFO,
                    dataRestrictions.isUserGdprProtected()
                            ? MIntegralConstans.IS_SWITCH_OFF
                            : MIntegralConstans.IS_SWITCH_ON);
        }
        final MIntegralUser user = new MIntegralUser();
        Integer age = targetingInfo.getUserAge();
        if (age != null) {
            user.setAge(age);
        }
        // 1 - male, 2 - fmale(int); set up not mandatory if unknown
        Gender gender = targetingInfo.getGender();
        if (gender == Gender.Male) {
            user.setGender(1);
        } else if (gender == Gender.Female) {
            user.setGender(2);
        }
        Location location = targetingInfo.getDeviceLocation();
        if (location != null) {
            user.setLat(location.getLatitude());
            user.setLng(location.getLongitude());
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                MIntegralSDKFactory.getMIntegralSDK().reportUser(user);
                if (syncCallback != null) {
                    syncCallback.onSyncFinished();
                }
            }
        });
    }

    private interface SyncCallback {
        void onSyncFinished();
    }

    static BMError mapError(@Nullable String msg) {
        if (msg != null) {
            msg = msg.toLowerCase();
            if (msg.contains("no fill") || msg.contains("no ads")) {
                return BMError.NoContent;
            } else if (msg.contains("timeout")) {
                return BMError.Connection;
            }
        }
        return BMError.Internal;
    }
}
