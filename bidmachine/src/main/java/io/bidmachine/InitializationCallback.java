package io.bidmachine;

/**
 * Callback with SDK initialization notification
 */
public interface InitializationCallback {

    /**
     * Method will be called when SDK was successfully initialized
     */
    void onInitialized();
}
