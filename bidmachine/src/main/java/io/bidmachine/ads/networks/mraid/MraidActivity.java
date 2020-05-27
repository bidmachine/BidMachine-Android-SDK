package io.bidmachine.ads.networks.mraid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import com.explorestack.iab.utils.Assets;
import com.explorestack.iab.utils.CircularProgressBar;
import com.explorestack.iab.utils.Utils;
import com.explorestack.iab.vast.VideoType;
import com.explorestack.iab.vast.view.CircleCountdownView;
import io.bidmachine.utils.BMError;

public class MraidActivity extends Activity {

    private static final int CLOSE_REGION_SIZE = 50;

    private static MraidFullScreenAd mraidInterstitial;

    private CircularProgressBar progressBar;
    private CircleCountdownView circleCountdownView;

    private Handler handler;
    private Runnable showCloseTime;
    private boolean canSkip;

    public static void show(Context context,
                            MraidFullScreenAd mraidInterstitial,
                            VideoType adType) {
        MraidActivity.mraidInterstitial = mraidInterstitial;
        try {
            Intent adActivityIntent = new Intent(context, MraidActivity.class);
            adActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            adActivityIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            adActivityIntent.putExtra("type", adType);
            context.startActivity(adActivityIntent);
        } catch (Exception e) {
            e.printStackTrace();
            if (mraidInterstitial != null && mraidInterstitial.getCallback() != null) {
                mraidInterstitial.getCallback().onAdShowFailed(BMError.Internal);
            }
            MraidActivity.mraidInterstitial = null;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (!getIntent().hasExtra("type")) {
                finishActivity();
                return;
            }
            VideoType type = (VideoType) getIntent().getSerializableExtra("type");
            //noinspection ResourceType
            setRequestedOrientation(Utils.getScreenOrientation(this));
            if (type == VideoType.NonRewarded) {
                canSkip = true;
            }
            hideTitleStatusBars();
            addBackgroundView();
            if (mraidInterstitial != null) {
                mraidInterstitial.setShowingActivity(this);
                showMraidInterstitial();
            }
        } catch (Exception e) {
            if (mraidInterstitial != null && mraidInterstitial.getCallback() != null) {
                mraidInterstitial.getCallback().onAdShowFailed(BMError.Internal);
            }
            finishActivity();
        }
    }

    @Override
    public void onBackPressed() {
        if (canSkip) {
            finishActivity();
        }
    }

    @Override
    protected void onDestroy() {
        if (mraidInterstitial != null) {
            mraidInterstitial.setShowingActivity(null);
            verifyClosedDispatched(mraidInterstitial);
            if (mraidInterstitial.getMraidInterstitial() != null) {
                mraidInterstitial.getMraidInterstitial().destroy();
            }
            mraidInterstitial = null;
        }
        super.onDestroy();
    }

    private void showMraidInterstitial() {
        if (mraidInterstitial != null && mraidInterstitial.getMraidInterstitial() != null) {
            mraidInterstitial.getMraidInterstitial().show(this, true);
        }
    }

    public void showProgressBar() {
        if (progressBar == null) {
            progressBar = new CircularProgressBar(this);
            progressBar.setColorSchemeColors(Assets.mainAssetsColor);
            progressBar.setProgressBackgroundColor(Assets.backgroundColor);
            FrameLayout.LayoutParams spinnerParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            spinnerParams.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
            progressBar.setLayoutParams(spinnerParams);
            addContentView(progressBar, spinnerParams);
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void showProgressBarWithCloseTime(long closeTimeSec) {
        showProgressBar();
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CLOSE_REGION_SIZE,
                getResources().getDisplayMetrics());
        circleCountdownView = new CircleCountdownView(this);
        circleCountdownView.setBackgroundColor(Color.TRANSPARENT);
        circleCountdownView.setVisibility(View.GONE);
        circleCountdownView.setImage(Assets.getBitmapFromBase64(Assets.close));
        circleCountdownView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mraidInterstitial != null && mraidInterstitial.getCallback() != null) {
                    mraidInterstitial.getCallback().onAdShowFailed(BMError.TimeoutError);
                }
                finishActivity();
            }
        });
        FrameLayout.LayoutParams spinnerParams = new FrameLayout.LayoutParams(size, size);
        spinnerParams.gravity = Gravity.TOP | Gravity.END;
        addContentView(circleCountdownView, spinnerParams);
        showCloseTime = new Runnable() {
            @Override
            public void run() {
                if (circleCountdownView != null) {
                    circleCountdownView.setVisibility(View.VISIBLE);
                    circleCountdownView.setClickable(true);
                }
            }
        };
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(showCloseTime, closeTimeSec * 1000);
    }

    public void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    public void hideProgressBarWithCloseTime() {
        hideProgressBar();
        if (handler != null && showCloseTime != null) {
            handler.removeCallbacks(showCloseTime);
        }
        if (circleCountdownView != null) {
            circleCountdownView.setVisibility(View.GONE);
        }
    }

    private void finishActivity() {
        finish();
        overridePendingTransition(0, 0);
        if (mraidInterstitial != null) {
            mraidInterstitial.setShowingActivity(null);
            verifyClosedDispatched(mraidInterstitial);
            mraidInterstitial = null;
        }
    }

    private void hideTitleStatusBars() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void addBackgroundView() {
        RelativeLayout mainView = new RelativeLayout(this);
        RelativeLayout.LayoutParams mainViewParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mainView.setLayoutParams(mainViewParams);
        mainView.setBackgroundColor(Color.parseColor("#7F000000"));
        setContentView(mainView);
    }

    private synchronized void verifyClosedDispatched(@Nullable MraidFullScreenAd mraidInterstitial) {
        if (mraidInterstitial != null
                && mraidInterstitial.getMraidInterstitial() != null
                && !mraidInterstitial.getMraidInterstitial().isClosed()) {
            mraidInterstitial.getMraidInterstitial().dispatchClose();
        }
    }
}