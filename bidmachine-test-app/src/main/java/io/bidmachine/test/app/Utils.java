package io.bidmachine.test.app;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import io.bidmachine.utils.ActivityHelper;

public class Utils {

    private static final String BMTestApplication = "BMTestApplication";

    public static void showToast(Context context, String text) {
        Log.d(BMTestApplication, text);
        if (ActivityHelper.getTopActivity() instanceof MainActivity) {
            Snackbar.make(((Activity) context).findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT)
                    .show();
        } else {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
    }

    public static int dp2px(Context context, float size) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size,
                context.getResources().getDisplayMetrics());
    }

    static Location getLocation(Context context) {
        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, false);
            if (bestProvider != null) {
                try {
                    Location location = locationManager.getLastKnownLocation(bestProvider);
                    if (location == null) {
                        List<String> allProviders = locationManager.getAllProviders();
                        if (allProviders != null && allProviders.size() > 1) {
                            for (String provider : allProviders) {
                                if (provider != null && !provider.equals(bestProvider)) {
                                    location = locationManager.getLastKnownLocation(provider);
                                    if (location != null) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    return location;
                } catch (SecurityException | IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
