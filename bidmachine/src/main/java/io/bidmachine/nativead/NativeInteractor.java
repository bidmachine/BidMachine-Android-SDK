package io.bidmachine.nativead;

public interface NativeInteractor {
    /**
     * Should be called when Native Ads was shown
     */
    void dispatchShown();

    /**
     * Should be called when Native Ads matches viewability requirements
     */
    void dispatchImpression();

    /**
     * Should be called when Native Ads was clicked
     */
    void dispatchClick();

    /**
     * Should be called when Native Ads video was finished (optional)
     */
    void dispatchVideoPlayFinished();
}
