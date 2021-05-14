package io.bidmachine.nativead.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Pair;

import com.explorestack.iab.vast.VastRequest;
import com.explorestack.iab.vast.processor.VastAd;

import java.io.File;
import java.lang.ref.WeakReference;

import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;

public class DownloadVastVideoTask implements Runnable {

    private static final String SUPPORTED_VIDEO_TYPE_REGEX = "video/.*(?i)(mp4|3gpp|mp2t|webm|matroska)";
    private static final String DIR_NAME = "native_video";
    private static final int SERVER_TIME_OUT = 20000;
    private static final int RESULT_FAIL = 0;
    private static final int RESULT_SUCCESS = 1;

    private final Handler handler;

    private WeakReference<Context> weakContext;
    private OnLoadedListener listener;
    private String videoTag;
    private File cacheDir;

    private boolean initialized;

    public interface OnLoadedListener {

        void onVideoLoaded(DownloadVastVideoTask task, Uri videoFileUri, VastRequest vastRequest);

        void onVideoLoadingError(DownloadVastVideoTask task);

    }

    public DownloadVastVideoTask(Context context, OnLoadedListener listener, String tag) {
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (DownloadVastVideoTask.this.listener != null) {
                    switch (msg.what) {
                        case RESULT_SUCCESS:
                            @SuppressWarnings("unchecked")
                            Pair<Uri, VastRequest> pair = (Pair<Uri, VastRequest>) msg.obj;
                            DownloadVastVideoTask.this.listener.onVideoLoaded(
                                    DownloadVastVideoTask.this,
                                    pair.first,
                                    pair.second);
                            break;
                        case RESULT_FAIL:
                            DownloadVastVideoTask.this.listener.onVideoLoadingError(
                                    DownloadVastVideoTask.this);
                            break;
                    }
                }
            }
        };
        if (context == null || tag == null || !Utils.canUseExternalFilesDir(context)) {
            listener.onVideoLoadingError(this);
            return;
        }

        this.weakContext = new WeakReference<>(context);
        this.listener = listener;
        videoTag = tag;
        if (Utils.canUseExternalFilesDir(context)) {
            cacheDir = Utils.getCacheDir(context, DIR_NAME);
        } else {
            listener.onVideoLoadingError(this);
            return;
        }
        initialized = true;
    }

    @Override
    public void run() {
        if (!initialized) {
            sendFail();
            return;
        }
        Context context = weakContext.get();
        if (context == null) {
            sendFail();
            return;
        }
        try {
            VastRequest vastRequest = VastRequest.newBuilder()
                    .setPreCache(false)
                    .build();
            vastRequest.loadVideoWithDataSync(context, videoTag, null);
            VastAd vastAd = vastRequest.getVastAd();
            if (vastAd != null) {
                if (vastAd.getPickedMediaFileTag().getType().matches(SUPPORTED_VIDEO_TYPE_REGEX)) {
                    String videoUrl = vastAd.getPickedMediaFileTag().getText();

                    File videoFile = CacheUtils.cacheVideoFile(videoUrl,
                                                               cacheDir,
                                                               SERVER_TIME_OUT);
                    if (videoFile != null) {
                        sendSuccess(videoFile, vastRequest);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            Logger.log(e);
        }

        sendFail();
    }

    private void sendSuccess(File file, VastRequest vastRequest) {
        if (handler != null) {
            Message message = handler.obtainMessage(RESULT_SUCCESS,
                                                    new Pair<>(Uri.fromFile(file), vastRequest));
            handler.sendMessage(message);
        }
    }

    private void sendFail() {
        if (handler != null) {
            handler.sendEmptyMessage(RESULT_FAIL);
        }
    }

}