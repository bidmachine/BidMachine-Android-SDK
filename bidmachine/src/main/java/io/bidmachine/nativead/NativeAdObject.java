package io.bidmachine.nativead;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.explorestack.iab.vast.VastRequest;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import io.bidmachine.AdObjectImpl;
import io.bidmachine.AdProcessCallback;
import io.bidmachine.ContextProvider;
import io.bidmachine.MediaAssetType;
import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;
import io.bidmachine.core.VisibilityTracker;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.nativead.utils.ImageHelper;
import io.bidmachine.nativead.utils.NativeNetworkExecutor;
import io.bidmachine.nativead.view.MediaView;
import io.bidmachine.nativead.view.NativeMediaView;
import io.bidmachine.unified.UnifiedNativeAd;
import io.bidmachine.unified.UnifiedNativeAdCallback;
import io.bidmachine.unified.UnifiedNativeAdRequestParams;
import io.bidmachine.utils.BMError;
import io.bidmachine.utils.ViewHelper;

public final class NativeAdObject
        extends AdObjectImpl<NativeRequest, AdObjectParams, UnifiedNativeAd, UnifiedNativeAdCallback, UnifiedNativeAdRequestParams>
        implements NativeData, NativeMediaPrivateData, NativeContainer, NativeInteractor, View.OnClickListener {

    static final float DEFAULT_RATING = -1;
    private static final int ICON_VIEW_ID = 100;
    private static final int MEDIA_VIEW_ID = 200;
    private static final String INSTALL = "Install";

    private static final WeakHashMap<ViewGroup, WeakHashMap<View, View.OnClickListener>> clickStorage =
            new WeakHashMap<>(3);

    private ViewGroup container;
    private MediaView mediaView;
    private ProgressDialog progressDialog;

    private Handler progressDialogCanceller;
    private Runnable progressRunnable;

    private boolean impressionTracked;
    private boolean viewRegistered;

    @Nullable
    private Bitmap iconBitmap;
    @Nullable
    private Uri iconUri;
    @Nullable
    private Bitmap imageBitmap;
    @Nullable
    private Uri imageUri;
    @Nullable
    private Uri videoUri;
    @Nullable
    private VastRequest vastRequest;
    @Nullable
    private NativeNetworkAdapter nativeNetworkAdapter;

    NativeAdObject(@NonNull ContextProvider contextProvider,
                   @NonNull AdProcessCallback processCallback,
                   @NonNull NativeRequest adRequest,
                   @NonNull AdObjectParams adObjectParams,
                   @NonNull UnifiedNativeAd unifiedAd) {
        super(contextProvider, processCallback, adRequest, adObjectParams, unifiedAd);
    }

    @Nullable
    @Override
    public String getTitle() {
        return nativeNetworkAdapter != null ? nativeNetworkAdapter.getTitle() : null;
    }

    @Nullable
    @Override
    public String getDescription() {
        return nativeNetworkAdapter != null ? nativeNetworkAdapter.getDescription() : null;
    }

    @Nullable
    @Override
    public String getCallToAction() {
        String callToAction = nativeNetworkAdapter != null
                ? nativeNetworkAdapter.getCallToAction()
                : null;
        return TextUtils.isEmpty(callToAction) ? INSTALL : callToAction;
    }

    @Override
    public float getRating() {
        return nativeNetworkAdapter != null ? nativeNetworkAdapter.getRating() : DEFAULT_RATING;
    }

    @Nullable
    @Override
    public String getIconUrl() {
        return nativeNetworkAdapter != null ? nativeNetworkAdapter.getIconUrl() : null;
    }

    @Nullable
    @Override
    public String getImageUrl() {
        return nativeNetworkAdapter != null ? nativeNetworkAdapter.getImageUrl() : null;
    }

    @Nullable
    @Override
    public String getClickUrl() {
        return nativeNetworkAdapter != null ? nativeNetworkAdapter.getClickUrl() : null;
    }

    @Nullable
    @Override
    public String getVideoUrl() {
        return nativeNetworkAdapter != null ? nativeNetworkAdapter.getVideoUrl() : null;
    }

    @Nullable
    @Override
    public String getVideoAdm() {
        return nativeNetworkAdapter != null ? nativeNetworkAdapter.getVideoAdm() : null;
    }

    @Override
    public void setIconBitmap(@Nullable Bitmap iconBitmap) {
        this.iconBitmap = iconBitmap;
    }

    @Override
    public void setIconUri(@Nullable Uri iconUri) {
        this.iconUri = iconUri;
    }

    @Override
    @Nullable
    public Uri getIconUri() {
        return iconUri;
    }

    @Nullable
    @Override
    public Bitmap getIconBitmap() {
        return iconBitmap;
    }

    @Override
    public void setImageUri(@Nullable Uri imageUri) {
        this.imageUri = imageUri;
    }

    @Override
    @Nullable
    public Uri getImageUri() {
        return imageUri;
    }

    @Override
    public void setImageBitmap(@Nullable Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    @Nullable
    @Override
    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    @Override
    public void setVideoUri(@Nullable Uri videoUri) {
        this.videoUri = videoUri;
    }

    @Nullable
    @Override
    public Uri getVideoUri() {
        return videoUri;
    }

    @Override
    public void setVastRequest(@Nullable VastRequest vastRequest) {
        this.vastRequest = vastRequest;
    }

    @Override
    @Nullable
    public VastRequest getVastRequest() {
        return vastRequest;
    }

    @Override
    public boolean hasVideo() {
        return videoUri != null
                || nativeNetworkAdapter != null && nativeNetworkAdapter.hasVideo();
    }

    @NonNull
    @Override
    public UnifiedNativeAdCallback createUnifiedCallback(@NonNull AdProcessCallback processCallback) {
        return new UnifiedNativeAdCallbackImpl(processCallback);
    }

    @Nullable
    @Override
    public View getProviderView(Context context) {
        return nativeNetworkAdapter != null
                ? nativeNetworkAdapter.configureProviderView()
                : null;
    }

    @Override
    public void registerView(@Nullable ViewGroup nativeAdView,
                             @Nullable View imageView,
                             @Nullable NativeMediaView nativeMediaView,
                             @Nullable Set<View> clickableViews) {
        try {
            if (!isNativeAdViewValid(nativeAdView, imageView, nativeMediaView, clickableViews)) {
                getProcessCallback().processShowFail(BMError.NoContent);
                return;
            }
            assert nativeAdView != null;
            configureClickableView(nativeAdView, clickableViews);
            ImageView iconView = configureIconView(imageView);
            configureMediaView(nativeMediaView);
            container = nativeAdView;
            if (!impressionTracked) {
                VisibilityTracker.startTracking(
                        container,
                        getParams().getViewabilityTimeThresholdMs(),
                        getParams().getViewabilityPixelThreshold(),
                        new VisibilityTracker.VisibilityChangeCallback() {
                            @Override
                            public void onViewShown() {
                                impressionTracked = true;
                                dispatchShown();
                            }

                            @Override
                            public void onViewTrackingFinished() {
                                dispatchImpression();
                            }
                        });
            }
            if (mediaView != null) {
                mediaView.onViewAppearOnScreen();
                mediaView.startVideoVisibilityCheckerTimer();
            }
            if (nativeNetworkAdapter != null) {
                nativeNetworkAdapter.registerNative(nativeAdView,
                                                    iconView,
                                                    nativeMediaView,
                                                    clickableViews);
            }
            viewRegistered = true;
        } catch (Throwable t) {
            unregisterView();
            getProcessCallback().processShowFail(BMError.catchError("Error during registerView"));
            Logger.log(t);
        }
    }

    @Override
    public void unregisterView() {
        try {
            if (container != null) {
                deConfigureClickableView(container);
                VisibilityTracker.stopTracking(container);
            }
            if (mediaView != null) {
                mediaView.stopVideoVisibilityCheckerTimer();
                mediaView.release();
            }
            if (nativeNetworkAdapter != null) {
                nativeNetworkAdapter.unregisterNative();
            }
            viewRegistered = false;
        } catch (Throwable t) {
            Logger.log(t);
        }
    }

    @Override
    public void onDestroy() {
        try {
            super.onDestroy();
            unregisterView();
            if (iconBitmap != null) {
                if (!iconBitmap.isRecycled()) {
                    iconBitmap.recycle();
                }
                iconBitmap = null;
            }
            if (imageBitmap != null) {
                if (!imageBitmap.isRecycled()) {
                    imageBitmap.recycle();
                }
                imageBitmap = null;
            }
            if (videoUri != null && videoUri.getPath() != null) {
                File file = new File(videoUri.getPath());
                if (file.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
                videoUri = null;
            }
            if (nativeNetworkAdapter != null) {
                nativeNetworkAdapter.destroy();
            }
        } catch (Throwable t) {
            Logger.log(t);
        }
    }

    @VisibleForTesting
    boolean isNativeAdViewValid(@Nullable ViewGroup nativeAdView,
                                @Nullable View imageView,
                                @Nullable NativeMediaView nativeMediaView,
                                @Nullable Set<View> clickableViews) {
        if (nativeAdView == null) {
            Logger.log("NativeAdView cannot be null. NativeAd is NOT registered");
            return false;
        }
        if (imageView == null && nativeMediaView == null) {
            Logger.log("ImageView or NativeMediaView cannot be null. NativeAd is NOT registered");
            return false;
        } else {
            if (imageView != null && !ViewHelper.belongTo(nativeAdView, imageView)) {
                Logger.log("ImageView should belong to NativeAdView. NativeAd is NOT registered");
                return false;
            }
            if (nativeMediaView != null && !ViewHelper.belongTo(nativeAdView, nativeMediaView)) {
                Logger.log("NativeMediaView should belong to NativeAdView. NativeAd is NOT registered");
                return false;
            }
        }
        if (clickableViews != null) {
            for (View view : clickableViews) {
                if (view != null && !ViewHelper.belongTo(nativeAdView, view)) {
                    Logger.log(
                            "All clickable views should belong to NativeAdView. NativeAd is NOT registered");
                    return false;
                }
            }
        }
        return true;
    }

    private void configureClickableView(@NonNull ViewGroup nativeAdView,
                                        @Nullable Set<View> clickableViews) {
        deConfigureClickableView(nativeAdView);
        if (clickableViews == null || clickableViews.size() == 0) {
            return;
        }
        WeakHashMap<View, View.OnClickListener> weakClickableMap = new WeakHashMap<>();
        clickStorage.put(nativeAdView, weakClickableMap);
        for (View view : clickableViews) {
            if (view != null) {
                view.setOnClickListener(this);
                weakClickableMap.put(view, this);
            }
        }
    }

    private void deConfigureClickableView(@NonNull ViewGroup nativeAdView) {
        WeakHashMap<View, View.OnClickListener> weakClickableMap = clickStorage.get(nativeAdView);
        if (weakClickableMap != null) {
            for (Map.Entry<View, View.OnClickListener> entry : weakClickableMap.entrySet()) {
                if (entry != null && entry.getKey() != null) {
                    entry.getKey().setOnClickListener(null);
                }
            }
            clickStorage.remove(nativeAdView);
        }
    }

    @Nullable
    private ImageView configureIconView(@Nullable View view) {
        ImageView iconView = null;
        if (view instanceof ImageView) {
            iconView = (ImageView) view;
        } else if (view instanceof ViewGroup) {
            iconView = new ImageView(view.getContext());
            iconView.setId(ICON_VIEW_ID);
            iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            ViewGroup viewGroup = ((ViewGroup) view);
            viewGroup.removeAllViews();
            viewGroup.addView(iconView, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            );
        }
        if (iconView != null) {
            ImageHelper.fillImageView(iconView.getContext(),
                                      iconView,
                                      iconUri,
                                      iconBitmap);
        }
        return iconView;
    }

    private void configureMediaView(@Nullable NativeMediaView nativeMediaView) {
        if (nativeMediaView != null) {
            nativeMediaView.removeAllViews();
            if (nativeNetworkAdapter != null
                    && !nativeNetworkAdapter.configureMediaView(nativeMediaView)) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.addRule(
                        RelativeLayout.CENTER_IN_PARENT,
                        RelativeLayout.TRUE);
                mediaView = new MediaView(nativeMediaView.getContext());
                mediaView.setId(MEDIA_VIEW_ID);

                final NativeRequest request = getAdRequest();
                if (request.containsAssetType(MediaAssetType.Image)
                        || request.containsAssetType(MediaAssetType.Video)) {
                    mediaView.setNativeAdObject(this);
                }
                nativeMediaView.addView(mediaView, layoutParams);
            }
        }
    }

    /* progress dialog */
    private void showProgressDialog(Context context) {
        if (container != null && context instanceof Activity && mayShowProgressDialog()) {
            Activity activity = (Activity) context;
            if (Utils.canAddWindowToActivity(activity)) {
                container.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View v) {

                    }

                    @Override
                    public void onViewDetachedFromWindow(View v) {
                        v.removeOnAttachStateChangeListener(this);
                        hideProgressDialog();
                    }
                });
                progressDialog = ProgressDialog.show(activity, "", "Loading...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressRunnable = new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                    }
                };
                progressDialogCanceller = new Handler(Looper.getMainLooper());
                progressDialogCanceller.postDelayed(progressRunnable, 5000);
            }
        }
    }

    private boolean mayShowProgressDialog() {
        return progressDialog == null || !progressDialog.isShowing();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        if (progressRunnable != null && progressDialogCanceller != null) {
            progressDialogCanceller.removeCallbacks(progressRunnable);
            progressDialogCanceller = null;
            progressRunnable = null;
        }
    }

    private void loadAsset(@NonNull Context context, @NonNull NativeData nativeData) {
        (new AssetLoader(getAdRequest(), getProcessCallback(), nativeData, this))
                .downloadNativeAdsImages(context);
    }

    @Override
    public boolean isViewRegistered() {
        return viewRegistered;
    }

    @Override
    public void dispatchShown() {
        getProcessCallback().processShown();
    }

    @Override
    public void onClick(View view) {
        dispatchClick();
    }

    @Override
    public void onClicked() {
        super.onClicked();
        String clickUrl = getClickUrl();
        if (TextUtils.isEmpty(clickUrl)) {
            return;
        }
        showProgressDialog(getContext());
        Utils.openBrowser(getContext(),
                          clickUrl,
                          NativeNetworkExecutor.getInstance(),
                          new Runnable() {
                              @Override
                              public void run() {
                                  hideProgressDialog();
                              }
                          });
    }

    @Override
    public void dispatchClick() {
        getProcessCallback().processClicked();
    }

    @Override
    public void dispatchImpression() {
        getProcessCallback().processImpression();
    }

    @Override
    public void dispatchVideoPlayFinished() {
    }

    private final class UnifiedNativeAdCallbackImpl extends BaseUnifiedAdCallback implements UnifiedNativeAdCallback {

        UnifiedNativeAdCallbackImpl(@NonNull AdProcessCallback processCallback) {
            super(processCallback);
        }

        @Override
        public void onAdLoaded(@NonNull NativeNetworkAdapter nativeNetworkAdapter) {
            NativeAdObject.this.nativeNetworkAdapter = nativeNetworkAdapter;
            try {
                loadAsset(getContext(), nativeNetworkAdapter);
            } catch (Exception e) {
                Logger.log(e);
                processCallback.processLoadFail(BMError.Internal);
            }
        }
    }
}