package io.bidmachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.google.android.gms.ads.doubleclick.PublisherAdRequest;

import java.util.HashMap;
import java.util.Map;

import io.bidmachine.ads.networks.mraid.MraidAdapter;
import io.bidmachine.ads.networks.vast.VastAdapter;
import io.bidmachine.models.AuctionResult;

public class BidMachineHelper {

    public static final String AD_TYPE_DISPLAY = "display";
    public static final String AD_TYPE_VIDEO = "video";

    public static final class AdManager {

        @NonNull
        public static PublisherAdRequest.Builder createPublisherAdRequestBuilder(@NonNull AdRequest adRequest) {
            PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
            fillPublisherAdRequestBuilder(builder, adRequest);
            return builder;
        }

        public static void fillPublisherAdRequestBuilder(@NonNull PublisherAdRequest.Builder builder,
                                                         @NonNull AdRequest adRequest) {
            Map<String, String> result = toMap(adRequest);
            for (Map.Entry<String, String> entry : result.entrySet()) {
                builder.addCustomTargeting(entry.getKey(), entry.getValue());
            }
        }

    }

    public static final class MoPub {

        @NonNull
        public static String toKeywords(@NonNull AdRequest adRequest) {
            Map<String, String> result = toMap(adRequest);
            return toKeywords(result);
        }

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

    @NonNull
    static Map<String, String> toMap(@NonNull AdRequest adRequest) {
        Map<String, String> result = new HashMap<>();
        AuctionResult auctionResult = adRequest.getAuctionResult();
        if (auctionResult == null) {
            return result;
        }
        result.put(BidMachineFetcher.KEY_ID, auctionResult.getId());
        result.put(BidMachineFetcher.KEY_PRICE,
                   BidMachineFetcher.roundPrice(auctionResult.getPrice()));
        result.put(BidMachineFetcher.KEY_NETWORK_KEY, auctionResult.getNetworkKey());
        String adType = identifyAdType(auctionResult.getNetworkKey());
        if (adType != null) {
            result.put(BidMachineFetcher.KEY_AD_TYPE, adType);
        }
        return result;
    }

    @Nullable
    @VisibleForTesting
    static String identifyAdType(@NonNull String networkName) {
        switch (networkName) {
            case MraidAdapter.KEY:
                return AD_TYPE_DISPLAY;
            case VastAdapter.KEY:
                return AD_TYPE_VIDEO;
        }
        return null;
    }

}