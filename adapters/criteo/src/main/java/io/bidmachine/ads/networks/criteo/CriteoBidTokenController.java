package io.bidmachine.ads.networks.criteo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.criteo.publisher.Bid;

import java.util.Map;
import java.util.WeakHashMap;

import io.bidmachine.AdRequest;

class CriteoBidTokenController {

    @VisibleForTesting
    static final Map<AdRequest, Bid> bidMap = new WeakHashMap<>();

    static synchronized void storeBid(@Nullable AdRequest adRequest, @NonNull Bid bid) {
        if (adRequest == null) {
            return;
        }
        bidMap.put(adRequest, bid);
    }

    @Nullable
    static synchronized Bid takeBid(@Nullable AdRequest adRequest) {
        if (adRequest == null) {
            return null;
        }
        return bidMap.remove(adRequest);
    }

}