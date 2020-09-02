package io.bidmachine.interstitial;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.runner.RunWith;

import io.bidmachine.BaseFullScreenRequestTestImpl;

@RunWith(AndroidJUnit4.class)
@LargeTest
public abstract class InterstitialRequestTest
        extends BaseFullScreenRequestTestImpl<InterstitialAd, InterstitialRequest> {

    @Override
    protected InterstitialAd createAd() {
        return new InterstitialAd(activityTestRule.getActivity());
    }

    @Override
    protected InterstitialRequest createAdRequest() {
        return new InterstitialRequest.Builder().build();
    }

}
