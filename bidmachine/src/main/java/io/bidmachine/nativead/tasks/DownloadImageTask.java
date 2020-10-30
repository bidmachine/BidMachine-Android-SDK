package io.bidmachine.nativead.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;
import io.bidmachine.nativead.utils.ImageHelper;
import io.bidmachine.nativead.utils.NativeConstants;

public class DownloadImageTask implements Runnable {

    private static final String DIR_NAME = "native_cache_image";
    private static final int SERVER_TIME_OUT = 20000;
    private static final int RESULT_FAIL = 0;
    private static final int RESULT_PATH_SUCCESS = 1;
    private static final int RESULT_IMAGE_SUCCESS = 2;

    private final Handler handler;
    private Context context;
    private String url;
    private File cacheDir;
    private boolean checkAspectRatio;

    @Nullable
    private OnCacheImageListener listener;

    public static Builder newBuilder(Context context, String url) {
        return new DownloadImageTask().new Builder(context, url);
    }

    public class Builder {

        public Builder(Context context, String url) {
            DownloadImageTask.this.context = context;
            DownloadImageTask.this.url = url;
        }

        public DownloadImageTask.Builder setCheckAspectRatio(boolean checkAspectRatio) {
            DownloadImageTask.this.checkAspectRatio = checkAspectRatio;
            return this;
        }

        public DownloadImageTask.Builder setOnCacheImageListener(OnCacheImageListener listener) {
            DownloadImageTask.this.listener = listener;
            return this;
        }

        public DownloadImageTask build() {
            try {
                return DownloadImageTask.this;
            } finally {
                if (context == null) {
                    if (listener != null) {
                        listener.onFail(DownloadImageTask.this);
                    }
                } else if (Utils.canUseExternalFilesDir(context)) {
                    cacheDir = Utils.getCacheDir(context, DIR_NAME);
                }
            }
        }
    }

    private DownloadImageTask() {
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (DownloadImageTask.this.listener != null) {
                    switch (msg.what) {
                        case RESULT_PATH_SUCCESS:
                            DownloadImageTask.this.listener.onPathSuccess(
                                    DownloadImageTask.this, (Uri) msg.obj);
                            break;
                        case RESULT_IMAGE_SUCCESS:
                            DownloadImageTask.this.listener.onImageSuccess(
                                    DownloadImageTask.this, (Bitmap) msg.obj);
                            break;
                        case RESULT_FAIL:
                            DownloadImageTask.this.listener.onFail(
                                    DownloadImageTask.this);
                            break;
                    }
                }
            }
        };
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(url) || !Utils.isHttpUrl(url)) {
            sendFail();
            return;
        }
        url = url.replace(" ", "%20");
        downloadImage(context, url);
    }

    private void sendPathSuccess(Uri uri) {
        if (handler != null) {
            Message message = handler.obtainMessage(RESULT_PATH_SUCCESS, uri);
            handler.sendMessage(message);
        }
    }

    private void sendImageSuccess(Bitmap image) {
        if (handler != null) {
            Message message = handler.obtainMessage(RESULT_IMAGE_SUCCESS, image);
            handler.sendMessage(message);
        }
    }

    private void sendFail() {
        if (handler != null) {
            handler.sendEmptyMessage(RESULT_FAIL);
        }
    }

    private void downloadImage(Context context, String url) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = true;
        File file = null;
        if (cacheDir != null) {
            file = new File(cacheDir, Utils.generateFileName(url));
            if (file.exists() && file.length() > 0) {
                BitmapFactory.decodeFile(file.getPath(), options);
                if (isAspectRatioCorrect(options)) {
                    sendPathSuccess(Uri.fromFile(file));
                } else {
                    sendFail();
                }
                return;
            }
        }
        InputStream inputStream = null;
        ByteArrayOutputStream byteBuffer = null;
        try {
            inputStream = ConnectionUtils.getInputStream(url, SERVER_TIME_OUT);
            byteBuffer = new ByteArrayOutputStream(inputStream.available());

            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            byte[] imageByte = byteBuffer.toByteArray();
            BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length, options);
            if (!isAspectRatioCorrect(options)) {
                sendFail();
                return;
            }
            if (file != null) {
                saveImage(file, imageByte, options);
                sendPathSuccess(Uri.fromFile(file));
            } else {
                int reqWidth = ImageHelper.calculateReqWidth(context);
                int reqHeight = ImageHelper.calculateReqHeight(reqWidth, checkAspectRatio);
                options.inSampleSize = ImageHelper.calculateInSamplesSize(options,
                                                                          reqWidth,
                                                                          reqHeight);
                Bitmap imageBitmap = convert(imageByte, options);
                if (imageBitmap != null) {
                    sendImageSuccess(imageBitmap);
                } else {
                    sendFail();
                }
            }
        } catch (Throwable t) {
            Logger.log(t);
            sendFail();
        } finally {
            Utils.flush(byteBuffer);
            Utils.close(byteBuffer);
            Utils.close(inputStream);
        }
    }

    private boolean isAspectRatioCorrect(BitmapFactory.Options options) {
        if (checkAspectRatio) {
            int imageWidth = options.outWidth;
            int imageHeight = options.outHeight;
            float bitmapAspectRatio = (float) imageWidth / imageHeight;
            return !(bitmapAspectRatio < NativeConstants.MIN_MAIN_BITMAP_ASPECT_RATIO);
        }
        return true;
    }

    private void saveImage(File file, byte[] byteImage, BitmapFactory.Options options) {
        options.inJustDecodeBounds = false;
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            writeBitmap(byteImage, options, fileOutputStream);
        } catch (Exception e) {
            Logger.log(e);
        } finally {
            Utils.flush(fileOutputStream);
            Utils.close(fileOutputStream);
        }
    }

    private Bitmap convert(byte[] byteImage, BitmapFactory.Options options) {
        options.inJustDecodeBounds = false;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream(byteImage.length);
            writeBitmap(byteImage, options, byteArrayOutputStream);
            return BitmapFactory.decodeStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        } catch (Exception e) {
            Logger.log(e);
        } finally {
            Utils.flush(byteArrayOutputStream);
            Utils.close(byteArrayOutputStream);
        }
        return null;
    }

    private void writeBitmap(byte[] byteImage,
                             BitmapFactory.Options options,
                             OutputStream outputStream) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length, options);
        bitmap.compress(Bitmap.CompressFormat.PNG, 85, outputStream);
        bitmap.recycle();
    }

    public interface OnCacheImageListener {
        void onPathSuccess(DownloadImageTask task, Uri imagePath);

        void onImageSuccess(DownloadImageTask task, Bitmap imageBitmap);

        void onFail(DownloadImageTask task);
    }

}