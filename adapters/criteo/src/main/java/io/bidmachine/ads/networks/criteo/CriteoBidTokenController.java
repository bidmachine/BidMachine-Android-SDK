package io.bidmachine.ads.networks.criteo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.criteo.publisher.BidToken;

import java.util.Map;
import java.util.WeakHashMap;

import io.bidmachine.AdRequest;

class CriteoBidTokenController {

    @VisibleForTesting
    static final Map<AdRequest, BidToken> bidTokenMap = new WeakHashMap<>();

    static synchronized void storeBidToken(@Nullable AdRequest adRequest,
                                           @NonNull BidToken bidToken) {
        if (adRequest == null) {
            return;
        }
        bidTokenMap.put(adRequest, bidToken);
    }

    @Nullable
    static synchronized BidToken takeBidToken(@Nullable AdRequest adRequest) {
        if (adRequest == null) {
            return null;
        }
        return bidTokenMap.remove(adRequest);
    }

}