package io.bidmachine;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

final class SimpleContextProvider implements ContextProvider {

    @NonNull
    private Context context;

    SimpleContextProvider(@NonNull Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Context getContext() {
        return context;
    }

    @Nullable
    @Override
    public Activity getActivity() {
        Context context = getContext();
        if (context instanceof Activity) {
            return (Activity) context;
        }
        return BidMachineImpl.getTopActivity();
    }
}