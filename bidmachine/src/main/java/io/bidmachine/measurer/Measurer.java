package io.bidmachine.measurer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

public interface Measurer {

    boolean configure(@NonNull Context context, @NonNull View view);

    void registerAdView(@NonNull View view);

    void addIgnoredView(@NonNull View view);

    void removeIgnoredView(@NonNull View view);

    void startSession();

    void loaded();

    void shown();

    void clicked();

    void stopSession();

    void destroy();

    interface InitListener {

        void onInitialized();

        void onInitializeFail(@NonNull String reason);

    }

}