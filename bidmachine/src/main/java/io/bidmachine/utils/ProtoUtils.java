package io.bidmachine.utils;

import androidx.annotation.NonNull;

import com.explorestack.protobuf.adcom.Placement;

import io.bidmachine.banner.BannerSize;

public class ProtoUtils {

    public static boolean isBannerPlacement(@NonNull Placement placement,
                                            @NonNull BannerSize bannerSize) {
        Placement.DisplayPlacement displayPlacement = placement.getDisplay();
        return !displayPlacement.getInstl()
                && bannerSize.width == displayPlacement.getW()
                && bannerSize.height == displayPlacement.getH();
    }

    public static boolean isInterstitialPlacement(@NonNull Placement placement) {
        if (isRewardedPlacement(placement)) {
            return false;
        }
        Placement.DisplayPlacement displayPlacement = placement.getDisplay();
        if (displayPlacement != Placement.DisplayPlacement.getDefaultInstance()
                && displayPlacement.getInstl()) {
            return true;
        }
        return placement.getVideo() != Placement.VideoPlacement.getDefaultInstance();
    }

    public static boolean isRewardedPlacement(@NonNull Placement placement) {
        return placement.getReward();
    }

    public static boolean isNativePlacement(@NonNull Placement placement) {
        return placement.getDisplay().getNativefmt().getAssetCount() > 0;
    }

}