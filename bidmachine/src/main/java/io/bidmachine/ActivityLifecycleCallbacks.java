package io.bidmachine;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import io.bidmachine.core.Utils;

class ActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // ignore
    }

    @Override
    public void onActivityStarted(Activity activity) {
        BidMachineImpl.get().setTopActivity(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        BidMachineImpl.get().setTopActivity(activity);
        Utils.onBackgroundThread(new Runnable() {
            @Override
            public void run() {
                SessionManager.get().resume();
            }
        });
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Utils.onBackgroundThread(new Runnable() {
            @Override
            public void run() {
                SessionManager.get().pause();
            }
        });
    }

    @Override
    public void onActivityStopped(Activity activity) {
        // ignore
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // ignore
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // ignore
    }

}