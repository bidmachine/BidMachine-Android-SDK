package io.bidmachine.nativead;

/**
 * Interface for working with native ads
 */
public interface NativePublicData {

    /**
     * Get title of native ad
     *
     * @return title string
     */
    String getTitle();

    /**
     * Get description of native ad
     *
     * @return description string
     */
    String getDescription();

    /**
     * Get call to action string of native ad, to show on the button
     *
     * @return call to action string
     */
    String getCallToAction();

    /**
     * Get native ad rating
     *
     * @return ad rating
     */
    float getRating();

    /**
     * @return {@code true} if native ad has video
     */
    boolean hasVideo();

}
