package io.bidmachine.core;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
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
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
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
    private static Handler backgroundHandler;
    private static String appName;
    private static String appVersion;

    static {
        HandlerThread thread = new HandlerThread("BackgroundHandlerThread");
        thread.start();
        backgroundHandler = new Handler(thread.getLooper());
    }

    private static String defaultHttpAgentString = "";
    private static String httpAgentString;

    public static void onUiThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            uiHandler.post(runnable);
        }
    }

    public static void onUiThread(Runnable runnable, long delayMillis) {
        uiHandler.postDelayed(runnable, delayMillis);
    }

    public static void cancelUiThreadTask(Runnable runnable) {
        uiHandler.removeCallbacks(runnable);
    }

    public static void onBackgroundThread(Runnable runnable) {
        onBackgroundThread(runnable, 0);
    }

    public static void onBackgroundThread(Runnable runnable, long delay) {
        backgroundHandler.postDelayed(runnable, delay);
    }

    public static void cancelBackgroundThreadTask(Runnable runnable) {
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

    public static class ConnectionInfo {
        public final String type;
        public final String subtype;
        public final boolean isFastType;

        ConnectionInfo(String type, String subtype, boolean isFastType) {
            this.type = type;
            this.subtype = subtype;
            this.isFastType = isFastType;
        }
    }

    @Nullable
    @SuppressLint("MissingPermission")
    public static NetworkInfo getActiveNetworkInfo(@NonNull Context context) {
        if (!Utils.isPermissionGranted(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
            Log.e("BidMachine", "Manifest permission not found: android.permission.ACCESS_NETWORK_STATE. Check the integration.");
            return null;
        }
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
    }

    @NonNull
    @SuppressLint("MissingPermission")
    public static ConnectionInfo obtainConnectionInfo(Context context) {
        NetworkInfo info = getActiveNetworkInfo(context);
        String connectionType = "unknown";
        String connectionSubtype = null;
        final boolean fast;
        if (info != null && info.isConnected()) {
            connectionType = info.getTypeName();
            connectionSubtype = info.getSubtypeName();
            switch (info.getType()) {
                case ConnectivityManager.TYPE_MOBILE:
                    switch (info.getSubtype()) {
                        case TelephonyManager.NETWORK_TYPE_EVDO_0: // ~ 400-1000 kbps
                        case TelephonyManager.NETWORK_TYPE_EVDO_A: // ~ 600-1400 kbps
                        case TelephonyManager.NETWORK_TYPE_EVDO_B: // ~ 5 Mbps
                        case TelephonyManager.NETWORK_TYPE_UMTS: // ~ 400-7000 kbps
                        case TelephonyManager.NETWORK_TYPE_EHRPD: // ~ 1-2 Mbps
                        case TelephonyManager.NETWORK_TYPE_HSPA: // ~ 700-1700 kbps
                        case TelephonyManager.NETWORK_TYPE_HSDPA: // ~ 2-14 Mbps
                        case TelephonyManager.NETWORK_TYPE_HSUPA: // ~ 1-23 Mbps
                        case TelephonyManager.NETWORK_TYPE_HSPAP: // ~ 10-20 Mbps
                        case TelephonyManager.NETWORK_TYPE_LTE: { // ~ 10+ Mbps
                            fast = true;
                            break;
                        }
                        default: {
                            fast = false;
                            break;
                        }
                    }
                    break;
                case ConnectivityManager.TYPE_WIFI:
                case ConnectivityManager.TYPE_WIMAX:
                case ConnectivityManager.TYPE_ETHERNET:
                    fast = true;
                    break;
                default:
                    fast = false;
                    break;
            }
        } else {
            fast = false;
        }
        if (connectionType != null) {
            if (connectionType.equals("CELLULAR")) {
                connectionType = "MOBILE";
            }
            connectionType = connectionType.toLowerCase(Locale.ENGLISH);
        }
        if (connectionSubtype != null) {
            connectionSubtype = connectionSubtype.toLowerCase(Locale.ENGLISH);
            if (connectionSubtype.isEmpty()) {
                connectionSubtype = null;
            }

        }
        return new ConnectionInfo(connectionType, connectionSubtype, fast);
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

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /*
    Location utils
     */

    @Nullable
    @SuppressLint("MissingPermission")
    public static Location getLocation(Context context) {
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
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static int getUtcOffsetMinutes() {
        return (int) TimeUnit.MILLISECONDS.toMinutes(
                TimeZone.getDefault().getOffset(System.currentTimeMillis()));
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

    public static float getScreenWidthInDp(Context context) {
        WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        Point size = new Point();
        display.getSize(size);
        return size.x / displayMetrics.density;
    }

    public static float getScreenHeightInDp(Context context) {
        WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        Point size = new Point();
        display.getSize(size);
        return size.y / displayMetrics.density;
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

    public static int getScreenOrientation(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            switch (rotation) {
                case Surface.ROTATION_180:
                case Surface.ROTATION_270:
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                case Surface.ROTATION_0:
                case Surface.ROTATION_90:
                default:
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            switch (rotation) {
                case Surface.ROTATION_180:
                case Surface.ROTATION_270:
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                case Surface.ROTATION_0:
                case Surface.ROTATION_90:
                default:
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
        } else {
            Logger.log("Unknown screen orientation. Defaulting to portrait.");
            return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        }
    }

    public static boolean isTablet(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        double width = metrics.widthPixels / metrics.xdpi;
        double height = metrics.heightPixels / metrics.ydpi;
        Double screenSize = Math.sqrt(width * width + height * height);
        return screenSize >= 6.6d;
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
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT || writePermissionGranted(context))
                && isExternalMemoryAvailable();
    }

    public static boolean isPermissionGranted(@NonNull Context context,
                                              @Nullable String permission) {
        if (TextUtils.isEmpty(permission)) {
            return false;
        }
        try {
            return context.checkPermission(permission, android.os.Process.myPid(), Process.myUid())
                    == PackageManager.PERMISSION_GRANTED;
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

    public static boolean isViewInsideParentRect(Rect parentRect, View childView) {
        Rect childRect = getViewRectangle(childView);
        return parentRect.contains(childRect);
    }

    public static Rect getViewRectangle(View adView) {
        int[] location = new int[2];
        adView.getLocationInWindow(location);
        return new Rect(location[0], location[1], adView.getWidth() + location[0], adView.getHeight() + location[1]);
    }

    public static boolean isViewTransparent(View view) {
        return view.getAlpha() == 0.0F;
    }

    public static boolean isViewHaveSize(View view) {
        return view.getMeasuredHeight() > 0 && view.getMeasuredWidth() > 0;
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

    public static void removeViewFromParent(View view) {
        if (view.getParent() != null && view.getParent() instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) view.getParent();
            parent.removeView(view);
        }
    }

    public static boolean canAddWindowToActivity(Activity activity) {
        return activity != null && activity.getWindow() != null && activity.getWindow().isActive() && activity.getWindow().getDecorView().getWindowToken() != null;
    }

    public static void openBrowser(final Context context, String url, Executor executor, final Runnable postMethod) {
        final String validUrl = getValidUrl(url);
        if (isHttpUrl(validUrl)) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    launchUrl(context, findEndpoint(validUrl));
                    if (postMethod != null) {
                        onUiThread(postMethod);
                    }
                }
            });
        } else {
            if (postMethod != null) {
                onUiThread(postMethod);
            }
            launchUrl(context, validUrl);
        }
    }

    private static void launchUrl(Context context, String url) {
        try {
            Logger.log(String.format("launch url: %s", url));
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName componentName = pickBrowser(context, browserIntent);
            if (componentName != null) {
                browserIntent.setComponent(componentName);
                context.startActivity(browserIntent);
            } else {
                url = URLDecoder.decode(url, "UTF-8");
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                componentName = pickBrowser(context, browserIntent);
                if (componentName != null) {
                    browserIntent.setComponent(componentName);
                    context.startActivity(browserIntent);
                } else {
                    Logger.log(String.format("No activities to handle intent: %s", url));
                }
            }
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    private static String findEndpoint(String urlString) {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setConnectTimeout(500);
            urlConnection.setReadTimeout(500);
            switch (urlConnection.getResponseCode()) {
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_MOVED_TEMP:
                case HttpURLConnection.HTTP_SEE_OTHER:
                case HttpURLConnection.HTTP_USE_PROXY:
                case 307:
                    String nextUrl = urlConnection.getHeaderField("Location");
                    if (nextUrl != null) {
                        if (isHttpUrl(nextUrl)) {
                            return findEndpoint(urlConnection.getHeaderField("Location"));
                        } else if (new URI(nextUrl).getScheme() == null) {
                            try {
                                String localNextUrl = new URL(url, nextUrl).toString();
                                if (localNextUrl.trim().length() > 0) {
                                    nextUrl = localNextUrl;
                                } else {
                                    return nextUrl;
                                }
                            } catch (Exception e) {
                                Logger.log(e);
                                return nextUrl;
                            }
                            return findEndpoint(nextUrl);
                        } else {
                            return nextUrl;
                        }
                    } else {
                        return url.toString();
                    }
                default:
                    return url.toString();
            }
        } catch (Exception e) {
            Logger.log(e);
            return urlString;
        } finally {
            if (urlConnection != null) {
                try {
                    urlConnection.getInputStream().close();
                    urlConnection.disconnect();
                } catch (Exception e) {
                    Logger.log(e);
                }
            }
        }
    }

    private static ComponentName pickBrowser(Context context, Intent browserIntent) {
        PackageManager manager = context.getPackageManager();
        List<ResolveInfo> intentActivities = manager.queryIntentActivities(browserIntent, 0);
        if (!intentActivities.isEmpty()) {
            for (ResolveInfo resolveInfo : intentActivities) {
                if (resolveInfo.activityInfo.packageName.equals("com.android.vending")) {
                    return new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                }
            }
            return new ComponentName(intentActivities.get(0).activityInfo.packageName,
                    intentActivities.get(0).activityInfo.name);
        } else {
            return null;
        }
    }

    private static String getValidUrl(String urlString) {
        try {
            //noinspection UnusedAssignment
            URL testUrl = new URL(urlString);
            return urlString;
        } catch (MalformedURLException e) {
            try {
                urlString = URLDecoder.decode(urlString, "UTF-8");
                return urlString;
            } catch (UnsupportedEncodingException ex) {
                return urlString;
            } catch (IllegalArgumentException exception) {
                return urlString;
            }
        }
    }

    public static String retrieveAndSaveFrame(Context context, Uri videoFileUri, String dirName) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(context, videoFileUri);
        String time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long duration = Long.parseLong(time);
        Bitmap bitmapFrame = mediaMetadataRetriever.getFrameAtTime(duration, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        if (bitmapFrame != null) {
            FileOutputStream fileOutputStream = null;
            try {
                File file = new File(getCacheDir(context, dirName), generateFileName(videoFileUri.toString()));
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
            throw new IllegalArgumentException("Wrong Birthday Year data: should be 4-digit integer, more or equal 1900 and less or equal than current year");
        }
    }

    public static boolean isYearValid(int year) {
        return year >= 1900 && year <= currentYear;
    }

    public static String getAdvertisingUUID(android.content.Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, context.MODE_PRIVATE);
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
    public static Object invokeMethodByName(Object object, String methodName, Pair<Class, Object>... parameterPairs) throws Exception {
        return invokeMethodByName(object, object.getClass(), methodName, parameterPairs);
    }

    @SafeVarargs
    public static Object invokeMethodByName(Object object, Class<?> clazz, String methodName, Pair<Class, Object>... parameterPairs) throws Exception {
        Class[] parameterTypes;
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
    public static <T> T oneOf(T primary, T secondary) {
        return oneOf(primary, secondary, null);
    }

    @Nullable
    public static <T> T oneOf(@Nullable T primary, @Nullable T secondary, @Nullable T otherwise) {
        return primary != null ? primary : secondary != null ? secondary : otherwise;
    }

    @NonNull
    public static <T> List<T> resolveList(@Nullable List<T> primary, @Nullable List<T> secondary) {
        return primary != null ? primary : secondary != null ? secondary : Collections.<T>emptyList();
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
                PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
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

}
