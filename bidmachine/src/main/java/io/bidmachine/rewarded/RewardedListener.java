package io.bidmachine.rewarded;

import io.bidmachine.AdFullScreenListener;
import io.bidmachine.AdListener;
import io.bidmachine.AdRewardedListener;

public interface RewardedListener extends AdListener<RewardedAd>, AdFullScreenListener<RewardedAd>, AdRewardedListener<RewardedAd> {

}