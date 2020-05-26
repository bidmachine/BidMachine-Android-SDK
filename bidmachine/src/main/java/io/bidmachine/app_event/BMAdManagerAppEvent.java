package io.bidmachine.app_event;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class BMAdManagerAppEvent {

    static final String TAG = "BMAdManagerAppEvent";

    final String adUnitId;

    @Nullable
    BMAdManagerAppEventListener listener;
    @Nullable
    EventTracker eventTracker;

    boolean isDestroyed = true;

    BMAdManagerAppEvent(String adUnitId) {
        this.adUnitId = adUnitId;
    }

    public void setListener(final @Nullable BMAdManagerAppEventListener adManagerAppEventListener) {
        if (adManagerAppEventListener == null) {
            this.listener = null;
        }
        assert adManagerAppEventListener != null;
        this.listener = new BMAdManagerAppEventUIListener(adManagerAppEventListener);
    }

    public void load(@NonNull final Context context) {
        eventTracker = new EventTracker();
        isDestroyed = false;
    }

    public abstract boolean isLoaded();

    public abstract void show(@NonNull final Context context);

    public void hide() {

    }

    public void destroy() {
        eventTracker = null;
        isDestroyed = true;
    }

}