package io.bidmachine.nativead.tasks;

import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;

class CacheUtils {

    @Nullable
    static File cacheVideoFile(String url, File cacheDir, int timeOut) {
        InputStream inputStream = null;
        try {
            inputStream = ConnectionUtils.getInputStream(url, timeOut);
            String fileName = Utils.generateFileName(url);
            File file = new File(cacheDir, fileName);
            if (file.exists() && file.length() > 0 && isVideoFileSupported(file)) {
                return file;
            }
            FileOutputStream fileOutput = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int bufferLength;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
            }
            fileOutput.close();
            if (isVideoFileSupported(file)) {
                return file;
            }
        } catch (Exception e) {
            Logger.log(e);
        } finally {
            Utils.close(inputStream);
        }
        return null;
    }

    private static boolean isVideoFileSupported(@NonNull File file) {
        return ThumbnailUtils.createVideoThumbnail(file.getPath(),
                                                   MediaStore.Images.Thumbnails.MINI_KIND) != null;
    }

}