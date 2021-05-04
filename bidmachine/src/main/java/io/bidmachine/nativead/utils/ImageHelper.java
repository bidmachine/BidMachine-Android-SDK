package io.bidmachine.nativead.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.io.InputStream;
import java.lang.ref.WeakReference;

import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;

public class ImageHelper {

    private static final int MAX_IMAGE_WIDTH = 1200;
    private static final int MAX_IMAGE_HEIGHT = 700;

    private static void loadImageByPath(Context context,
                                        Uri imageUri,
                                        ImageView targetImageView,
                                        OnImageHelperListener onImageHelperListener) {
        if (onImageHelperListener == null) {
            return;
        }
        if (imageUri == null || TextUtils.isEmpty(imageUri.getPath())) {
            onImageHelperListener.onError("ImagePath is invalid");
            return;
        }
        if (targetImageView == null) {
            onImageHelperListener.onError("Target ImageView is null");
            return;
        }
        NativeNetworkExecutor.getInstance().execute(new ImagePreparation(context,
                                                                         imageUri,
                                                                         targetImageView,
                                                                         onImageHelperListener));
    }

    public static int calculateReqWidth(Context context) {
        Point screenSize = Utils.getScreenSize(context);
        return Math.min(MAX_IMAGE_WIDTH, Math.min(screenSize.x, screenSize.y));
    }

    public static int calculateReqHeight(int maxWidth, boolean checkAspectRatio) {
        int maxHeight;
        if (checkAspectRatio) {
            maxHeight = (int) ((float) maxWidth / NativeConstants.MIN_MAIN_BITMAP_ASPECT_RATIO);
        } else {
            // noinspection SuspiciousNameCombination
            maxHeight = maxWidth;
        }
        if (maxHeight > MAX_IMAGE_HEIGHT) {
            maxHeight = MAX_IMAGE_HEIGHT;
        }
        return maxHeight;
    }

    public static int calculateInSamplesSize(BitmapFactory.Options options,
                                             int reqWidth,
                                             int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;
        while ((width / inSampleSize) > reqWidth || (height / inSampleSize) > reqHeight) {
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    public static void fillImageView(Context context,
                                     ImageView imageView,
                                     Uri imageUri,
                                     Bitmap imageBitmap) {
        if (imageBitmap != null) {
            imageView.setImageBitmap(imageBitmap);
        } else {
            ImageHelper.loadImageByPath(context,
                                        imageUri,
                                        imageView,
                                        new ImageHelper.OnImageHelperListener() {
                                            @Override
                                            public void onImagePrepared(@NonNull ImageView targetView,
                                                                        @NonNull Bitmap bitmap) {
                                                targetView.setImageBitmap(bitmap);
                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                Logger.log(errorMessage);
                                            }
                                        });
        }
    }


    private static class ImagePreparation implements Runnable {

        private final Uri imageUri;
        private final Context context;
        private final WeakReference<ImageView> weakTargetImageView;
        private final OnImageHelperListener onImageHelperListener;

        private Bitmap image;

        ImagePreparation(Context context,
                         Uri imageUri,
                         ImageView targetImageView,
                         OnImageHelperListener onImageHelperListener) {
            this.context = context;
            this.imageUri = imageUri;
            this.weakTargetImageView = new WeakReference<>(targetImageView);
            this.onImageHelperListener = onImageHelperListener;
        }

        @Override
        public void run() {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                InputStream stream = context.getContentResolver().openInputStream(imageUri);
                try {
                    BitmapFactory.decodeStream(stream, null, options);
                } finally {
                    Utils.close(stream);
                }

                if (options.outWidth == 0 || options.outHeight == 0) {
                    onImageHelperListener.onError("Image size is (0;0)");
                    return;
                }

                int reqWidth = calculateReqWidth(context);
                int reqHeight = calculateReqHeight(reqWidth, false);

                options.inSampleSize = calculateInSamplesSize(options, reqWidth, reqHeight);
                options.inJustDecodeBounds = false;
                stream = context.getContentResolver().openInputStream(imageUri);
                try {
                    image = BitmapFactory.decodeStream(stream, null, options);
                } finally {
                    Utils.close(stream);
                }
                Utils.onUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageView imageView = weakTargetImageView.get();
                        if (imageView != null && image != null) {
                            onImageHelperListener.onImagePrepared(imageView, image);
                        } else {
                            onImageHelperListener.onError("Target ImageView or Bitmap is invalid");
                        }
                    }
                });
            } catch (Throwable t) {
                if (t.getMessage() != null) {
                    onImageHelperListener.onError(t.getMessage());
                } else {
                    onImageHelperListener.onError("ImagePreparation error");
                }
            }
        }

    }

    public interface OnImageHelperListener {

        void onImagePrepared(@NonNull ImageView targetView, @NonNull Bitmap bitmap);

        void onError(String errorMessage);

    }

}