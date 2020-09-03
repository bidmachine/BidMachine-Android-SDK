package io.bidmachine.interstitial;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class InterstitialVastRequestTest extends InterstitialRequestTest {

    @Override
    protected PlacementDisplayBuilder createPlacementDisplayBuilder() {
        return new VastVideoDisplayBuilder();
    }

    @Override
    protected void testClick(InterstitialAd ad) {
        new AwaitHelper() {
            @Override
            public boolean isReady() {
                return rewardedState.getState() != null;
            }
        }.start(10000);
        super.testClick(ad);
    }
}
