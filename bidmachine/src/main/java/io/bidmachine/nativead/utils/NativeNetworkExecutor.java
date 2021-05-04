package io.bidmachine.nativead.utils;

import androidx.annotation.NonNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NativeNetworkExecutor implements Executor {

    private static NativeNetworkExecutor executor = null;

    private final ThreadPoolExecutor loaderExecutor;

    public static NativeNetworkExecutor getInstance() {
        if (executor == null) {
            executor = new NativeNetworkExecutor();
        }
        return executor;
    }

    private NativeNetworkExecutor() {
        int threadCount = Runtime.getRuntime().availableProcessors() * 2;
        BlockingQueue<Runnable> loaderQueue = new LinkedBlockingQueue<>();
        loaderExecutor = new ThreadPoolExecutor(threadCount,
                                                threadCount,
                                                0L,
                                                TimeUnit.MICROSECONDS,
                                                loaderQueue);
    }

    @Override
    public void execute(@NonNull Runnable command) {
        loaderExecutor.execute(command);
    }

}