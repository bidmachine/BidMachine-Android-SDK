package io.bidmachine.ads.networks;

import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdRegistration;
import com.amazon.device.ads.DTBAdCallback;
import com.amazon.device.ads.DTBAdRequest;
import com.amazon.device.ads.DTBAdResponse;
import com.amazon.device.ads.DTBAdSize;
import com.amazon.device.ads.MRAIDPolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.bidmachine.AdContentType;
import io.bidmachine.AdsType;
import io.bidmachine.ContextProvider;
import io.bidmachine.HeaderBiddingAdRequestParams;
import io.bidmachine.HeaderBiddingAdapter;
import io.bidmachine.HeaderBiddingCollectParamsCallback;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfigParams;
import io.bidmachine.ads.networks.amazon.BuildConfig;
import io.bidmachine.banner.BannerSize;
import io.bidmachine.core.AdapterLogger;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;
import io.bidmachine.utils.BMError;

class AmazonAdapter extends NetworkAdapter implements HeaderBiddingAdapter {

    AmazonAdapter() {
        super("amazon",
              BuildConfig.ADAPTER_SDK_VERSION_NAME,
              BuildConfig.ADAPTER_VERSION_NAME,
              new AdsType[]{AdsType.Banner, AdsType.Interstitial});
    }

    @Override
    public void setLogging(boolean enabled) throws Throwable {
        AdRegistration.enableLogging(enabled);
    }

    @Override
    protected void onInitialize(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParams adRequestParams,
                                @NonNull NetworkConfigParams networkConfigParams) throws Throwable {
        super.onInitialize(contextProvider, adRequestParams, networkConfigParams);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            AdapterLogger.logError(getKey(), "Initialize failed: minSdkVersion for Amazon is 19");
            return;
        }
        Map<String, String> mediationConfig = networkConfigParams.obtainNetworkParams();
        if (mediationConfig == null) {
            AdapterLogger.logError(getKey(), "Initialize failed: network parameters not found");
            return;
        }
        String appKey = mediationConfig.get(AmazonConfig.APP_KEY);
        if (TextUtils.isEmpty(appKey)) {
            AdapterLogger.logError(getKey(),
                                   String.format("Initialize failed: %s not provided",
                                                 AmazonConfig.APP_KEY));
            return;
        }
        assert appKey != null;

