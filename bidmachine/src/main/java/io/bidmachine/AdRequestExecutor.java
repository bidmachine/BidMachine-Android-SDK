package io.bidmachine;

import androidx.annotation.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class AdRequestExecutor extends ThreadPoolExecutor {

    private static volatile AdRequestExecutor instance;

    public static AdRequestExecutor get() {
        if (instance == null) {
            synchronized (AdRequestExecutor.class) {
                if (instance == null) {
                    instance = new AdRequestExecutor(
                            Runtime.getRuntime().availableProcessors() * 2);
                }
            }
        }
        return instance;
    }

    private List<Runnable> pendingCommands;
    private boolean isEnabled = false;

    @VisibleForTesting
    AdRequestExecutor(int nThreads) {
        super(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

    @Override
    public void execute(Runnable command) {
        if (isEnabled) {
            super.execute(command);
        } else {
            // To prevent unnecessary synchronization we shouldn't wrap all 'execute' logic
            synchronized (AdRequestExecutor.this) {
                // Since we can rich this block during processing enabling logic, we should check
                // enabled state
                if (isEnabled) {
                    super.execute(command);
                } else {
                    if (pendingCommands == null) {
                        pendingCommands = new ArrayList<>();
                    }
                    pendingCommands.add(command);
                }
            }
        }
    }

    void enable() {
        isEnabled = true;
        synchronized (AdRequestExecutor.this) {
            if (pendingCommands != null) {
                for (Runnable command : pendingCommands) {
                    execute(command);
                }
                pendingCommands.clear();
            }
        }
    }

}