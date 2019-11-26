package io.bidmachine.nativead;

/**
 * Interface to work with native ads
 */
public interface NativePublicData {

    /**
     * Gets title of native ad
     *
     * @return title string
     */
    String getTitle();

    /**
     * Gets description of native ad
     *
     * @return description string
     */
    String getDescription();

    /**
     * Gets call to action string of native ad, to show on the button
     *
     * @return call to action string
     */
    String getCallToAction();

    /**
     * Gets native ad rating
     *
     * @return ad rating
     */
    float getRating();

    /**
     * @return {@code true} if native ad has video
     */
    boolean hasVideo();

}
