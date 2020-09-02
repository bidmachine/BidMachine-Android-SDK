package io.bidmachine.banner;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.test.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.bidmachine.BidMachine;
import io.bidmachine.utils.BMError;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class BannerViewTest {

    @Rule
    public final ActivityTestRule<BannerViewActivity> activityRule =
            new ActivityTestRule<>(BannerViewActivity.class);

    private BannerView bannerView;

    @BeforeClass
    public static void setupClass() {
        BidMachine.initialize(InstrumentationRegistry.getContext(), "1");
    }

    @Before
    public void setup() {
        final BannerViewActivity activity = activityRule.getActivity();
        bannerView = activity.bannerView;
    }

    //TODO: add to base view test
    @Test
    public void success_load_show() throws InterruptedException {
        performBannerTest(new BannerRequest.Builder().setSize(BannerSize.Size_320x50).build(),
                true, true, 2);
    }

    @Test
    public void success_load_no_show_translation() throws InterruptedException {
        changeViewState(bannerView, new Runnable() {
            @Override
            public void run() {
                bannerView.setTranslationY(10000);
            }
        });
        performBannerTest(new BannerRequest.Builder().setSize(BannerSize.Size_320x50).build(),
                true, false, 2);
    }

    @Test
    public void fail_no_publisherId() throws InterruptedException {
        performBannerTest(new BannerRequest.Builder().setSize(BannerSize.Size_320x50).build(),
                false, false, 1);
    }

    private void performBannerTest(BannerRequest request,
                                   boolean expectedLoadResult,
                                   boolean expectedShowResult,
                                   int expectedInvocations)
            throws InterruptedException {
        final AtomicBoolean isLoaded = new AtomicBoolean(false);
        final AtomicBoolean isShown = new AtomicBoolean(false);
        final CountDownLatch lock = new CountDownLatch(expectedInvocations);
        bannerView.setListener(new SimpleBannerListener() {
            @Override
            public void onAdLoaded(@NonNull BannerView ad) {
                super.onAdLoaded(ad);
                isLoaded.set(true);
                lock.countDown();
            }

            @Override
            public void onAdShown(@NonNull BannerView ad) {
                super.onAdShown(ad);
                isShown.set(true);
                lock.countDown();
            }

            @Override
            public void onAdLoadFailed(@NonNull BannerView ad, @NonNull BMError error) {
                super.onAdLoadFailed(ad, error);
                lock.countDown();
            }
        });
        bannerView.load(request);
        lock.await(3, TimeUnit.SECONDS);
        assertEquals(expectedLoadResult, isLoaded.get());
        assertEquals(expectedShowResult, isShown.get());
    }

    private void changeViewState(View view, final Runnable action) throws InterruptedException {
        final CountDownLatch lock = new CountDownLatch(1);
        view.post(new Runnable() {
            @Override
            public void run() {
                action.run();
                lock.countDown();
            }
        });
        lock.await();
    }

}
