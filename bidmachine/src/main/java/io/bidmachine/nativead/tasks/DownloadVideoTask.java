package io.bidmachine.nativead.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.File;

import io.bidmachine.core.Utils;

public class DownloadVideoTask implements Runnable {

    private static final String DIR_NAME = "native_video";
    private final static int SERVER_TIME_OUT = 20000;
    private static final int RESULT_FAIL = 0;
    private static final int RESULT_SUCCESS = 1;

    private OnLoadedListener listener;
    private String videoUrl;
    private File cacheDir;
    private final Handler handler;
    private boolean initialized;

    public interface OnLoadedListener {

        void onVideoLoaded(DownloadVideoTask task, Uri videoFileUri);

        void onVideoLoadingError(DownloadVideoTask task);

    }

    public DownloadVideoTask(Context context, OnLoadedListener listener, String url) {
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (DownloadVideoTask.this.listener != null) {
                    switch (msg.what) {
                        case RESULT_SUCCESS:
                            Uri uri = (Uri) msg.obj;
                            DownloadVideoTask.this.listener.onVideoLoaded(
                                    DownloadVideoTask.this,
                                    uri);
                            break;
                        case RESULT_FAIL:
                            DownloadVideoTask.this.listener.onVideoLoadingError(
                                    DownloadVideoTask.this);
                            break;
                    }
                }
            }
        };
        if (context == null || url == null || !Utils.canUseExternalFilesDir(context)) {
            listener.onVideoLoadingError(this);
            return;
        }

        this.listener = listener;
        videoUrl = url;
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
        File videoFile = CacheUtils.cacheVideoFile(videoUrl, cacheDir, SERVER_TIME_OUT);
        if (videoFile != null) {
            sendSuccess(videoFile);
        } else {
            sendFail();
        }
    }

    private void sendSuccess(File file) {
        if (handler != null) {
            Message message = handler.obtainMessage(RESULT_SUCCESS, Uri.fromFile(file));
            handler.sendMessage(message);
        }
    }

    private void sendFail() {
        if (handler != null) {
            handler.sendEmptyMessage(RESULT_FAIL);
        }
    }

}