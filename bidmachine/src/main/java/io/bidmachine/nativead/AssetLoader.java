package io.bidmachine.nativead;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.explorestack.iab.vast.VastRequest;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.bidmachine.AdProcessCallback;
import io.bidmachine.MediaAssetType;
import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;
import io.bidmachine.nativead.tasks.DownloadImageTask;
import io.bidmachine.nativead.tasks.DownloadVastVideoTask;
import io.bidmachine.nativead.tasks.DownloadVideoTask;
import io.bidmachine.nativead.utils.NativeNetworkExecutor;
import io.bidmachine.utils.BMError;

class AssetLoader {

    private static final String DIR_NAME = "native_cache_image";

    private final List<Runnable> pendingTasks = new CopyOnWriteArrayList<>();

    private NativeRequest adRequest;
    private AdProcessCallback callback;
    private NativeData nativeData;
    private NativeMediaPrivateData nativeMediaData;

    AssetLoader(@NonNull NativeRequest request,
                @NonNull AdProcessCallback callback,
                @NonNull NativeData nativeData,
                @NonNull NativeMediaPrivateData nativeMediaData) {
        this.adRequest = request;
        this.callback = callback;
        this.nativeData = nativeData;
        this.nativeMediaData = nativeMediaData;
    }

    void downloadNativeAdsImages(@NonNull Context context) {
        startDownloadTask(context);
        checkTasksCount();
    }

    private void startDownloadTask(@NonNull Context context) {
        final String iconUrl = nativeData.getIconUrl();
        final String imageUrl = nativeData.getImageUrl();
        final String videoUrl = nativeData.getVideoUrl();
        final String videoAdm = nativeData.getVideoAdm();
        if (adRequest.containsAssetType(MediaAssetType.Icon)) {
            createIconTask(context, iconUrl);
        }
        if (adRequest.containsAssetType(MediaAssetType.Image)) {
            createImageTask(context, imageUrl);
        }
        if (adRequest.containsAssetType(MediaAssetType.Video)) {
            if (!TextUtils.isEmpty(videoUrl)) {
                createVideoTask(context, videoUrl);
            } else if (!TextUtils.isEmpty(videoAdm)) {
                createVastVideoTask(context, videoAdm);
            }
        }
        if (pendingTasks.isEmpty()) {
            checkTasksCount();
        } else {
            for (Runnable task : pendingTasks) {
                NativeNetworkExecutor.getInstance().execute(task);
            }
        }
    }

    private void createIconTask(final Context context, String url) {
        if (!TextUtils.isEmpty(url)) {
            DownloadImageTask.OnCacheImageListener listener = new DownloadImageTask.OnCacheImageListener() {
                @Override
                public void onPathSuccess(DownloadImageTask task, Uri imagePath) {
                    nativeMediaData.setIconUri(imagePath);
                    removePendingTask(task);
                }

                @Override
                public void onImageSuccess(DownloadImageTask task, Bitmap imageBitmap) {
                    nativeMediaData.setIconBitmap(imageBitmap);
                    removePendingTask(task);
                }

                @Override
                public void onFail(DownloadImageTask task) {
                    removePendingTask(task);
                }
            };
            pendingTasks.add(DownloadImageTask.newBuilder(context, url)
                                     .setOnCacheImageListener(listener)
                                     .build());
        }
    }

    private void createImageTask(final Context context, String url) {
        if (!TextUtils.isEmpty(url)) {
            DownloadImageTask.OnCacheImageListener listener = new DownloadImageTask.OnCacheImageListener() {
                @Override
                public void onPathSuccess(DownloadImageTask task, Uri imagePath) {
                    nativeMediaData.setImageUri(imagePath);
                    removePendingTask(task);
                }

                @Override
                public void onImageSuccess(DownloadImageTask task, Bitmap imageBitmap) {
                    nativeMediaData.setImageBitmap(imageBitmap);
                    removePendingTask(task);
                }

                @Override
                public void onFail(DownloadImageTask task) {
                    removePendingTask(task);
                }
            };
            pendingTasks.add(DownloadImageTask.newBuilder(context, url)
                                     .setCheckAspectRatio(true)
                                     .setOnCacheImageListener(listener)
                                     .build());
        }
    }

    private void createVideoTask(final Context context, String url) {
        DownloadVideoTask.OnLoadedListener listener = new DownloadVideoTask.OnLoadedListener() {
            @Override
            public void onVideoLoaded(DownloadVideoTask task, Uri videoFileUri) {
                nativeMediaData.setVideoUri(videoFileUri);
                if (TextUtils.isEmpty(nativeData.getImageUrl())
                        && videoFileUri != null
                        && videoFileUri.getPath() != null
                        && new File(videoFileUri.getPath()).exists()) {
                    nativeMediaData.setImageUri(
                            Uri.parse(Utils.retrieveAndSaveFrame(
                                    context,
                                    videoFileUri,
                                    DIR_NAME)));
                }
                removePendingTask(task);
            }

            @Override
            public void onVideoLoadingError(DownloadVideoTask task) {
                removePendingTask(task);
            }
        };
        pendingTasks.add(new DownloadVideoTask(context, listener, url));
    }

    private void createVastVideoTask(final Context context, String vastVideoAdm) {
        DownloadVastVideoTask.OnLoadedListener listener = new DownloadVastVideoTask.OnLoadedListener() {
            @Override
            public void onVideoLoaded(DownloadVastVideoTask task,
                                      Uri videoFileUri,
                                      VastRequest vastRequest) {
                nativeMediaData.setVideoUri(videoFileUri);
                nativeMediaData.setVastRequest(vastRequest);
                if (TextUtils.isEmpty(nativeData.getImageUrl())
                        && videoFileUri != null
                        && videoFileUri.getPath() != null
                        && new File(videoFileUri.getPath()).exists()) {
                    nativeMediaData.setImageUri(
                            Uri.parse(Utils.retrieveAndSaveFrame(
                                    context,
                                    videoFileUri,
                                    DIR_NAME)));
                }
                removePendingTask(task);
            }

            @Override
            public void onVideoLoadingError(DownloadVastVideoTask task) {
                removePendingTask(task);
            }
        };
        pendingTasks.add(new DownloadVastVideoTask(context, listener, vastVideoAdm));
    }

    private void removePendingTask(Runnable task) {
        pendingTasks.remove(task);
        checkTasksCount();
    }

    private void checkTasksCount() {
        if (pendingTasks.isEmpty()) {
            notifyNativeCallback();
        }
    }

    private synchronized void notifyNativeCallback() {
        if (!isAssetsValid()) {
            callback.processLoadFail(BMError.IncorrectAdUnit);
            callback.processDestroy();
        } else {
            callback.processLoadSuccess();
        }
    }

    private boolean isAssetsValid() {
        try {
            return isIconValid() && isImageValid() && isVideoValid();
        } catch (Exception e) {
            Logger.log(e);
            return false;
        }
    }

    private boolean isIconValid() {
        if (adRequest.containsAssetType(MediaAssetType.Icon)) {
            return nativeMediaData.getIconUri() != null
                    || nativeMediaData.getIconBitmap() != null;
        }
        return true;
    }

    private boolean isImageValid() {
        if (adRequest.containsAssetType(MediaAssetType.Image)) {
            return nativeMediaData.getImageUri() != null
                    || nativeMediaData.getImageBitmap() != null;
        }
        return true;
    }

    private boolean isVideoValid() {
        if (adRequest.containsAssetType(MediaAssetType.Video)) {
            return nativeData.hasVideo();
        }
        return true;
    }

}