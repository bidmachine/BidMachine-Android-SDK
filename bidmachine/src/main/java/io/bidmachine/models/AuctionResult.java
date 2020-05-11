package io.bidmachine.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

import io.bidmachine.CreativeFormat;

public interface AuctionResult {

    /**
     * @return Winner Bid Id provided in request
     */
    @NonNull
    String getId();

    /**
     * @return Winner advertising source name
     */
    @Nullable
    String getDemandSource();

    /**
     * @return Winner price as CPM
     */
    double getPrice();

    /**
     * @return ID of Price Floor
     */
    @Nullable
    String getDeal();

    /**
     * @return ID of the buyer seat who made a bid
     */
    String getSeat();

    /**
     * @return Winner creative Id
     */
    @NonNull
    String getCreativeId();

    /**
     * @return Winner Campaign ID or other identifier of brand-related ads.
     */
    @Nullable
    String getCid();

    /**
     * @return Winner Advertiser domain; top two levels only (e.g.: "ford.com").
     */
    @Nullable
    String[] getAdDomains();

    /**
     * @return Key of winner network. This network would be loaded.
     */
    @NonNull
    String getNetworkKey();

    /**
     * @return {@link CreativeFormat} of winner ad
     */
    @Nullable
    CreativeFormat getCreativeFormat();

    /**
     * @return Map that contains additional information of response
     */
    @NonNull
    Map<String, String> getCustomParams();

}