package io.bidmachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import io.bidmachine.AdRequest.AdRequestListener;
import io.bidmachine.models.AuctionResult;
import io.bidmachine.utils.BMError;

public class BidMachineFetcher {

    public static final String KEY_ID = "bm_id";
    public static final String KEY_PRICE = "bm_pf";

    private static final BigDecimal DEF_PRICE_ROUNDING = new BigDecimal("0.01");
    private static final RoundingMode DEF_PRICE_ROUNDING_MODE = RoundingMode.CEILING;

    @VisibleForTesting
    static BigDecimal priceRounding = DEF_PRICE_ROUNDING;
    @VisibleForTesting
    static RoundingMode priceRoundingMode = DEF_PRICE_ROUNDING_MODE;

    @VisibleForTesting
    static EnumMap<AdsType, Map<String, AdRequest>> cachedRequests = new EnumMap<>(AdsType.class);

    public static void setPriceRounding(double rounding) {
        setPriceRounding(rounding, DEF_PRICE_ROUNDING_MODE);
    }

    public static void setPriceRounding(double rounding, RoundingMode roundingMode) {
        if (roundingMode == RoundingMode.UNNECESSARY) {
            throw new IllegalArgumentException("Invalid rounding mode");
        }
        priceRounding = new BigDecimal(String.valueOf(rounding));
        priceRoundingMode = roundingMode;
    }

    @Nullable
    @SuppressWarnings({"unchecked"})
    public static Map<String, String> fetch(@NonNull AdRequest adRequest) {
        AuctionResult auctionResult = adRequest.getAuctionResult();
        if (auctionResult == null) {
            return null;
        }
        final String id = auctionResult.getId();
        final AdsType adsType = adRequest.getType();
        adRequest.addListener(new AdRequestListener() {
            @Override
            public void onRequestSuccess(@NonNull AdRequest adRequest,
                                         @NonNull AuctionResult auctionResult) {
                //ignore
            }

            @Override
            public void onRequestFailed(@NonNull AdRequest adRequest, @NonNull BMError bmError) {
                //ignore
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
        Map<String, String> result = new HashMap<>();
        result.put(KEY_ID, id);
        result.put(KEY_PRICE, roundPrice(auctionResult.getPrice()));
        return result;
    }

    @Nullable
    public static AdRequest release(@NonNull AdRequest adRequest) {
        AuctionResult auctionResult = adRequest.getAuctionResult();
        return auctionResult != null
                ? release(adRequest.getType(), auctionResult.getId())
                : null;
    }

    @Nullable
    public static AdRequest release(@NonNull AdsType adsType,
                                    @NonNull Map<String, String> fetchedParams) {
        String requestId = fetchedParams.get(KEY_ID);
        return release(adsType, requestId);
    }

    @Nullable
    public static AdRequest release(@NonNull AdsType adsType, @Nullable String requestId) {
        if (TextUtils.isEmpty(requestId)) {
            return null;
        }
        synchronized (BidMachineFetcher.class) {
            Map<String, AdRequest> cached = cachedRequests.get(adsType);
            return cached != null ? cached.remove(requestId) : null;
        }
    }

    public static String roundPrice(double price) {
        BigDecimal value = new BigDecimal(String.valueOf(price));
        BigDecimal roundedValue = priceRounding.signum() == 0
                ? value
                : (value.divide(priceRounding, 0, priceRoundingMode)).multiply(priceRounding);
        return roundedValue.setScale(priceRounding.scale(), RoundingMode.HALF_UP).toString();
    }

    public static void resetPriceRounding() {
        priceRounding = DEF_PRICE_ROUNDING;
        priceRoundingMode = DEF_PRICE_ROUNDING_MODE;
    }

}