        initialize(contextProvider, adRequestParams, appKey);
        AdRegistration.setMRAIDSupportedVersions(new String[]{"1.0", "2.0"});
        AdRegistration.setMRAIDPolicy(MRAIDPolicy.CUSTOM);
    }

    @Override
    public void collectHeaderBiddingParams(@NonNull ContextProvider contextProvider,
                                           @NonNull UnifiedAdRequestParams adRequestParams,
                                           @NonNull HeaderBiddingAdRequestParams hbAdRequestParams,
                                           @NonNull final HeaderBiddingCollectParamsCallback collectCallback,
                                           @NonNull Map<String, String> mediationConfig) throws Throwable {
        if (!isInitialized()) {
            collectCallback.onCollectFail(BMError.adapterNotInitialized());
            return;
        }
        final String slotUuid = mediationConfig.get(AmazonConfig.SLOT_UUID);
        if (TextUtils.isEmpty(slotUuid)) {
            collectCallback.onCollectFail(BMError.adapterGetsParameter(AmazonConfig.SLOT_UUID));
            return;
        }
        final String appKey = mediationConfig.get(AmazonConfig.APP_KEY);
        if (TextUtils.isEmpty(appKey)) {
            collectCallback.onCollectFail(BMError.adapterGetsParameter(AmazonConfig.APP_KEY));
            return;
        }
        assert appKey != null;

        initialize(contextProvider, adRequestParams, appKey);

        final AdsType adsType = hbAdRequestParams.getAdsType();
        final AdContentType adContentType = hbAdRequestParams.getAdContentType();
        String usPrivacy = adRequestParams.getDataRestrictions().getUSPrivacyString();
        if (adsType == AdsType.Banner) {
            BannerSize bannerSize = ((UnifiedBannerAdRequestParams) adRequestParams).getBannerSize();
            AmazonLoader.forDisplay(collectCallback)
                    .withUsPrivacy(usPrivacy)
                    .load(new DTBAdSize(bannerSize.width, bannerSize.height, slotUuid));
        } else if (adsType == AdsType.Interstitial || adsType == AdsType.Rewarded) {
            if (adContentType == AdContentType.Video) {
                DisplayMetrics metrics = contextProvider.getContext()
                        .getResources()
                        .getDisplayMetrics();
                AmazonLoader.forVideo(collectCallback)
                        .withUsPrivacy(usPrivacy)
                        .load(new DTBAdSize.DTBVideo(metrics.widthPixels,
                                                     metrics.heightPixels,
                                                     slotUuid));
            } else {
                AmazonLoader.forDisplay(collectCallback)
                        .withUsPrivacy(usPrivacy)
                        .load(new DTBAdSize.DTBInterstitialAdSize(slotUuid));
            }
        } else {
            collectCallback.onCollectFail(BMError.adapter("Unsupported ads type"));
        }
    }

    private void initialize(@NonNull ContextProvider contextProvider,
                            @NonNull UnifiedAdRequestParams adRequestParams,
                            @NonNull String appKey) {
        AdRegistration.getInstance(appKey, contextProvider.getContext().getApplicationContext());
        AdRegistration.enableTesting(adRequestParams.isTestMode());
        DataRestrictions dataRestrictions = adRequestParams.getDataRestrictions();
        if (dataRestrictions != null) {
            AdRegistration.useGeoLocation(dataRestrictions.canSendGeoPosition());
        }
    }

    private boolean isInitialized() {
        return AdRegistration.isInitialized();
    }


    private static abstract class AmazonLoader {

        static AmazonLoader forDisplay(@NonNull HeaderBiddingCollectParamsCallback callback) {
            return new AmazonLoader(callback) {
                @Override
                void handleResponse(@NonNull DTBAdResponse adResponse,
                                    @NonNull Map<String, String> outMap) {
                    Map<String, List<String>> params = adResponse.getDefaultDisplayAdsRequestCustomParams();
                    for (Map.Entry<String, List<String>> entry : params.entrySet()) {
                        List<String> values = entry.getValue();
                        if (values != null) {
                            String value = values.get(0);
                            if (value != null) {
                                outMap.put(entry.getKey(), value);
                            }
                        }
                    }
                }
            };
        }

        static AmazonLoader forVideo(@NonNull HeaderBiddingCollectParamsCallback callback) {
            return new AmazonLoader(callback) {
                @Override
                void handleResponse(@NonNull DTBAdResponse adResponse,
                                    @NonNull Map<String, String> outMap) {
                    Map<String, String> params = adResponse.getDefaultVideoAdsRequestCustomParams();
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        String value = entry.getValue();
                        if (value != null) {
                            outMap.put(entry.getKey(), value);
                        }
                    }
                }
            };
        }

        private final HeaderBiddingCollectParamsCallback collectCallback;
        private String usPrivacy;

        private AmazonLoader(@NonNull HeaderBiddingCollectParamsCallback collectCallback) {
            this.collectCallback = collectCallback;
        }

        AmazonLoader withUsPrivacy(@Nullable String usPrivacy) {
            this.usPrivacy = usPrivacy;
            return this;
        }

        void load(@NonNull DTBAdSize size) {
            DTBAdRequest request = new DTBAdRequest();
            request.setSizes(size);
            if (!TextUtils.isEmpty(usPrivacy)) {
                request.putCustomTarget("us_privacy", usPrivacy);
            }
            request.loadAd(new DTBAdCallback() {
                @Override
                public void onFailure(@NonNull AdError adError) {
                    collectCallback.onCollectFail(mapError(adError));
                }

                @Override
                public void onSuccess(@NonNull DTBAdResponse dtbAdResponse) {
                    Map<String, String> resultMap = new HashMap<>();
                    handleResponse(dtbAdResponse, resultMap);
                    if (resultMap.isEmpty()) {
                        collectCallback.onCollectFail(BMError.adapter(
                                "Response returned empty parameters"));
                    } else {
                        collectCallback.onCollectFinished(resultMap);
                    }
                }
            });
        }

        abstract void handleResponse(@NonNull DTBAdResponse adResponse,
                                     @NonNull Map<String, String> outMap);

    }

    private static BMError mapError(@NonNull AdError error) {
        switch (error.getCode()) {
            case NO_FILL:
                return BMError.noFill();
            case NETWORK_ERROR:
                return BMError.NoConnection;
            case NETWORK_TIMEOUT:
                return BMError.TimeoutError;
            default:
                return BMError.internal("Unknown error");
        }
    }

}