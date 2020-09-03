package io.bidmachine.interstitial;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class InterstitialMraidRequestTest extends InterstitialRequestTest {

    @Override
    protected PlacementDisplayBuilder createPlacementDisplayBuilder() {
        return new MraidDisplayBuilder().setSize(320, 400);
    }
}
