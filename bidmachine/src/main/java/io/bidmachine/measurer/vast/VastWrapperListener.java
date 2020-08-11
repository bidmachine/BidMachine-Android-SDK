package io.bidmachine.measurer.vast;

import android.content.Context;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.explorestack.iab.vast.VastActivityListener;
import com.explorestack.iab.vast.VastClickCallback;
import com.explorestack.iab.vast.VastPlaybackListener;
import com.explorestack.iab.vast.VastRequest;
import com.explorestack.iab.vast.activity.VastActivity;
import com.explorestack.iab.vast.activity.VastView;
import com.explorestack.iab.vast.processor.VastAd;

import io.bidmachine.core.Utils;

public class VastWrapperListener implements VastActivityListener, VastPlaybackListener {

    private final VastIABMeasurer measurer;
    private final VastActivityListener vastActivityListener;
    private final VastPlaybackListener playbackListener;

    public VastWrapperListener(@NonNull VastIABMeasurer measurer,
                               @Nullable VastActivityListener vastActivityListener,
                               @Nullable VastPlaybackListener playbackListener) {
        this.measurer = measurer;
        this.vastActivityListener = vastActivityListener;
        this.playbackListener = playbackListener;
    }

    /*
        VastActivity events
    */
    @Override
    public void onVastShown(@NonNull final VastActivity vastActivity,
                            @NonNull final VastRequest vastRequest) {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                VastView vastView = vastActivity.getVastView();
                if (vastView == null) {
                    return;
                }
                if (!measurer.isSessionStarted()) {
                    VastAd vastAd = vastRequest.getVastAd();
                    if (vastAd != null) {
                        measurer.addVerificationScriptResources(
                                VerificationResource.transformToList(vastAd.getAdVerificationsExtension())
                        );
                    }
                    measurer.setAutoPlay(true);
                    measurer.setSkipOffset(vastView.getSkipTime());
                    measurer.configure(vastView.getContext(), vastView);
                    measurer.registerAdView(vastView);
                    measurer.startSession();
                    measurer.loaded();
                    measurer.shown();
                }
            }
        });

        if (vastActivityListener != null) {
            vastActivityListener.onVastShown(vastActivity, vastRequest);
        }
    }

    @Override
    public void onVastClick(@NonNull VastActivity vastActivity,
                            @NonNull VastRequest vastRequest,
                            @NonNull VastClickCallback clickCallback,
                            @Nullable String url) {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                measurer.clicked();
            }
        });

        if (vastActivityListener != null) {
            vastActivityListener.onVastClick(vastActivity, vastRequest, clickCallback, url);
        }
    }

    @Override
    public void onVastComplete(@NonNull VastActivity vastActivity,
                               @NonNull VastRequest vastRequest) {
        if (vastActivityListener != null) {
            vastActivityListener.onVastComplete(vastActivity, vastRequest);
        }
    }

    @Override
    public void onVastDismiss(@NonNull VastActivity vastActivity,
                              @Nullable VastRequest vastRequest,
                              boolean finished) {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                measurer.destroy();
            }
        });

        if (vastActivityListener != null) {
            vastActivityListener.onVastDismiss(vastActivity, vastRequest, finished);
        }
    }

    @Override
    public void onVastError(@NonNull Context context, @NonNull VastRequest vastRequest, int error) {
        if (vastActivityListener != null) {
            vastActivityListener.onVastError(context, vastRequest, error);
        }
    }

    /*
        Playback events
    */
    @Override
    public void started(final float duration, @FloatRange(from = 0, to = 1) final float volume) {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                measurer.videoStarted(duration, volume);
            }
        });

        if (playbackListener != null) {
            playbackListener.started(duration, volume);
        }
    }

    @Override
    public void firstQuartile() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                measurer.videoFirstQuartile();
            }
        });

        if (playbackListener != null) {
            playbackListener.firstQuartile();
        }
    }

    @Override
    public void midpoint() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                measurer.videoMidpoint();
            }
        });

        if (playbackListener != null) {
            playbackListener.midpoint();
        }
    }

    @Override
    public void thirdQuartile() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                measurer.videoThirdQuartile();
            }
        });

        if (playbackListener != null) {
            playbackListener.thirdQuartile();
        }
    }

    @Override
    public void complete() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                measurer.videoCompleted();
            }
        });

        if (playbackListener != null) {
            playbackListener.complete();
        }
    }

    @Override
    public void pause() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                measurer.videoPaused();
            }
        });

        if (playbackListener != null) {
            playbackListener.pause();
        }
    }

    @Override
    public void resume() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                measurer.videoResumed();
            }
        });

        if (playbackListener != null) {
            playbackListener.resume();
        }
    }

    @Override
    public void volumeChanged(@FloatRange(from = 0, to = 1) final float volume) {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                measurer.videoVolumeChanged(volume);
            }
        });

        if (playbackListener != null) {
            playbackListener.volumeChanged(volume);
        }
    }

    @Override
    public void skipped() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                measurer.videoSkipped();
            }
        });

        if (playbackListener != null) {
            playbackListener.skipped();
        }
    }

}