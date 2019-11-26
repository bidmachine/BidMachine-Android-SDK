package io.bidmachine.nativead.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;

import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;

public class DownloadVideoTask implements Runnable {

    private OnLoadedListener listener;
    private String videoUrl;
    private final static int serverTimeOut = 20000;
    private File cacheDir;
    private static final int RADIX = 10 + 26;
    private final Handler handler;
    private final int RESULT_FAIL = 0;
    private final int RESULT_SUCCESS = 1;
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
                            DownloadVideoTask.this.listener.onVideoLoaded(DownloadVideoTask.this, uri);
                            break;
                        case RESULT_FAIL:
                            DownloadVideoTask.this.listener.onVideoLoadingError(DownloadVideoTask.this);
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
        File externalStorage = context.getExternalFilesDir(null);
        if (externalStorage != null) {
            String dir = externalStorage.getPath() + "/native_video/";
            cacheDir = new File(dir);
            if (!cacheDir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                cacheDir.mkdirs();
            }
        } else {
            listener.onVideoLoadingError(this);
            return;
        }
        initialized = true;
    }

    @Override
    public void run() {
        if (!initialized) {
            handler.sendEmptyMessage(RESULT_FAIL);
            return;
        }
        InputStream inputStream = null;
        try {
            URL fileUrl = new URL(videoUrl);
            HttpURLConnection connection = (HttpURLConnection) fileUrl.openConnection();
            connection.setConnectTimeout(serverTimeOut);
            connection.setReadTimeout(serverTimeOut);
            inputStream = connection.getInputStream();
            String tempName = "temp" + System.currentTimeMillis();
            File file = new File(cacheDir, tempName);
            FileOutputStream fileOutput = new FileOutputStream(file);
            long totalSize = connection.getContentLength();
            long downloadedSize = 0;
            byte[] buffer = new byte[1024];
            int bufferLength;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
            }
            fileOutput.close();
            String fileName = generateFileName(videoUrl);
            if (totalSize == downloadedSize) {
                //noinspection ResultOfMethodCallIgnored
                file.renameTo(new File(cacheDir, fileName));
            }
            File result = new File(cacheDir, fileName);
            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(
                    result.getPath(),
                    MediaStore.Images.Thumbnails.MINI_KIND);
            if (thumb != null) {
                Message message = handler.obtainMessage(RESULT_SUCCESS, Uri.fromFile(result));
                handler.sendMessage(message);
                return;
            } else {
                Logger.log("Video file is not supported");
            }
        } catch (Exception e) {
            Logger.log(e);
        } finally {
            closeInputStream(inputStream);
        }
        handler.sendEmptyMessage(RESULT_FAIL);
    }

    private void closeInputStream(InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    private String generateFileName(String imageUri) {
        byte[] md5 = Utils.getMD5(imageUri.getBytes());
        BigInteger bi = new BigInteger(md5).abs();
        return bi.toString(RADIX);
    }

}