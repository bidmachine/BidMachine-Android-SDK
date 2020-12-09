package io.bidmachine;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;

import java.util.List;

import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;
import io.bidmachine.models.AdObject;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.models.AuctionResult;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.utils.BMError;

public abstract class BidMachineAd<
        SelfType extends IAd,
        AdRequestType extends AdRequest<AdRequestType, UnifiedAdRequestParamsType>,
        AdObjectType extends AdObject<AdObjectParamsType, UnifiedAdRequestParamsType, ?>,
        AdObjectParamsType extends AdObjectParams,
        UnifiedAdRequestParamsType extends UnifiedAdRequestParams,
        AdListenerType extends AdListener<SelfType>>
        implements IAd<SelfType, AdRequestType> {

    @NonNull
    private final Context context;
    @NonNull
    private final AdsType adsType;
    @Nullable
    @VisibleForTesting
    AdRequestType adRequest;
    @Nullable
    private AdListenerType listener;
    @Nullable
    private AdObjectType loadedObject;
    @NonNull
    private State currentState = State.Idle;

    private boolean isShownTracked;
    private boolean isImpressionTracked;
    private boolean isClickTracked;
    private boolean isFinishTracked;
    private boolean isCloseTracked;

    @NonNull
    private final ContextProvider contextProvider;

    @NonNull
    @VisibleForTesting
    final TrackingObject trackingObject = new TrackingObject() {
        @Override
        public Object getTrackingKey() {
            AuctionResult auctionResult = getAuctionResult();
            if (auctionResult != null) {
                return auctionResult.getId();
            }
            return "-1";
        }

        @Nullable
        @Override
        public List<String> getTrackingUrls(@NonNull TrackEventType eventType) {
            return loadedObject != null && loadedObject.getParams() != null
                    ? loadedObject.getParams().getTrackUrls(eventType) : null;
        }
    };

    public BidMachineAd(@NonNull Context context, @NonNull AdsType adsType) {
        this.context = context;
        this.adsType = adsType;
        contextProvider = new SimpleContextProvider(context);
    }

    @NonNull
    Context getContext() {
        return context;
    }

    @Nullable
    @Override
    public AuctionResult getAuctionResult() {
        return adRequest != null ? adRequest.getAuctionResult() : null;
    }

    @Nullable
    protected final AdObjectType getLoadedObject() {
        return loadedObject;
    }

    @SuppressWarnings("unchecked")
    public SelfType setListener(@Nullable AdListenerType listener) {
        this.listener = listener;
        return (SelfType) this;
    }

    @NonNull
    protected final AdsType getType() {
        return adsType;
    }

    @Nullable
    public AdRequestType getAdRequest() {
        return adRequest;
    }

    /*
    Processing logic
     */

    @Override
    @SuppressWarnings("unchecked")
    public SelfType load(AdRequestType request) {
        processCallback.log("load requested");
        if (!BidMachineImpl.get().isInitialized()) {
            processRequestFail(BMError.NotInitialized);
            return (SelfType) this;
        }
        if (currentState != State.Idle) {
            processCallback.log("request process abort because it's already processing");
            return (SelfType) this;
        }
        if (request == null) {
            processRequestFail(BMError.paramError("No Request"));
            return (SelfType) this;
        }
        detachRequest(adRequest);
        adRequest = request;
        attachRequest(adRequest);
        processRequest(request);
        return (SelfType) this;
    }

    @Override
    public void destroy() {
        processCallback.processDestroy();
        BidMachineEvents.clear(trackingObject);
    }

    @Override
    public boolean isLoading() {
        return currentState == State.Requesting || currentState == State.Loading;
    }

    @Override
    public boolean isLoaded() {
        return loadedObject != null && currentState == State.Success;
    }

    @Override
    public boolean canShow() {
        return isLoaded() && !isShownTracked;
    }

    @Override
    public boolean isDestroyed() {
        return currentState == State.Destroyed;
    }

    @Override
    public boolean isExpired() {
        return currentState == State.Expired;
    }

    boolean prepareShow() {
        if (isDestroyed()) {
            processCallback.processShowFail(BMError.Destroyed);
            return false;
        } else if (isExpired()) {
            processCallback.processShowFail(BMError.Expired);
            return false;
        } else if (!isLoaded() || loadedObject == null) {
            processCallback.processShowFail(BMError.NotLoaded);
            return false;
        } else if (isShownTracked) {
            processCallback.processShowFail(BMError.AlreadyShown);
            return false;
        }
        return true;
    }

    /*
   Processing methods
    */

    private void processRequest(@NonNull final AdRequestType request) {
        processCallback.log("process request start");
        final AuctionResult auctionResult = getAuctionResult();
        if (auctionResult != null) {
            if (request.isExpired()) {
                processCallback.log("AuctionResult expired, please request new one");
                processRequestFail(BMError.RequestExpired);
            } else {
                processRequestSuccess(request,
                                      request.seatBidResult,
                                      request.bidResult,
                                      request.adResult);
            }
            return;
        }
        currentState = State.Requesting;
        request.request(getContext());
    }

    private void processRequestSuccess(@Nullable AdRequestType request,
                                       @Nullable Response.Seatbid seatbid,
                                       @Nullable Response.Seatbid.Bid bid,
                                       @Nullable Ad ad) {
        if (currentState.ordinal() > State.Loading.ordinal()) return;
        BidMachineEvents.eventStart(trackingObject, TrackEventType.Load, getType());
        currentState = State.Loading;
        if (request == null || seatbid == null || bid == null || ad == null) {
            processRequestFail(BMError.Internal);
        } else {
            BMError processResult = processResponseSuccess(request, seatbid, bid, ad);
            if (processResult != null) {
                processCallback.processLoadFail(processResult);
            }
        }
    }

    private void attachRequest(@Nullable AdRequestType request) {
        if (request != null) {
            request.addListener(adRequestListener);
            request.addInternalListener(internalAdRequestListener);
        }
    }

    private void detachRequest(@Nullable AdRequestType request) {
        if (request != null) {
            request.removeListener(adRequestListener);
            request.removeInternalListener(internalAdRequestListener);
        }
    }

    @Nullable
    private BMError processResponseSuccess(@NonNull AdRequestType adRequest,
                                           @NonNull Response.Seatbid seatbid,
                                           @NonNull Response.Seatbid.Bid bid,
                                           @NonNull Ad ad) {
        try {
            UnifiedAdRequestParamsType adRequestParams = adRequest.getUnifiedRequestParams();
            if (adRequestParams == null) {
                return BMError.Internal;
            }
            NetworkConfig networkConfig = getType().obtainNetworkConfig(ad);
            if (networkConfig != null) {
                AdObjectParams adObjectParams = getType().createAdObjectParams(
                        contextProvider, adRequestParams, seatbid, bid, ad);
                if (adObjectParams != null && adObjectParams.isValid()) {
                    loadedObject = createAdObject(
                            contextProvider,
                            adRequest,
                            networkConfig.obtainNetworkAdapter(),
                            adObjectParams,
                            processCallback);
                    if (loadedObject != null) {
                        loadedObject.load(contextProvider, adRequestParams);
                        return null;
                    }
                }
                return BMError.IncorrectAdUnit;
            }
            return BMError.adapterNotFoundError("for Ad with id: " + ad.getId());
        } catch (Throwable e) {
            Logger.log(e);
            return BMError.Internal;
        }
    }

    protected abstract AdObjectType createAdObject(@NonNull ContextProvider contextProvider,
                                                   @NonNull AdRequestType adRequest,
                                                   @NonNull NetworkAdapter adapter,
                                                   @NonNull AdObjectParams adObjectParams,
                                                   @NonNull AdProcessCallback processCallback);

    private void processRequestFail(BMError error) {
        if (currentState.ordinal() > State.Loading.ordinal()) return;
        BidMachineEvents.eventStart(trackingObject, TrackEventType.Load, getType());
        processCallback.processLoadFail(error);
    }

    private final AdRequest.AdRequestListener<AdRequestType> adRequestListener =
            new AdRequest.AdRequestListener<AdRequestType>() {
                @Override
                public void onRequestSuccess(@NonNull AdRequestType request,
                                             @NonNull AuctionResult auctionResult) {
                    if (request == adRequest) {
                        processRequestSuccess(request,
                                              request.seatBidResult,
                                              request.bidResult,
                                              request.adResult);
                    }
                }

                @Override
                public void onRequestFailed(@NonNull AdRequestType request,
                                            @NonNull BMError error) {
                    if (request == adRequest) {
                        processRequestFail(error);
                    }
                }

                @Override
                public void onRequestExpired(@NonNull AdRequestType request) {
                    if (request == adRequest) {
                        processCallback.processExpired();
                    }
                }
            };

    private final AdRequest.InternalAdRequestListener<AdRequestType> internalAdRequestListener =
            new AdRequest.InternalAdRequestListener<AdRequestType>() {
                @Override
                public void onRequestDestroyed(@NonNull AdRequest request) {
                    if (request == adRequest) {
                        destroy();
                    }
                }
            };

    @CallSuper
    protected void onDestroy() {
    }

    final AdProcessCallback processCallback = new AdProcessCallback() {

        @Override
        @SuppressWarnings("unchecked")
        public void processLoadSuccess() {
            if (currentState.ordinal() > State.Loading.ordinal()) {
                return;
            }
            log("processLoadSuccess");
            currentState = State.Success;
            trackEvent(TrackEventType.Load, null);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        log("notify AdLoaded");
                        listener.onAdLoaded((SelfType) BidMachineAd.this);
                    }
                }
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public void processLoadFail(final BMError error) {
            log("processLoadFail - " + error.getMessage());
            currentState = State.Failed;
            trackEvent(TrackEventType.Load, error);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        log("notify AdLoadFailed");
                        listener.onAdLoadFailed((SelfType) BidMachineAd.this, error);
                    }
                }
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public void processShown() {
            if (currentState.ordinal() > State.Success.ordinal()) {
                return;
            }
            if (isShownTracked) {
                return;
            }
            isShownTracked = true;
            if (adRequest != null) {
                adRequest.onShown();
            }
            if (loadedObject != null) {
                loadedObject.onShown();
            }
            log("processShown");
            SessionAdParams sessionAdParams = BidMachineImpl.get().getSessionAdParams(adsType);
            sessionAdParams.setLastBundle(null);
            sessionAdParams.setLastAdDomain(null);
            sessionAdParams.addImpression();
            if (adRequest != null) {
                AuctionResult auctionResult = adRequest.getAuctionResult();
                if (auctionResult != null
                        && auctionResult.getCreativeFormat() == CreativeFormat.Video) {
                    sessionAdParams.addVideoImpression();
                }
                Ad ad = adRequest.adResult;
                if (ad != null) {
                    for (String bundle : ad.getBundleList()) {
                        if (!TextUtils.isEmpty(bundle)) {
                            sessionAdParams.setLastBundle(bundle);
                            break;
                        }
                    }
                    for (String adomain : ad.getAdomainList()) {
                        if (!TextUtils.isEmpty(adomain)) {
                            sessionAdParams.setLastAdDomain(adomain);
                            break;
                        }
                    }
                }
            }
            trackEvent(TrackEventType.Show, null);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        log("notify AdShown");
                        listener.onAdShown((SelfType) BidMachineAd.this);
                    }
                }
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public void processShowFail(final BMError error) {
            if (loadedObject != null) {
                loadedObject.onShowFailed();
            }
            log("processShowFail");
            trackEvent(TrackEventType.Show, error);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener instanceof AdFullScreenListener) {
                        log("notify AdShowFailed");
                        ((AdFullScreenListener) listener).onAdShowFailed(BidMachineAd.this, error);
                    }
                }
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public void processClicked() {
            if (currentState.ordinal() > State.Success.ordinal()) {
                return;
            }
            if (!isClickTracked) {
                SessionAdParams sessionAdParams = BidMachineImpl.get().getSessionAdParams(adsType);
                sessionAdParams.addClick();
            }
            isClickTracked = true;
            if (loadedObject != null) {
                loadedObject.onClicked();
            }
            log("processClicked");
            trackEvent(TrackEventType.Click, null);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        log("notify AdClicked");
                        listener.onAdClicked((SelfType) BidMachineAd.this);
                    }
                }
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public void processImpression() {
            if (currentState.ordinal() > State.Success.ordinal()) {
                return;
            }
            if (isImpressionTracked) {
                return;
            }
            isImpressionTracked = true;
            if (loadedObject != null) {
                loadedObject.onImpression();
            }
            log("processImpression");
            trackEvent(TrackEventType.Impression, null);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        log("notify AdImpression");
                        listener.onAdImpression((SelfType) BidMachineAd.this);
                    }
                }
            });
        }

        @Override
        public void processFinished() {
            if (currentState.ordinal() > State.Success.ordinal()) {
                return;
            }
            if (isFinishTracked) {
                return;
            }
            isFinishTracked = true;
            if (loadedObject != null) {
                loadedObject.onFinished();
            }
            log("processFinished");
            if (adRequest != null) {
                AuctionResult auctionResult = adRequest.getAuctionResult();
                if (auctionResult != null
                        && auctionResult.getCreativeFormat() == CreativeFormat.Video) {
                    BidMachineImpl.get()
                            .getSessionAdParams(adsType)
                            .addCompletedVideo();
                }
            }
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener instanceof AdRewardedListener) {
                        log("notify AdRewarded");
                        ((AdRewardedListener) listener).onAdRewarded(BidMachineAd.this);
                    }
                }
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public void processClosed() {
            if (currentState.ordinal() > State.Success.ordinal()) {
                return;
            }
            if (isCloseTracked) {
                return;
            }
            isCloseTracked = true;
            if (loadedObject != null) {
                loadedObject.onClosed(isFinishTracked);
            }
            log("processClosed (" + isFinishTracked + ")");
            trackEvent(TrackEventType.Close, null);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener instanceof AdFullScreenListener) {
                        log("notify AdClosed");
                        ((AdFullScreenListener) listener).onAdClosed(BidMachineAd.this, isFinishTracked);
                    }
                }
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public void processExpired() {
            if (currentState.ordinal() > State.Success.ordinal()) {
                return;
            }
            if (adRequest != null) {
                adRequest.onExpired();
            }
            if (loadedObject != null) {
                loadedObject.onExpired();
            }
            log("processExpired");
            currentState = State.Expired;
            trackEvent(TrackEventType.Expired, null);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        log("notify AdExpired");
                        listener.onAdExpired((SelfType) BidMachineAd.this);
                    }
                }
            });
        }

        @Override
        public void processDestroy() {
            log("destroy requested");
            if (currentState == State.Loading) {
                trackEvent(TrackEventType.Load, BMError.Destroyed);
            }
            trackEvent(TrackEventType.Destroy, null);
            currentState = State.Destroyed;
            if (adRequest != null) {
                adRequest.cancel();
                detachRequest(adRequest);
            }
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (loadedObject != null) {
                        loadedObject.onDestroy();
                    }
                    onDestroy();
                }
            });
        }

        @Override
        public void log(String message) {
            if (Logger.isLoggingEnabled()) {
                String selfMessage = toStringShort();
                AuctionResult auctionResult = getAuctionResult();
                if (auctionResult != null) {
                    selfMessage += "(demand: " + auctionResult.getDemandSource() + ")";
                }
                Logger.log(String.format("%s: %s", selfMessage, message));
            }
        }

    };

    private void trackEvent(TrackEventType eventType, @Nullable BMError error) {
        BidMachineEvents.eventFinish(trackingObject, eventType, getType(), error);
    }

    @NonNull
    @Override
    public String toString() {
        return toStringShort() + ": state=" + currentState + ", auctionResult=" + getAuctionResult();
    }

    /*
    Logger helpers
     */

    private String cachedClassTag;

    protected String toStringShort() {
        if (cachedClassTag == null) {
            cachedClassTag = getClass().getSimpleName() + "[@" + Integer.toHexString(
                    hashCode()) + "]";
        }
        return cachedClassTag;
    }

    /*
    Inner classes
     */

    enum State {
        Idle, Requesting, Loading, Success, Failed, Destroyed, Expired
    }

}
