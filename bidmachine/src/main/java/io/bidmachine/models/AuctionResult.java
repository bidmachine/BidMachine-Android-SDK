package io.bidmachine.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

}