package io.bidmachine.banner;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.runner.RunWith;

import io.bidmachine.BaseViewAdRequestTest;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class BannerRequestTest extends BaseViewAdRequestTest<BannerAd, BannerRequest> {

    protected BannerSize size = BannerSize.Size_320x50;

    @Override
    protected BannerAd createAd() {
        return new BannerAd(activityTestRule.getActivity());
    }

    @Override
    protected BannerRequest createAdRequest() {
        return new BannerRequest.Builder().setSize(size).build();
    }

    @Override
    protected PlacementDisplayBuilder createPlacementDisplayBuilder() {
        return new MraidDisplayBuilder().setSize(size.width, size.height);
    }

}
