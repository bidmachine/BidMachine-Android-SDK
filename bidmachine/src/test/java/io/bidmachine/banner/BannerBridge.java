package io.bidmachine.banner;

import android.content.Context;

import androidx.annotation.NonNull;

import io.bidmachine.BidMachineAd;

public class BannerBridge {

    public static BidMachineAd createBannerAd(@NonNull Context context) {
        return new BannerAd(context);
    }

}
