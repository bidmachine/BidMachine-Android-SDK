package io.bidmachine.nativead.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.explorestack.iab.utils.Assets;
import com.explorestack.iab.vast.view.CircleCountdownView;

import io.bidmachine.core.Logger;

import static io.bidmachine.core.Utils.getScreenDensity;

public class VideoPlayerActivity extends Activity implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {

    private final static String FILE_URI = "io.bidmachine.fileUri";
    private final static String SEEK_TO = "io.bidmachine.seekTo";
    private int seekTo;
    private VideoView videoView;
    private VideoPlayerActivityListener listener;

    interface VideoPlayerActivityListener {
        void videoPlayerActivityClosed(int position, boolean finished);
    }

    public static Intent getIntent(Context packageContext, String fileUri, int seekTo) {
        Intent intent = new Intent(packageContext, VideoPlayerActivity.class);
        intent.putExtra(FILE_URI, fileUri);
        intent.putExtra(SEEK_TO, seekTo);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = getIntent();
        String fileUri = intent.getStringExtra(FILE_URI);
        seekTo = intent.getIntExtra(SEEK_TO, 0);
        Logger.log(String.format("VideoPlayerActivity started, position: %s", seekTo));
        if (fileUri == null) {
            return;
        }
        listener = MediaView.listener;
        RelativeLayout rootLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams rootViewParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        rootLayout.setLayoutParams(rootViewParams);

        videoView = new VideoView(this);
        RelativeLayout.LayoutParams videoViewParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        videoViewParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        videoView.setLayoutParams(videoViewParams);
        videoView.setOnCompletionListener(this);
        videoView.setOnPreparedListener(this);
        videoView.setVideoPath(fileUri);
        rootLayout.addView(videoView);

        int closeButtonSize = Math.round(24 * getScreenDensity(this));
        int margin = Math.round(8 * getScreenDensity(this));
        CircleCountdownView closeButton = new CircleCountdownView(this);
        closeButton.setColors(Assets.mainAssetsColor, Assets.backgroundColor);
        RelativeLayout.LayoutParams closeButtonParams = new RelativeLayout.LayoutParams(closeButtonSize, closeButtonSize);
        closeButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        closeButtonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        closeButtonParams.setMargins(margin, margin, margin, margin);
        closeButton.setImage(Assets.getBitmapFromBase64(Assets.close));
        closeButton.setLayoutParams(closeButtonParams);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeClicked();
            }
        });
        rootLayout.addView(closeButton);
        setContentView(rootLayout);
    }

    private void finishActivity() {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } catch (Exception e) {
                    Logger.log(e);
                }
            }
        });
        finish();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (listener != null) {
            listener.videoPlayerActivityClosed(0, true);
        }
        this.finishActivity();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        this.finishActivity();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (videoView != null && videoView.canSeekForward()) {
            videoView.seekTo(seekTo);
            videoView.start();
        }
    }

    @Override
    public void onBackPressed() {
        closeClicked();
    }

    private void closeClicked() {
        if (listener != null) {
            int currentPosition = 0;
            if (videoView.isPlaying()) {
                currentPosition = videoView.getCurrentPosition();
            }
            listener.videoPlayerActivityClosed(currentPosition, false);
        }
        finishActivity();
    }

}
