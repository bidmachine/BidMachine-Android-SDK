package io.bidmachine;

import io.bidmachine.utils.BMError;

public interface AdProcessCallback {

    void processLoadSuccess();

    void processLoadFail(BMError error);

    void processShown();

    void processShowFail(BMError error);

    void processClicked();

    void processImpression();

    void processFinished();

    void processClosed();

    void processExpired();

    void processDestroy();

    void log(String message);

}