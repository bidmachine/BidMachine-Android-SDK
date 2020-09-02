package io.bidmachine.test.app;

import androidx.multidex.MultiDexApplication;

import io.bidmachine.BidMachine;

public class BMTestApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        BidMachine.setLoggingEnabled(true);
    }

}
