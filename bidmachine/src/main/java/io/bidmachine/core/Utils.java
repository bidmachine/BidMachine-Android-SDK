package io.bidmachine.core;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.security.NetworkSecurityPolicy;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class Utils {

    private static final String UUID_ID = "uuid";
    private static final String SHARED_PREFERENCES_NAME = "ad_core_preferences";

    private static final Handler uiHandler = new Handler(Looper.getMainLooper());

    @NonNull
    private static final Handler backgroundHandler;
    private static String appName;
    private static String appVersion;

    static {
        HandlerThread thread = new HandlerThread("BackgroundHandlerThread");
        thread.start();
        backgroundHandler = new Handler(thread.getLooper());
    }

    private static String defaultHttpAgentString = "";
    private static String httpAgentString;

    public static boolean isUiThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static void onUiThread(@NonNull Runnable runnable) {
        if (isUiThread()) {
            runnable.run();
        } else {
            uiHandler.post(runnable);
        }
    }

    public static void onUiThread(@NonNull Runnable runnable, long delayMillis) {
        uiHandler.postDelayed(runnable, delayMillis);
    }

    public static void cancelUiThreadTask(@NonNull Runnable runnable) {
        uiHandler.removeCallbacks(runnable);
    }

    public static void onBackgroundThread(@NonNull Runnable runnable) {
        if (isUiThread()) {
            backgroundHandler.post(runnable);
        } else {
            runnable.run();
        }
    }

    public static void onBackgroundThread(@NonNull Runnable runnable, long delay) {
        backgroundHandler.postDelayed(runnable, delay);
    }

    public static void cancelBackgroundThreadTask(@NonNull Runnable runnable) {
        backgroundHandler.removeCallbacks(runnable);
    }

    public static String streamToString(final InputStream inputStream) throws Exception {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }

    @Nullable
    @SuppressLint("MissingPermission")
    public static NetworkInfo getActiveNetworkInfo(@NonNull Context context) {
        if (!Utils.isPermissionGranted(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
            Logger.logError("Manifest permission not found: android.permission.ACCESS_NETWORK_STATE. Check the integration.");
            return null;
        }
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        } catch (Throwable t) {
            Logger.log(t);
        }
        return null;
    }

    @SuppressLint("MissingPermission")
    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo activeNetworkInfo = getActiveNetworkInfo(context);
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @NonNull
    public static String obtainHttpAgentString(final Context context) {
        final CountDownLatch latch = new CountDownLatch(1);
        if (httpAgentString != null) {
            return httpAgentString;
        }
        onUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        httpAgentString = new WebView(context).getSettings().getUserAgentString();
                    } else {
                        httpAgentString = WebSettings.getDefaultUserAgent(context);
                    }
                } catch (Throwable t) {
                    Logger.log(t);
                } finally {
                    latch.countDown();
                }
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return TextUtils.isEmpty(httpAgentString)
                ? obtainDefaultHttpAgentString()
                : httpAgentString;
    }

    private static String obtainDefaultHttpAgentString() {
        if (TextUtils.isEmpty(defaultHttpAgentString)) {
            try {
                defaultHttpAgentString = System.getProperty("http.agent", "");
            } catch (Throwable t) {
                Logger.log(t);
            }
        }
        return defaultHttpAgentString;
    }

    /*
    Location utils
     */

    @Nullable
    @SuppressLint("MissingPermission")
    public static Location getLocation(Context context) {
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager == null) {
                return null;
            }
            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, false);
            if (bestProvider == null) {
                return null;
            }
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                return location;
            }
            List<String> allProviders = locationManager.getAllProviders();
            if (allProviders == null || allProviders.size() <= 1) {
                return null;
            }
            for (String provider : allProviders) {
                if (provider != null && !provider.equals(bestProvider)) {
                    location = locationManager.getLastKnownLocation(provider);
                    if (location != null) {
                        return location;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getUtcOffsetMinutes() {
        return (int) TimeUnit.MILLISECONDS.toMinutes(TimeZone.getDefault()
                                                             .getOffset(System.currentTimeMillis()));
    }

    /*
    Screen utils
     */

    @NonNull
    public static Point getScreenSize(Context context) {
        WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        if (window != null) {
            Display display = window.getDefaultDisplay();
            display.getSize(size);
        }
        return size;
    }

    public static int getScreenDpi(Context context) {
        WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        return displayMetrics.densityDpi;
    }

    public static float getScreenDensity(Context context) {
        WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        return displayMetrics.density;
    }

    public static boolean isTablet(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        double width = metrics.widthPixels / metrics.xdpi;
        double height = metrics.heightPixels / metrics.ydpi;
        return Math.sqrt(width * width + height * height) >= 6.6d;
    }

    /*
    Device info utils
     */

    public static float getBatteryPercent(Context context) {
        try {
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, intentFilter);
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                if (level == -1 || scale == -1) {
                    return -1;
                } else {
                    return level / (float) scale * 100;
                }
            }
        } catch (Exception e) {
            Logger.log(e);
        }
        return -1;
    }

    public static boolean canUseExternalFilesDir(Context context) {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                || writePermissionGranted(context))
                && isExternalMemoryAvailable();
    }

    public static boolean isPermissionGranted(@NonNull Context context,
                                              @Nullable String permission) {
        if (TextUtils.isEmpty(permission)) {
            return false;
        }
        try {
            return context.checkPermission(permission,
                                           android.os.Process.myPid(),
                                           Process.myUid()) == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            return false;
        }
    }

    @VisibleForTesting
    static boolean writePermissionGranted(Context context) {
        return isPermissionGranted(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static boolean isExternalMemoryAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static byte[] getMD5(byte[] bytes) {
        byte[] hash = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(bytes);
            hash = digest.digest();
        } catch (NoSuchAlgorithmException e) {
            Logger.log(e);
        }
        return hash;
    }

    @Nullable
    public static File getCacheDir(Context context, String dirName) {
        File externalStorage = context.getExternalFilesDir(null);
        if (externalStorage != null) {
            String dir = externalStorage.getPath() + "/" + dirName + "/";
            File cacheDir = new File(dir);
            if (cacheDir.exists() || cacheDir.mkdirs()) {
                return cacheDir;
            }
        }
        return null;
    }

    public static boolean isHttpUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    public static String generateFileName(String url) {
        byte[] md5 = Utils.getMD5(url.getBytes());
        BigInteger bi = new BigInteger(md5).abs();
        return bi.toString(10 + 26);
    }

    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    public static void flush(Flushable flushable) {
        try {
            if (flushable != null) {
                flushable.flush();
            }
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    public static Rect getViewRectangle(View adView) {
        int[] location = new int[2];
        adView.getLocationInWindow(location);
        return new Rect(location[0],
                        location[1],
                        adView.getWidth() + location[0],
                        adView.getHeight() + location[1]);
    }

    public static boolean isViewTransparent(View view) {
        return view.getAlpha() == 0.0F;
    }

    public static void httpGetURL(final String url, Executor executor) {
        if (!TextUtils.isEmpty(url) && executor != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection conn = null;
                    try {
                        URL httpUrl = new URL(url);
                        conn = (HttpURLConnection) httpUrl.openConnection();
                        conn.setConnectTimeout(5000);
                        conn.setInstanceFollowRedirects(true);
                        conn.setRequestProperty("Connection", "close");
                        conn.setRequestMethod("GET");
                        conn.getResponseCode();
                    } catch (Exception e) {
                        Logger.log(e);
                    } finally {
                        if (conn != null) {
                            try {
                                conn.getInputStream().close();
                                conn.disconnect();
                            } catch (Exception e) {
                                Logger.log(e);
                            }
                        }
                    }
                }
            });
        }
    }

    public static boolean canAddWindowToActivity(Activity activity) {
        return activity != null
                && activity.getWindow() != null
                && activity.getWindow().isActive()
                && activity.getWindow().getDecorView().getWindowToken() != null;
    }

    public static String retrieveAndSaveFrame(Context context, Uri videoFileUri, String dirName) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(context, videoFileUri);
        String time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long duration = Long.parseLong(time);
        Bitmap bitmapFrame = mediaMetadataRetriever.getFrameAtTime(duration,
                                                                   MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        if (bitmapFrame != null) {
            FileOutputStream fileOutputStream = null;
            try {
                File file = new File(getCacheDir(context, dirName),
                                     generateFileName(videoFileUri.toString()));
                fileOutputStream = new FileOutputStream(file);
                bitmapFrame.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);

                return file.getAbsolutePath();
            } catch (Exception e) {
                Logger.log(e);
            } finally {
                flush(fileOutputStream);
                close(fileOutputStream);
            }
        }
        return null;
    }

    private static final Integer currentYear;

    static {
        currentYear = Calendar.getInstance().get(Calendar.YEAR);
    }

    public static void assertYear(int year) {
        if (!isYearValid(year)) {
            throw new IllegalArgumentException(
                    "Wrong Birthday Year data: should be 4-digit integer, more or equal 1900 and less or equal than current year");
        }
    }

    public static boolean isYearValid(int year) {
        return year >= 1900 && year <= currentYear;
    }

    public static String getAdvertisingUUID(android.content.Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
                                                                    Context.MODE_PRIVATE);
        if (sharedPref.contains(UUID_ID)) {
            return sharedPref.getString(UUID_ID, null);
        } else {
            String uuid = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(UUID_ID, uuid);
            editor.apply();
            return uuid;
        }
    }

    @SafeVarargs
    public static Object invokeMethodByName(Object object,
                                            String methodName,
                                            Pair<Class<?>, Object>... parameterPairs) throws Exception {
        return invokeMethodByName(object, object.getClass(), methodName, parameterPairs);
    }

    @SafeVarargs
    public static Object invokeMethodByName(Object object,
                                            Class<?> clazz,
                                            String methodName,
                                            Pair<Class<?>, Object>... parameterPairs) throws Exception {
        Class<?>[] parameterTypes;
        Object[] parameterObject;

        if (parameterPairs != null) {
            parameterTypes = new Class[parameterPairs.length];
            parameterObject = new Object[parameterPairs.length];

            for (int i = 0; i < parameterPairs.length; i++) {
                parameterTypes[i] = parameterPairs[i].first;
                parameterObject[i] = parameterPairs[i].second;
            }
        } else {
            parameterTypes = null;
            parameterObject = null;
        }

        int maxStep = 10;
        while (maxStep > 0) {
            if (clazz == null) {
                break;
            }

            try {
                Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
                return method.invoke(object, parameterObject);
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                break;
            } catch (InvocationTargetException e) {
                break;
            }

            maxStep--;
        }

        return null;
    }

    @Nullable
    public static <T> T oneOf(@Nullable T primary, @Nullable T secondary) {
        return oneOf(primary, secondary, null);
    }

    @Nullable
    public static <T> T oneOf(@Nullable T primary, @Nullable T secondary, @Nullable T otherwise) {
        return primary != null ? primary : secondary != null ? secondary : otherwise;
    }

    public static long getOrDefault(long target, long targetDefault, long def) {
        return target == targetDefault ? def : target;
    }

    public static float getOrDefault(float target, float targetDefault, float def) {
        return target == targetDefault ? def : target;
    }

    public static String capitalize(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    public static String getAppName(android.content.Context context) {
        if (appName == null) {
            PackageManager packageManager = context.getPackageManager();
            appName = (String) packageManager.getApplicationLabel(context.getApplicationInfo());
        }
        return appName;
    }

    public static String getAppVersion(android.content.Context context) {
        if (appVersion == null) {
            try {
                PackageManager packageManager = context.getPackageManager();
                PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(),
                                                                        0);
                if ((packageInfo.versionName) != null) {
                    appVersion = packageInfo.versionName;
                }
            } catch (Exception e) {
                Logger.log(e);
            }
        }
        return appVersion;
    }

    public static boolean canUseCleartextTraffic() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted();
    }

    @NonNull
    public static Map<String, String> toMap(@NonNull JSONObject jsonObject) throws Exception {
        Map<String, String> map = new HashMap<>();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObject.opt(key);
            if (value != null) {
                map.put(key, value.toString());
            }
        }
        return map;
    }

}