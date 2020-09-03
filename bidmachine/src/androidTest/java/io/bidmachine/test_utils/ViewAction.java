package io.bidmachine.test_utils;

import android.app.Activity;
import android.graphics.Rect;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

import androidx.test.platform.app.InstrumentationRegistry;

import io.bidmachine.core.Utils;

public class ViewAction {

    public static void click(View view) {
        Rect rect = TestHelper.getRect(view);
        click(rect);
    }

    public static void click(Activity activity) {
        Rect rect = TestHelper.getRect(activity);
        click(rect);
    }

    public static void click(Rect rect) {
        dispatchMotionEvents(
                obtainMotionEvent(rect, MotionEvent.ACTION_DOWN),
                obtainMotionEvent(rect, MotionEvent.ACTION_UP)
        );
    }

    private static MotionEvent obtainMotionEvent(Rect rect, int action) {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        float x = rect.centerX();
        float y = rect.centerY();
        int metaState = 0;
        return MotionEvent.obtain(
                downTime,
                eventTime,
                action,
                x,
                y,
                metaState
        );
    }

    private static void dispatchMotionEvents(final MotionEvent... events) {
        InstrumentationRegistry.getInstrumentation().setInTouchMode(true);
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (final MotionEvent motionEvent : events) {
                            InstrumentationRegistry.getInstrumentation().sendPointerSync(motionEvent);
                        }
                    }
                }).start();
            }
        });
    }

}
