package io.bidmachine;

import android.support.annotation.NonNull;

public interface AdRewardedListener<AdType extends IAd> {

    /**
     * Called when Rewarded Ad was completed (e.g.: the video has been played to the end).
     * You can use this event to reward	user
     *
     * @param ad - Ad type {@link IAd}
     */
    void onAdRewarded(@NonNull AdType ad);

}