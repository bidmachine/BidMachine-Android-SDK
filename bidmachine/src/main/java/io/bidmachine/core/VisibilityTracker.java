package io.bidmachine.core;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class VisibilityTracker {

    private static final int NO_TRACK = -1;

    private static final ArrayList<TrackingHolder> holders = new ArrayList<>();

    private static class TrackingHolder {

        private static final int AFD_MAX_COUNT_OVERLAPPED_VIEWS = 3;
        private static final int CHECK_DELAY = 100;

        private final WeakReference<View> viewReference;
        private final long requiredOnScreenTime;
        private final float visibilityPercent;
        private final boolean ignoreCheckWindowFocus;
        private final VisibilityChangeCallback callback;

        private ViewTreeObserver.OnPreDrawListener preDrawListener;

        private long lastShownTimeMs;
        private boolean isCheckerScheduled;
        private boolean isShownTracked;
        private boolean isFinishedTracked;
        private boolean isFinishedRequested;

        TrackingHolder(@NonNull View view,
                       long requiredOnScreenTimeMs,
                       float visibilityPercent,
                       boolean ignoreCheckWindowFocus,
                       @NonNull VisibilityChangeCallback callback) {
            this.viewReference = new WeakReference<>(view);
            this.requiredOnScreenTime = requiredOnScreenTimeMs;
            this.visibilityPercent = visibilityPercent;
            this.ignoreCheckWindowFocus = ignoreCheckWindowFocus;
            this.callback = callback;
        }

        public void start() {
            final View view = viewReference.get();
            if (view == null) {
                release();
                return;
            }
            Logger.log(String.format("Start tracking - %s", view.toString()));

            if (preDrawListener == null) {
                preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        scheduleChecker();
                        return true;
                    }
                };
            }
            view.getViewTreeObserver().addOnPreDrawListener(preDrawListener);
        }

        private void scheduleChecker() {
            if (isCheckerScheduled) {
                return;
            }
            isCheckerScheduled = true;

            Utils.onUiThread(checkRunnable, CHECK_DELAY);
        }

        private boolean check() {
            final View view = viewReference.get();
            if (view == null) {
                release();
                return true;
            }
            if (isShownTracked && isFinishedTracked) {
                release();
                return true;
            }
            if (isOnTop(view, visibilityPercent, ignoreCheckWindowFocus)) {
                if (!isShownTracked) {
                    callback.onViewShown();
                    isShownTracked = true;
                }
                if (!isFinishedRequested && !isFinishedTracked) {
                    Utils.onUiThread(finishRunnable, requiredOnScreenTime);
                    lastShownTimeMs = System.currentTimeMillis();
                    isFinishedRequested = true;
                }
            } else {
                if (!isFinishedTracked) {
                    Utils.cancelUiThreadTask(finishRunnable);
                    isFinishedRequested = false;
                    lastShownTimeMs = 0;
                }
            }
            return false;
        }

        private void release() {
            final View view = viewReference.get();
            if (view != null) {
                Logger.log("Stop tracking - " + view.toString());

                if (isShownTracked
                        && !isFinishedTracked
                        && requiredOnScreenTime > NO_TRACK
                        && lastShownTimeMs > 0
                        && (System.currentTimeMillis() - lastShownTimeMs) >= requiredOnScreenTime) {
                    isFinishedTracked = true;
                    callback.onViewTrackingFinished();
                }
                view.getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
            }
            Utils.cancelUiThreadTask(checkRunnable);
            Utils.cancelUiThreadTask(finishRunnable);
            synchronized (holders) {
                holders.remove(this);
            }
        }

        public boolean isOnTop(@NonNull View view,
                               float visibilityPercent,
                               boolean ignoreCheckWindowFocus) {
            try {
                Rect viewRect = new Rect();
                if (!view.getGlobalVisibleRect(viewRect)) {
                    Logger.log(String.format(
                            "Show wasn't tracked: global visibility verification failed - %s",
                            view.toString()));
                    return false;
                }
                if (!view.isShown()) {
                    Logger.log(String.format(
                            "Show wasn't tracked: view visibility verification failed - %s",
                            view.toString()));
                    return false;
                }
                if (Utils.isViewTransparent(view)) {
                    Logger.log(String.format(
                            "Show wasn't tracked: view transparent verification failed - %s",
                            view.toString()));
                    return false;
                }
                if (!ignoreCheckWindowFocus && !view.hasWindowFocus()) {
                    Logger.log(String.format(
                            "Show wasn't tracked: window focus verification failed - %s",
                            view.toString()));
                    return false;
                }
                float totalAdViewArea = view.getWidth() * view.getHeight();
                if (totalAdViewArea == 0.0F) {
                    Logger.log(String.format(
                            "Show wasn't tracked: view size verification failed - %s",
                            view.toString()));
                    return false;
                }

                int viewArea = viewRect.width() * viewRect.height();
                float percentOnScreen = (viewArea / totalAdViewArea);
                if (percentOnScreen < visibilityPercent) {
                    Logger.log(String.format(
                            "Show wasn't tracked: ad view not completely visible (%s / %s) - %s",
                            percentOnScreen, visibilityPercent, view.toString()));
                    return false;
                }

                View content = (View) view.getParent();
                while (content != null && content.getId() != android.R.id.content) {
                    content = (View) content.getParent();
                }
                if (content == null) {
                    Logger.log(String.format(
                            "Show wasn't tracked: activity content layout not found - %s",
                            view.toString()));
                    return false;
                }
                Rect rootViewRect = new Rect();
                content.getGlobalVisibleRect(rootViewRect);
                if (!Rect.intersects(viewRect, rootViewRect)) {
                    Logger.log(String.format(
                            "Show wasn't tracked: ad view is out of current window - %s",
                            view.toString()));
                    return false;
                }

                ViewGroup rootView = (ViewGroup) view.getRootView();
                int countOverlappedViews = 0;
                ViewGroup parent = (ViewGroup) view.getParent();
                while (parent != null) {
                    int index = parent.indexOfChild(view);
                    for (int i = index + 1; i < parent.getChildCount(); i++) {
                        View child = parent.getChildAt(i);
                        if (child.getVisibility() == View.VISIBLE) {
                            int[] childLoc = new int[2];
                            child.getLocationInWindow(childLoc);
                            Rect childRect = Utils.getViewRectangle(child);
                            if (Rect.intersects(viewRect, childRect)) {
                                float visiblePercent = viewNotOverlappedAreaPercent(viewRect,
                                                                                    childRect);
                                Logger.log(String.format(
                                        "Show wasn't tracked: ad view is overlapped by another visible view (%s), visible percent: %s / %s",
                                        child.toString(),
                                        visiblePercent,
                                        visibilityPercent));
                                if (visiblePercent < visibilityPercent) {
                                    Logger.log(String.format(
                                            "Show wasn't tracked: ad view is covered by another view - %s",
                                            view.toString()));
                                    return false;
                                } else {
                                    countOverlappedViews++;
                                    if (countOverlappedViews >= AFD_MAX_COUNT_OVERLAPPED_VIEWS) {
                                        Logger.log(String.format(
                                                "Show wasn't tracked: ad view is covered by too many views - %s",
                                                view.toString()));
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                    if (parent != rootView) {
                        view = parent;
                        parent = (ViewGroup) view.getParent();
                    } else {
                        parent = null;
                    }
                }
            } catch (Exception e) {
                Logger.log(e.getMessage());
            }
            return true;
        }

        private static float viewNotOverlappedAreaPercent(Rect viewRect, Rect coverRect) {
            int viewArea = viewRect.width() * viewRect.height();
            if (viewArea == 0) {
                return 0;
            }
            int minRight = Math.min(viewRect.right, coverRect.right);
            int maxLeft = Math.max(viewRect.left, coverRect.left);
            int minBottom = Math.min(viewRect.bottom, coverRect.bottom);
            int maxTop = Math.max(viewRect.top, coverRect.top);
            int xOverlap = Math.max(0, minRight - maxLeft);
            int yOverlap = Math.max(0, minBottom - maxTop);
            int overlapArea = xOverlap * yOverlap;
            return ((float) (viewArea - overlapArea) / viewArea);
        }

        private final Runnable checkRunnable = new Runnable() {
            @Override
            public void run() {
                if (!check()) {
                    isCheckerScheduled = false;
                    scheduleChecker();
                }
            }
        };

        private final Runnable finishRunnable = new Runnable() {
            @Override
            public void run() {
                release();
            }
        };

    }

    public static void startTracking(@NonNull View view,
                                     long requiredOnScreenTimeMs,
                                     float visibilityPercent,
                                     boolean ignoreCheckWindowFocus,
                                     @NonNull VisibilityChangeCallback callback) {
        synchronized (holders) {
            stopTracking(view);
            TrackingHolder holder = new TrackingHolder(view,
                                                       requiredOnScreenTimeMs,
                                                       visibilityPercent,
                                                       ignoreCheckWindowFocus,
                                                       callback);
            holders.add(holder);
            holder.start();
        }
    }

    public static void stopTracking(@NonNull View view) {
        synchronized (holders) {
            for (TrackingHolder holder : holders) {
                if (holder.viewReference.get() == view) {
                    holder.release();
                    break;
                }
            }
        }
    }

    public interface VisibilityChangeCallback {
        void onViewShown();

        void onViewTrackingFinished();
    }

}
