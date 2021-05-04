package io.bidmachine;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ContextProvider {

    @NonNull
    Context getApplicationContext();

    @NonNull
    Context getContext();

    @Nullable
    Activity getActivity();

}