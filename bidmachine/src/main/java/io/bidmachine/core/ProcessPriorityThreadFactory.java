package io.bidmachine.core;

import androidx.annotation.NonNull;

import java.util.concurrent.ThreadFactory;

final class ProcessPriorityThreadFactory implements ThreadFactory {
    private final int threadPriority;

    ProcessPriorityThreadFactory(int threadPriority) {
        this.threadPriority = threadPriority;
    }

    @Override
    public Thread newThread(@NonNull Runnable r) {
        Thread thread = new Thread(r);
        thread.setPriority(threadPriority);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        });
        return thread;
    }

}
