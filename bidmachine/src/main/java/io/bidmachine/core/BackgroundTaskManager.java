package io.bidmachine.core;

import androidx.annotation.NonNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BackgroundTaskManager implements Executor {

    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;

    private final ThreadPoolExecutor serviceTaskThreadPool;

    private static final BackgroundTaskManager instance = new BackgroundTaskManager();

    public static void async(@NonNull Runnable task) {
        instance.execute(task);
    }

    private BackgroundTaskManager() {
        BlockingQueue<Runnable> mServiceTaskQueue = new LinkedBlockingQueue<>();
        RejectedExecutionHandler rejectedExecutionHandlerForServiceTask = new BidMachineRejectedExecutionHandler();
        ThreadFactory threadFactory = new ProcessPriorityThreadFactory(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        serviceTaskThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mServiceTaskQueue, threadFactory,
                rejectedExecutionHandlerForServiceTask);
    }

    @Override
    public void execute(@NonNull Runnable task) {
        serviceTaskThreadPool.execute(task);
    }

    private final static class BidMachineRejectedExecutionHandler implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
            //ignore
        }

    }

}
