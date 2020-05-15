package io.bidmachine.app_event;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class BMAdManagerAppEvent {

    static final String TAG = "BMAdManagerAppEvent";

    final String adUnitId;

    @Nullable
    BMAdManagerAppEventListener listener;

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

    public abstract void load(@NonNull final Context context);

    public abstract boolean isLoaded();

    public abstract void show(@NonNull final Context context);

    public void hide() {

    }

    public abstract void destroy();

}