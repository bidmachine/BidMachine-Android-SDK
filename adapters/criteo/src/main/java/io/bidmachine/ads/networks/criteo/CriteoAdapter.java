package io.bidmachine.ads.networks.criteo;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.criteo.publisher.Criteo;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.model.AdUnit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.bidmachine.AdRequest;
import io.bidmachine.AdsType;
import io.bidmachine.BidMachine;
import io.bidmachine.ContextProvider;
import io.bidmachine.HeaderBiddingAdRequestParams;
import io.bidmachine.HeaderBiddingAdapter;
import io.bidmachine.HeaderBiddingCollectParamsCallback;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfigParams;
import io.bidmachine.core.AdapterLogger;
import io.bidmachine.models.AuctionResult;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.utils.BMError;

class CriteoAdapter extends NetworkAdapter implements HeaderBiddingAdapter {

    CriteoAdapter() {
        super("criteo",
              BuildConfig.ADAPTER_SDK_VERSION_NAME,
              BuildConfig.ADAPTER_VERSION_NAME,
              new AdsType[]{AdsType.Banner, AdsType.Interstitial});
        BidMachine.registerAdRequestListener(new AdRequest.AdRequestListener() {
            @Override
            public void onRequestSuccess(@NonNull AdRequest adRequest,
                                         @NonNull AuctionResult auctionResult) {
                if (!isCriteoNetwork(getKey(), auctionResult)) {
                    CriteoBidTokenStorage.takeBid(adRequest);
                }
            }

            @Override
            public void onRequestFailed(@NonNull AdRequest adRequest, @NonNull BMError error) {
                CriteoBidTokenStorage.takeBid(adRequest);
            }

            @Override
            public void onRequestExpired(@NonNull AdRequest adRequest) {
                CriteoBidTokenStorage.takeBid(adRequest);
            }

            private boolean isCriteoNetwork(@Nullable String networkKey,
                                            @Nullable AuctionResult auctionResult) {
                if (auctionResult == null || TextUtils.isEmpty(networkKey)) {
                    return false;
                }
                assert networkKey != null;
                return networkKey.equals(auctionResult.getNetworkKey());
            }
        });
    }

    @Override
    public UnifiedBannerAd createBanner() {
        return new CriteoBanner();
    }

    @Override
    public UnifiedFullscreenAd createInterstitial() {
        return new CriteoInterstitial();
    }

    @Override
    protected void onInitialize(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParams adRequestParams,
                                @NonNull NetworkConfigParams networkConfigParams) throws Throwable {
        super.onInitialize(contextProvider, adRequestParams, networkConfigParams);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            AdapterLogger.logError(getKey(), "Initialize failed: minSdkVersion for Criteo is 16");
            return;
        }
        if (isInitialized()) {
            return;
        }
        Map<String, String> networkParams = networkConfigParams.obtainNetworkParams();
        if (networkParams == null) {
            AdapterLogger.logError(getKey(), "Initialize failed: network parameters not found");
            return;
        }
        String publisherId = networkParams.get(CriteoConfig.PUBLISHER_ID);
        if (TextUtils.isEmpty(publisherId)) {
            AdapterLogger.logError(getKey(),
                                   String.format("Initialize failed: %s not provided",
                                                 CriteoConfig.PUBLISHER_ID));
            return;
        }
        assert publisherId != null;
        List<AdUnit> adUnitList = CriteoAdUnitStorage.extractAdUnits(networkConfigParams);
        if (adUnitList == null || adUnitList.size() == 0) {
            AdapterLogger.logError(getKey(), "Initialize failed: AdUnits not found");
            return;
        }
        configure(contextProvider.getContext(),
                  publisherId,
                  adUnitList,
                  adRequestParams.isTestMode());
    }

    @Override
    public void collectHeaderBiddingParams(@NonNull ContextProvider contextProvider,
                                           @NonNull UnifiedAdRequestParams adRequestParams,
                                           @NonNull HeaderBiddingAdRequestParams hbAdRequestParams,
                                           @NonNull HeaderBiddingCollectParamsCallback collectCallback,
                                           @NonNull Map<String, String> mediationConfig) throws Throwable {
        if (!isInitialized()) {
            collectCallback.onCollectFail(BMError.adapterNotInitialized());
            return;
        }
        String adUnitId = mediationConfig.get(CriteoConfig.AD_UNIT_ID);
        if (TextUtils.isEmpty(adUnitId)) {
            collectCallback.onCollectFail(BMError.adapterGetsParameter(CriteoConfig.AD_UNIT_ID));
            return;
        }
        assert adUnitId != null;
        AdUnit adUnit = CriteoAdUnitStorage.getAdUnit(adUnitId);
        if (adUnit == null) {
            collectCallback.onCollectFail(BMError.adapterGetsParameter("AdUnit"));
            return;
        }
        Criteo criteo = Criteo.getInstance();
        criteo.loadBid(adUnit, bid -> {
            if (bid != null) {
                CriteoBidTokenStorage.storeBid(adRequestParams.getAdRequest(), bid);

                Map<String, String> params = new HashMap<>();
                params.put(CriteoConfig.AD_UNIT_ID, adUnitId);
                params.put(CriteoConfig.PRICE, String.valueOf(bid.getPrice()));
                collectCallback.onCollectFinished(params);
            } else {
                collectCallback.onCollectFail(BMError.adapterGetsParameter("Bid"));
            }
        });
    }

    private boolean isInitialized() {
        try {
            return Criteo.getInstance() != null;
        } catch (Throwable t) {
            return false;
        }
    }

    private void configure(@NonNull Context context,
                           @NonNull String publisherId,
                           @NonNull List<AdUnit> adUnitList,
                           boolean isTestMode) {
        try {
            Application application = (Application) context.getApplicationContext();
            if (application != null) {
                new Criteo.Builder(application, publisherId)
                        .debugLogsEnabled(isTestMode)
                        .adUnits(adUnitList)
                        .init();
            } else {
                AdapterLogger.logError(getKey(), "Initialize failed: application is null");
            }
        } catch (Throwable t) {
            AdapterLogger.logThrowable(t);
        }
    }

    static BMError mapError(CriteoErrorCode criteoErrorCode) {
        switch (criteoErrorCode) {
            case ERROR_CODE_NO_FILL:
                return BMError.noFill();
            case ERROR_CODE_NETWORK_ERROR:
                return BMError.NoConnection;
            default:
                return BMError.internal("Unknown error");
        }
    }

}