package io.bidmachine;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import io.bidmachine.AdRequest.AdRequestListener;
import io.bidmachine.core.Logger;
import io.bidmachine.models.AuctionResult;
import io.bidmachine.utils.BMError;

public class BidMachineFetcher {

    public static final String KEY_ID = "bm_id";
    public static final String KEY_PRICE = "bm_pf";
    public static final String KEY_AD_TYPE = "bm_ad_type";
    public static final String KEY_NETWORK_KEY = "bm_network_key";

    public static final String AD_TYPE_DISPLAY = "display";
    public static final String AD_TYPE_VIDEO = "video";
    public static final String AD_TYPE_NATIVE = "native";

    private static final String TAG = BidMachineFetcher.class.getSimpleName();
    private static final BigDecimal PRICE_ROUNDING = new BigDecimal("0.01");
    private static final RoundingMode PRICE_ROUNDING_MODE = RoundingMode.CEILING;

    @VisibleForTesting
    static EnumMap<AdsType, Map<String, AdRequest>> cachedRequests = new EnumMap<>(AdsType.class);

    @Nullable
    @SuppressWarnings({"unchecked"})
    public static Map<String, String> fetch(@NonNull AdRequest adRequest) {
        Logger.log(TAG, String.format("fetch - %s", adRequest));

        final Map<String, String> result = toMap(adRequest);
        final String id = result.get(KEY_ID);
        if (TextUtils.isEmpty(id)) {
            return null;
        }
        assert id != null;
        final AdsType adsType = adRequest.getType();
        adRequest.addListener(new AdRequestListener() {
            @Override
            public void onRequestSuccess(@NonNull AdRequest adRequest,
                                         @NonNull AuctionResult auctionResult) {
                // ignore
            }

            @Override
            public void onRequestFailed(@NonNull AdRequest adRequest, @NonNull BMError bmError) {
                // ignore
            }

            @Override
            public void onRequestExpired(@NonNull AdRequest adRequest) {
                release(adRequest);
            }
        });
        synchronized (BidMachineFetcher.class) {
            Map<String, AdRequest> cached = cachedRequests.get(adsType);
            if (cached == null) {
                cached = new HashMap<>();
                cachedRequests.put(adsType, cached);
            }
            cached.put(id, adRequest);
        }
        return result;
    }

    @Nullable
    public static <T extends AdRequest> T release(@NonNull T adRequest) {
        AuctionResult auctionResult = adRequest.getAuctionResult();
        if (auctionResult != null) {
            return release(adRequest.getType(), auctionResult.getId());
        }
        return null;
    }

    @Nullable
    public static <T extends AdRequest> T release(@NonNull AdsType adsType,
                                                  @NonNull Map<String, String> fetchedParams) {
        String requestId = fetchedParams.get(KEY_ID);
        return release(adsType, requestId);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends AdRequest> T release(@NonNull AdsType adsType,
                                                  @Nullable String requestId) {
        if (TextUtils.isEmpty(requestId)) {
            return null;
        }
        Logger.log(TAG, String.format("release - %s", requestId));

        synchronized (BidMachineFetcher.class) {
            Map<String, AdRequest> cached = cachedRequests.get(adsType);
            if (cached != null) {
                try {
                    return (T) cached.remove(requestId);
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        }
    }

    @NonNull
    public static Map<String, String> toMap(@NonNull AdRequest adRequest) {
        Logger.log(TAG, String.format("toMap - %s", adRequest));

        Map<String, String> result = new HashMap<>();
        AuctionResult auctionResult = adRequest.getAuctionResult();
        if (auctionResult == null) {
            return result;
        }
        result.put(BidMachineFetcher.KEY_ID, auctionResult.getId());
        result.put(BidMachineFetcher.KEY_PRICE,
                   BidMachineFetcher.roundPrice(auctionResult.getPrice()));
        result.put(BidMachineFetcher.KEY_NETWORK_KEY, auctionResult.getNetworkKey());
        String adType = identifyAdType(auctionResult.getCreativeFormat());
        if (adType != null) {
            result.put(BidMachineFetcher.KEY_AD_TYPE, adType);
        }
        result.putAll(auctionResult.getCustomParams());
        return result;
    }

    @Nullable
    @VisibleForTesting
    static String identifyAdType(@Nullable CreativeFormat creativeFormat) {
        if (creativeFormat == null) {
            return null;
        }
        switch (creativeFormat) {
            case Banner:
                return AD_TYPE_DISPLAY;
            case Video:
                return AD_TYPE_VIDEO;
            case Native:
                return AD_TYPE_NATIVE;
            default:
                return null;
        }
    }

    private static String roundPrice(double price) {
        BigDecimal value = new BigDecimal(String.valueOf(price));
        BigDecimal roundedValue = PRICE_ROUNDING.signum() == 0
                ? value
                : (value.divide(PRICE_ROUNDING, 0, PRICE_ROUNDING_MODE)).multiply(PRICE_ROUNDING);
        return roundedValue.setScale(PRICE_ROUNDING.scale(), RoundingMode.HALF_UP).toString();
    }

}