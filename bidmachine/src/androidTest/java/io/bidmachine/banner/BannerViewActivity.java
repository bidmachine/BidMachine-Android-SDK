package io.bidmachine.banner;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

public class BannerViewActivity extends Activity {

    public FrameLayout parentFrame;
    public BannerView bannerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentFrame = new FrameLayout(this);
        bannerView = new BannerView(this);
        parentFrame.addView(bannerView);
        setContentView(parentFrame);
    }

    public void setBannerViewVisibility(final int visibility) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bannerView.setVisibility(visibility);
            }
        });
    }

    public void setBannerViewSize(final int width, final int height) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bannerView.setLayoutParams(new FrameLayout.LayoutParams(width, height));
            }
        });
    }
}
