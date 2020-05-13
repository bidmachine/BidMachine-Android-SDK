package io.bidmachine.app_event;

import android.content.Context;
import android.support.annotation.NonNull;

import io.bidmachine.BidMachine;

public class BMAdManager {

    public static void initialize(@NonNull Context context, @NonNull String sellerId) {
        BidMachine.initialize(context, sellerId);
    }

}