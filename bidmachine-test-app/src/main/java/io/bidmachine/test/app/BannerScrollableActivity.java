package io.bidmachine.test.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class BannerScrollableActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.banner_scrollable_activity);

        final int[] frameIds = {R.id.frame1, R.id.frame2, R.id.frame3, R.id.frame4};
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        for (int frameId : frameIds) {
            View view = findViewById(frameId);
            view.setLayoutParams(new LinearLayout.LayoutParams(metrics.widthPixels, metrics.heightPixels));
        }

    }

}
