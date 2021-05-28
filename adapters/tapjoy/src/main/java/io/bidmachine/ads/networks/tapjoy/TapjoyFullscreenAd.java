package io.bidmachine.ads.networks.tapjoy;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tapjoy.TJPlacement;
import com.tapjoy.Tapjoy;
import com.tapjoy.TapjoyAuctionFlags;

import java.util.HashMap;

import io.bidmachine.BidMachine;
import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

public class TapjoyFullscreenAd extends UnifiedFullscreenAd {

    @Nullable
    private TJPlacement tjPlacement;

    @Override
    public void load(@NonNull ContextProvider context,
                     @NonNull UnifiedFullscreenAdCallback callback,
                     @NonNull UnifiedFullscreenAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) throws Throwable {
        Activity activity = context.getActivity();
        if (activity == null) {
            callback.log("Activity not provided");
            callback.onAdLoadFailed(BMError.Internal);
            return;
        }
        String placementName = mediationParams.getString(TapjoyConfig.KEY_PLACEMENT_NAME);
        String auctionId = mediationParams.getString(TapjoyAuctionFlags.AUCTION_ID);
        String auctionData = mediationParams.getString(TapjoyAuctionFlags.AUCTION_DATA);

        Tapjoy.setActivity(activity);
        TapjoyFullscreenAdListener listener = new TapjoyFullscreenAdListener(callback);
        tjPlacement = Tapjoy.getLimitedPlacement(placementName, listener);
        tjPlacement.setVideoListener(listener);
        tjPlacement.setMediationName(BidMachine.NAME);
        tjPlacement.setAdapterVersion(BuildConfig.VERSION_NAME);

        HashMap<String, String> auctionParams = new HashMap<>();
        auctionParams.put(TapjoyAuctionFlags.AUCTION_ID, auctionId);
        auctionParams.put(TapjoyAuctionFlags.AUCTION_DATA, auctionData);
        tjPlacement.setAuctionData(auctionParams);

        tjPlacement.requestContent();
    }

    @Override
    public void show(@NonNull Context context,
                     @NonNull UnifiedFullscreenAdCallback callback) throws Throwable {
        if (tjPlacement != null && tjPlacement.isContentReady()) {
            tjPlacement.showContent();
        } else {
            callback.onAdShowFailed(BMError.NotLoaded);
        }
    }

    @Override
    public void onDestroy() {
        if (tjPlacement != null) {
            tjPlacement.setVideoListener(null);
            tjPlacement = null;
        }
    }

}