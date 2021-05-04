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

    private static final BigDecimal DEF_PRICE_ROUNDING = new BigDecimal("0.01");
    private static final RoundingMode DEF_PRICE_ROUNDING_MODE = RoundingMode.CEILING;

    @VisibleForTesting
    static BigDecimal priceRounding = DEF_PRICE_ROUNDING;
    @VisibleForTesting
    static RoundingMode priceRoundingMode = DEF_PRICE_ROUNDING_MODE;

    @VisibleForTesting
    static EnumMap<AdsType, Map<String, AdRequest>> cachedRequests = new EnumMap<>(AdsType.class);

    @Deprecated
    public static void setPriceRounding(double rounding) {
        setPriceRounding(rounding, DEF_PRICE_ROUNDING_MODE);
    }

    @Deprecated
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

    @Deprecated
    public static String roundPrice(double price) {
        BigDecimal value = new BigDecimal(String.valueOf(price));
        BigDecimal roundedValue = priceRounding.signum() == 0
                ? value
                : (value.divide(priceRounding, 0, priceRoundingMode)).multiply(priceRounding);
        return roundedValue.setScale(priceRounding.scale(), RoundingMode.HALF_UP).toString();
    }

    @Deprecated
    public static void resetPriceRounding() {
        priceRounding = DEF_PRICE_ROUNDING;
        priceRoundingMode = DEF_PRICE_ROUNDING_MODE;
    }

    @NonNull
    public static Map<String, String> toMap(@NonNull AdRequest adRequest) {
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

    @Deprecated
    public static final class MoPub {

        @Deprecated
        @NonNull
        public static String toKeywords(@NonNull AdRequest adRequest) {
            Map<String, String> result = toMap(adRequest);
            return toKeywords(result);
        }

        @Deprecated
        @NonNull
        public static String toKeywords(@NonNull Map<String, String> result) {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, ?> entry : result.entrySet()) {
                if (builder.length() > 0) {
                    builder.append(",");
                }
                builder.append(entry.getKey())
                        .append(":")
                        .append(entry.getValue());
            }
            return builder.toString();
        }

    }

}