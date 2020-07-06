package com.appodeal.ads.core;

import android.app.Activity;
import android.net.NetworkInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class UtilsTest {

    private Activity activity;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.buildActivity(Activity.class).create().get();
    }

    @Test
    public void isPermissionGranted_resultFalse() {
        boolean result = Utils.isPermissionGranted(activity,
                                                   "android.permission.ACCESS_NETWORK_STATE");
        assertFalse(result);
        result = Utils.isPermissionGranted(activity, "android.permission.INTERNET");
        assertFalse(result);
        result = Utils.isPermissionGranted(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
        assertFalse(result);
    }

    @Test
    public void isPermissionGranted_resultTrue() {
        Shadows.shadowOf(activity).grantPermissions("android.permission.ACCESS_NETWORK_STATE");
        boolean result = Utils.isPermissionGranted(activity,
                                                   "android.permission.ACCESS_NETWORK_STATE");
        assertTrue(result);
        result = Utils.isPermissionGranted(activity, "android.permission.INTERNET");
        assertFalse(result);
        result = Utils.isPermissionGranted(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
        assertFalse(result);
    }

    @Test
    public void writePermissionGranted_resultFalse() {
        boolean result = Utils.writePermissionGranted(activity);
        assertFalse(result);
    }

    @Test
    public void writePermissionGranted_resultTrue() {
        Shadows.shadowOf(activity).grantPermissions("android.permission.WRITE_EXTERNAL_STORAGE");
        boolean result = Utils.writePermissionGranted(activity);
        assertTrue(result);
    }

    @Test
    public void getActiveNetworkInfo() {
        NetworkInfo networkInfo = Utils.getActiveNetworkInfo(activity);
        assertNull(networkInfo);
        Shadows.shadowOf(activity).grantPermissions("android.permission.ACCESS_NETWORK_STATE");
        networkInfo = Utils.getActiveNetworkInfo(activity);
        assertNotNull(networkInfo);
    }

    @Test
    public void isNetworkAvailable() {
        boolean result = Utils.isNetworkAvailable(activity);
        assertFalse(result);
        Shadows.shadowOf(activity).grantPermissions("android.permission.ACCESS_NETWORK_STATE");
        result = Utils.isNetworkAvailable(activity);
        assertTrue(result);
    }

}