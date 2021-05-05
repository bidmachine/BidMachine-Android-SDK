package io.bidmachine.core;

import android.view.View;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

//TODO: Add more test cases; Optimize timings

@RunWith(AndroidJUnit4.class)
public class VisibilityTrackerTest {

    @Rule
    public ActivityTestRule<VisibilityTrackerActivity> activity =
            new ActivityTestRule<>(VisibilityTrackerActivity.class);

    private AtomicBoolean isShownCalled;
    private AtomicBoolean isFinishedCalled;

    @Before
    public void setup() {
        isShownCalled = new AtomicBoolean();
        isFinishedCalled = new AtomicBoolean();
    }

    @Test
    public void success_shown() throws InterruptedException {
        startTracking(true, 0);
    }

    @Test
    public void success_shown_finished() throws InterruptedException {
        startTracking(true, 2000);
    }

    @Test
    public void success_shown_delayed_translation() throws InterruptedException {
        final View view = activity.getActivity().trackingView;
        view.setTranslationY(10000);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setTranslationY(0);
            }
        }, 1000);
        startTracking(true, 0);
    }

    @Test
    public void success_shown_finished_delayed_translation() throws InterruptedException {
        final View view = activity.getActivity().trackingView;
        view.setTranslationY(10000);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setTranslationY(0);
            }
        }, 1000);
        startTracking(true, 2000);
    }

    @Test
    public void success_shown_skipped_finished_delayed_translation() throws InterruptedException {
        final View view = activity.getActivity().trackingView;
        view.setTranslationY(10000);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setTranslationY(0);
            }
        }, 500);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setTranslationY(10000);
            }
        }, 1000);
        startTracking(true, false, 2000, 3);
    }

    @Test
    public void success_visibility_gone_visible() throws Throwable {
        visibilityChangeTest(View.GONE, View.VISIBLE, true);
    }

    @Test
    public void success_visibility_invisible_visible() throws Throwable {
        visibilityChangeTest(View.INVISIBLE, View.VISIBLE, true);
    }

    @Test
    public void fail_show_translation() throws InterruptedException {
        final View view = activity.getActivity().trackingView;
        view.setTranslationY(10000);
        startTracking(false, false, 0, 1);
    }

    @Test
    public void fail_show_visibility_gone_invisible() throws Throwable {
        visibilityChangeTest(View.GONE, View.INVISIBLE, false);
    }

    private void visibilityChangeTest(final int visibilityFrom, final int visibilityTo,
                                      boolean expectedResults) throws Throwable {
        final CountDownLatch lock = new CountDownLatch(1);
        final View view = activity.getActivity().trackingView;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(visibilityFrom);
                lock.countDown();
            }
        });
        lock.await();
        final AtomicBoolean isVisibilityChanged = new AtomicBoolean(false);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(visibilityTo);
                isVisibilityChanged.set(true);
            }
        }, 1000);
        startTracking(expectedResults, expectedResults, 1000, 3);
        Assert.assertTrue(isVisibilityChanged.get());
    }

    @SuppressWarnings("SameParameterValue")
    private void startTracking(boolean expectedResult,
                               long requiredOnScreenTimeMs) throws InterruptedException {
        startTracking(expectedResult, expectedResult, requiredOnScreenTimeMs, 10);
    }

    private void startTracking(boolean showExpectedResult,
                               boolean finishExpectedResult,
                               final long requiredOnScreenTimeMs,
                               long timeOutSec) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(requiredOnScreenTimeMs > 0 ? 2 : 1);
        final AtomicLong showReceivedTimeMs = new AtomicLong();
        VisibilityTracker.startTracking(activity.getActivity().trackingView,
                                        requiredOnScreenTimeMs,
                                        100,
                                        false,
                                        new VisibilityTracker.VisibilityChangeCallback() {
                                            @Override
                                            public void onViewShown() {
                                                showReceivedTimeMs.set(System.currentTimeMillis());
                                                isShownCalled.set(true);
                                                latch.countDown();
                                            }

                                            @Override
                                            public void onViewTrackingFinished() {
                                                Assert.assertEquals(requiredOnScreenTimeMs,
                                                                    System.currentTimeMillis()
                                                                            - showReceivedTimeMs.get(),
                                                                    100);
                                                isFinishedCalled.set(true);
                                                latch.countDown();
                                            }
                                        });

        latch.await(timeOutSec, TimeUnit.SECONDS);
        Assert.assertEquals(showExpectedResult, isShownCalled.get());
        if (requiredOnScreenTimeMs > 0) {
            Assert.assertEquals(finishExpectedResult, isFinishedCalled.get());
        }
    }

}
