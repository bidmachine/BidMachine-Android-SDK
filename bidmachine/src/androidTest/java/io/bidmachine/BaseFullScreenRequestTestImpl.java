package io.bidmachine;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.bidmachine.protobuf.ErrorReason;
import io.bidmachine.utils.BMError;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@LargeTest
public abstract class BaseFullScreenRequestTestImpl<AdType extends FullScreenAd, AdRequestType extends AdRequest>
        extends BaseRequestTestImpl<AdType, AdRequestType> implements FullScreenAdRequestTests {

    protected ResultState rewardedState = new ResultState();

    @Test
    @Override
    public void testShownFailedNotLoaded() {
        AdType ad = createAd();
        ad.setListener(this);
        testShown(ad, false);
        assertEquals(BMError.ERROR_NOT_LOADED, shownFailState.getError().getCode());
    }

    @Test
    @Override
    public void testShowFailedNoConnection() {
        try {
            AdType ad = createAd();
            ad.setListener(this);
            testLoad(ad, createAdRequest(), buildResponse(), null, true);
            setNetworkState(false);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            testShown(ad, false);
            assertEquals(ErrorReason.ERROR_REASON_NO_CONNECTION_VALUE,
                    shownFailState.getError().getCode());
        } finally {
            setNetworkState(true);
        }
    }

    //    @Test
    @Override
    public void testClosed() {
//        AdType ad = createAd();
//        AdRequestType adRequest = createAdRequest();
//        testLoad(ad, adRequest, buildResponse(new ResponseBuildCallback() {
//            @Override
//            void onCreateAd(Ad.Builder adBuilder) {
//                super.onCreateAd(adBuilder);
//                AdExtension adExtension = AdExtension.newBuilder()
//                        .setViewabilityTimeThreshold(3000)
//                        .build();
//                adBuilder.addExt(Any.pack(adExtension));
//            }
//        }), null, true);
//
//        testShown(ad, true);
//        testImpression(500, false);
//        testClose(ad);
//
//        assertTrue(closedState.getState());
    }

    @Override
    public void onAdRewarded(@NonNull AdType ad) {
        super.onAdRewarded(ad);
        rewardedState.setState(true);
    }
}
