package io.bidmachine;

import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.explorestack.protobuf.Any;
import com.explorestack.protobuf.InvalidProtocolBufferException;
import com.explorestack.protobuf.Message;
import com.explorestack.protobuf.Struct;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.adcom.Context;
import com.explorestack.protobuf.adcom.Placement;
import com.explorestack.protobuf.openrtb.Openrtb;
import com.explorestack.protobuf.openrtb.Request;
import com.explorestack.protobuf.openrtb.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.bidmachine.core.Logger;
import io.bidmachine.core.NetworkRequest;
import io.bidmachine.core.Utils;
import io.bidmachine.displays.PlacementBuilder;
import io.bidmachine.measurer.OMSDKSettings;
import io.bidmachine.models.AuctionResult;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.RequestBuilder;
import io.bidmachine.models.RequestParams;
import io.bidmachine.protobuf.RequestExtension;
import io.bidmachine.protobuf.ResponsePayload;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.utils.BMError;

public abstract class AdRequest<SelfType extends AdRequest, UnifiedAdRequestParamsType extends UnifiedAdRequestParams> {

    private static final long DEF_EXPIRATION_TIME = TimeUnit.MINUTES.toSeconds(29);

    private final String trackingId = UUID.randomUUID().toString();

    @NonNull
    private final AdsType adsType;

    PriceFloorParams priceFloorParams;
    TargetingParams targetingParams;
    SessionAdParams sessionAdParams;
    Map<String, NetworkConfig> networkConfigMap;
    int timeOut = -1;
    String bidPayload;
    String placementId;

    @VisibleForTesting
    UserRestrictionParams userRestrictionParams;
    private ExtraParams extraParams;

    @Nullable
    Ad adResult;
    @Nullable
    Response.Seatbid seatBidResult;
    @Nullable
    Response.Seatbid.Bid bidResult;

    @Nullable
    @VisibleForTesting
    AuctionResult auctionResult;
    @Nullable
    private ApiRequest<Request, Response> currentApiRequest;
    @Nullable
    @VisibleForTesting
    List<AdRequestListener<SelfType>> adRequestListeners;
    @Nullable
    @VisibleForTesting
    List<InternalAdRequestListener<SelfType>> internalAdRequestListeners;
    @Nullable
    private UnifiedAdRequestParamsType unifiedAdRequestParams;
    @Nullable
    @VisibleForTesting
    Map<TrackEventType, List<String>> trackUrls;

    private long expirationTime = -1;

    private final AtomicBoolean isApiRequestCanceled = new AtomicBoolean(false);
    private final AtomicBoolean isApiRequestCompleted = new AtomicBoolean(false);
    private boolean isAdWasShown;
    private boolean isExpired;
    private boolean isExpireTrackerSubscribed;
    private boolean isDestroyed;

    private final Runnable expiredRunnable = new ExpiredRunnable(this);

    private final Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isCompleted()) {
                processRequestFail(BMError.TimeoutError);
                cancel();
            }
        }
    };

    private final TrackingObject trackingObject = new SimpleTrackingObject(trackingId) {
        @Nullable
        @Override
        List<String> getTrackingUrls(@NonNull TrackEventType eventType) {
            return trackUrls != null
                    ? trackUrls.get(eventType)
                    : super.getTrackingUrls(eventType);
        }
    };

    protected AdRequest(@NonNull AdsType adsType) {
        this.adsType = adsType;
    }

    @VisibleForTesting
    @Nullable
    Object build(final android.content.Context context, AdsType adsType) {
        try {
            final String sellerId = BidMachineImpl.get().getSellerId();
            if (TextUtils.isEmpty(sellerId)) {
                return BMError.paramError("Seller Id not provided");
            }
            assert sellerId != null;

            final BidMachineImpl bidMachine = BidMachineImpl.get();
            final SessionManager sessionManager = SessionManager.get();

            AdvertisingPersonalData.updateInfo(context);

            final TargetingParams targetingParams =
                    RequestParams.resolveParams(this.targetingParams,
                                                bidMachine.getTargetingParams());
            final UserRestrictionParams userRestrictionParams =
                    RequestParams.resolveParams(this.userRestrictionParams,
                                                bidMachine.getUserRestrictionParams());
            unifiedAdRequestParams = createUnifiedAdRequestParams(targetingParams,
                                                                  userRestrictionParams);

            SessionAdParams bidMachineSessionAdParams = sessionManager.getSessionAdParams(adsType)
                    .setSessionDuration(sessionManager.getSessionDuration());
            final SessionAdParams sessionAdParams =
                    RequestParams.resolveParams(this.sessionAdParams,
                                                bidMachineSessionAdParams);

            // PriceFloor params
            final PriceFloorParams priceFloorParams = Utils.oneOf(this.priceFloorParams,
                                                                  bidMachine.getPriceFloorParams());
            final Map<String, Double> priceFloorsMap =
                    priceFloorParams.getPriceFloors() == null
                            || priceFloorParams.getPriceFloors().size() == 0
                            ? bidMachine.getPriceFloorParams().getPriceFloors()
                            : priceFloorParams.getPriceFloors();

            if (priceFloorsMap == null) {
                return BMError.paramError("PriceFloors not provided");
            }

            final ArrayList<Message.Builder> placements = new ArrayList<>();
            adsType.collectDisplayPlacements(new SimpleContextProvider(context),
                                             this,
                                             unifiedAdRequestParams,
                                             placements,
                                             networkConfigMap);

            final Request.Builder requestBuilder = Request.newBuilder();

            final Request.Item.Builder itemBuilder = Request.Item.newBuilder();
            itemBuilder.setId(UUID.randomUUID().toString());
            itemBuilder.setQty(1);

            for (Map.Entry<String, Double> bid : priceFloorsMap.entrySet()) {
                final Request.Item.Deal.Builder dealBuilder = Request.Item.Deal.newBuilder();
                dealBuilder.setId(bid.getKey());
                dealBuilder.setFlr(bid.getValue());
                dealBuilder.setFlrcur("USD");
                itemBuilder.addDeal(dealBuilder);
            }

            // Request -> Item -> Spec -> Placement
            final Placement.Builder placementBuilder = Placement.newBuilder();
            placementBuilder.setSsai(0);
            placementBuilder.setSdk(BidMachine.NAME);
            placementBuilder.setSdkver(BidMachine.VERSION);
            placementBuilder.setSecure(!Utils.canUseCleartextTraffic());
            if (!TextUtils.isEmpty(placementId)) {
                placementBuilder.setTagid(placementId);
            }
            for (Message.Builder displayBuilder : placements) {
                if (displayBuilder instanceof Placement.DisplayPlacement.Builder) {
                    placementBuilder.setDisplay((Placement.DisplayPlacement.Builder) displayBuilder);
                } else if (displayBuilder instanceof Placement.VideoPlacement.Builder) {
                    placementBuilder.setVideo((Placement.VideoPlacement.Builder) displayBuilder);
                } else {
                    throw new IllegalArgumentException("Unsupported display type: "
                                                               + displayBuilder);
                }
            }

            // Request -> Item -> Spec -> Placement -> Extension
            Struct.Builder placementExtBuilder = Struct.newBuilder();
            OMSDKSettings.fillExtension(placementExtBuilder);
            if (placementExtBuilder.getFieldsCount() > 0) {
                placementBuilder.setExt(placementExtBuilder);
            }

            onBuildPlacement(placementBuilder);
            itemBuilder.setSpec(Any.pack(placementBuilder.build()));

            requestBuilder.addItem(itemBuilder.build());

            // Context
            final Context.Builder contextBuilder = Context.newBuilder();

            // Context -> App
            final Context.App.Builder appBuilder = Context.App.newBuilder();
            Publisher publisher = bidMachine.getPublisher();
            if (publisher != null) {
                publisher.build(appBuilder);
            }
            targetingParams.build(context, appBuilder);

            // Context -> App -> Extension
            Struct.Builder appExtBuilder = Struct.newBuilder();
            targetingParams.fillAppExtension(appExtBuilder);
            if (appExtBuilder.getFieldsCount() > 0) {
                appBuilder.setExt(appExtBuilder.build());
            }

            contextBuilder.setApp(appBuilder);

            // Context -> Restrictions
            final BlockedParams blockedParams = targetingParams.getBlockedParams();
            if (blockedParams != null) {
                final Context.Restrictions.Builder restrictionsBuilder = Context.Restrictions.newBuilder();
                blockedParams.build(restrictionsBuilder);
                contextBuilder.setRestrictions(restrictionsBuilder);
            }

            // Context -> User
            final Context.User.Builder userBuilder = Context.User.newBuilder();
            userRestrictionParams.build(userBuilder);
            if (userRestrictionParams.canSendUserInfo()) {
                targetingParams.build(userBuilder);
            }

            // Context -> User -> Extension
            Struct.Builder userExtBuilder = Struct.newBuilder();
            sessionAdParams.fillUserExtension(userExtBuilder);
            if (userExtBuilder.getFieldsCount() > 0) {
                userBuilder.setExt(userExtBuilder.build());
            }
            bidMachineSessionAdParams.setIsUserClickedOnLastAd(false);

            contextBuilder.setUser(userBuilder);

            // Context -> Regs
            final Context.Regs.Builder regsBuilder = Context.Regs.newBuilder();
            userRestrictionParams.build(regsBuilder);
            contextBuilder.setRegs(regsBuilder);

            // Context -> Device
            final Context.Device.Builder deviceBuilder = Context.Device.newBuilder();
            bidMachine.getDeviceParams().build(context,
                                               deviceBuilder,
                                               targetingParams,
                                               bidMachine.getTargetingParams(),
                                               userRestrictionParams);

            // Context -> Device -> Extension
            Struct.Builder deviceExtBuilder = Struct.newBuilder();
            bidMachine.getDeviceParams().fillDeviceExtension(context,
                                                             deviceExtBuilder,
                                                             userRestrictionParams);
            if (deviceExtBuilder.getFieldsCount() > 0) {
                deviceBuilder.setExt(deviceExtBuilder.build());
            }

            contextBuilder.setDevice(deviceBuilder);

            requestBuilder.setContext(Any.pack(contextBuilder.build()));

            requestBuilder.setTest(bidMachine.isTestMode());
            requestBuilder.addCur("USD");
            requestBuilder.setAt(2);
            requestBuilder.setTmax(10000);

            // Request
            final RequestExtension.Builder requestExtensionBuilder = RequestExtension.newBuilder();
            requestExtensionBuilder.setSellerId(sellerId);
            requestExtensionBuilder.setBmIfv(bidMachine.obtainIFV(context));
            requestExtensionBuilder.setSessionId(sessionManager.getSessionId());

            requestBuilder.addExtProto(Any.pack(requestExtensionBuilder.build()));

            return requestBuilder.build();
        } catch (Throwable t) {
            Logger.log(t);
            return null;
        }
    }

    protected void onBuildPlacement(Placement.Builder builder) {
    }

    protected BMError verifyRequest() {
        return null;
    }

    @NonNull
    protected final AdsType getType() {
        return adsType;
    }

    boolean isValid() {
        return !TextUtils.isEmpty(BidMachineImpl.get().getSellerId());
    }

    boolean isPlacementBuilderMatch(PlacementBuilder placementBuilder) {
        return true;
    }

    @Nullable
    @SuppressWarnings("WeakerAccess")
    public AuctionResult getAuctionResult() {
        return auctionResult;
    }

    public void request(@NonNull final android.content.Context context) {
        if (!BidMachineImpl.get().isInitialized()) {
            processRequestFail(BMError.NotInitialized);
            return;
        }
        if (isDestroyed) {
            processRequestFail(BMError.RequestDestroyed);
            return;
        }
        BMError verifyError = verifyRequest();
        if (verifyError != null) {
            processRequestFail(verifyError);
            return;
        }
        AdRequestExecutor.get().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    cancel();
                    isAdWasShown = false;
                    unsubscribeExpireTracker();
                    isApiRequestCanceled.set(false);
                    isApiRequestCompleted.set(false);

                    Logger.log(toString() + ": api request start");
                    BidMachineEvents.eventStart(trackingObject, TrackEventType.AuctionRequest);
                    if (!TextUtils.isEmpty(bidPayload)) {
                        processBidPayload(bidPayload);
                        return;
                    }
                    processRequestObject(context);
                    if (timeOut > 0) {
                        Utils.onBackgroundThread(timeOutRunnable, timeOut);
                    }
                } catch (Throwable t) {
                    Logger.log(t);
                    processRequestFail(BMError.Internal);
                }
            }
        });
    }

    private void processBidPayload(@NonNull String bidPayload) {
        try {
            byte[] bytes = Base64.decode(bidPayload, Base64.DEFAULT);
            ResponsePayload responsePayload = ResponsePayload.parseFrom(bytes);
            if (responsePayload != null) {
                processBidPayload(responsePayload);
                return;
            }
        } catch (Throwable t) {
            Logger.log(t);
        }
        processRequestFail(BMError.IncorrectContent);
    }

    @VisibleForTesting
    void processBidPayload(@NonNull ResponsePayload responsePayload) {
        if (isBidPayloadValid(responsePayload)) {
            Openrtb openrtb = responsePayload.getResponseCache();
            if (openrtb != null && openrtb != Openrtb.getDefaultInstance()) {
                processApiRequestSuccess(openrtb.getResponse());
                return;
            }
            String url = responsePayload.getResponseCacheUrl();
            if (!TextUtils.isEmpty(url) && Utils.isHttpUrl(url)) {
                retrieveBody(url);
                return;
            }
        }
        processRequestFail(BMError.IncorrectContent);
    }

    @VisibleForTesting
    boolean isBidPayloadValid(@NonNull ResponsePayload responsePayload) {
        Placement placement = responsePayload.getRequestItemSpec();
        try {
            return placement == Placement.getDefaultInstance() || isPlacementObjectValid(placement);
        } catch (Throwable t) {
            Logger.log(t);
        }
        return false;
    }

    @VisibleForTesting
    void retrieveBody(@NonNull String url) {
        ApiRequest.Builder<Request, Response> requestBuilder = new ApiRequest.Builder<Request, Response>()
                .url(url)
                .setLoadingTimeOut(timeOut)
                .setDataBinder(new ApiRequest.ApiResponseAuctionDataBinder())
                .setCallback(new NetworkRequest.Callback<Response, BMError>() {
                    @Override
                    public void onSuccess(@Nullable Response result) {
                        processApiRequestSuccess(result);
                    }

                    @Override
                    public void onFail(@Nullable BMError result) {
                        processApiRequestFail(result);
                    }
                })
                .setCancelCallback(new NetworkRequest.CancelCallback() {
                    @Override
                    public void onCanceled() {
                        processApiRequestCancel();
                    }
                });
        processRequestBuilder(requestBuilder);
    }

    private void processRequestObject(android.content.Context context) {
        Object requestObject = build(context, getType());
        if (requestObject instanceof Request) {
            ApiRequest.Builder<Request, Response> requestBuilder = new ApiRequest.Builder<Request, Response>()
                    .url(BidMachineImpl.get().getAuctionUrl())
                    .setRequestData((Request) requestObject)
                    .setLoadingTimeOut(timeOut)
                    .setDataBinder(getType().getBinder())
                    .setCallback(new NetworkRequest.Callback<Response, BMError>() {
                        @Override
                        public void onSuccess(@Nullable Response result) {
                            processApiRequestSuccess(result);
                        }

                        @Override
                        public void onFail(@Nullable BMError result) {
                            processApiRequestFail(result);
                        }
                    })
                    .setCancelCallback(new NetworkRequest.CancelCallback() {
                        @Override
                        public void onCanceled() {
                            processApiRequestCancel();
                        }
                    });
            processRequestBuilder(requestBuilder);
        } else {
            processRequestFail(requestObject instanceof BMError
                                       ? (BMError) requestObject
                                       : BMError.Internal);
        }
    }

    private void processRequestBuilder(@NonNull ApiRequest.Builder<Request, Response> requestBuilder) {
        if (isCanceled()) {
            return;
        }
        currentApiRequest = requestBuilder.request();
    }

    public void notifyMediationWin() {
        if (!isCompleted()) {
            return;
        }

        Logger.log(toString() + ": notifyMediationWin");

        BMError bmError;
        if (isDestroyed) {
            bmError = BMError.RequestDestroyed;
        } else if (isExpired) {
            bmError = BMError.RequestExpired;
        } else {
            bmError = null;
        }
        BidMachineEvents.eventFinish(trackingObject,
                                     TrackEventType.MediationWin,
                                     getType(),
                                     bmError);
    }

    public void notifyMediationLoss() {
        if (!isCompleted()) {
            return;
        }

        Logger.log(toString() + ": notifyMediationLoss");

        BMError bmError;
        if (isDestroyed) {
            bmError = BMError.RequestDestroyed;
        } else if (isExpired) {
            bmError = BMError.RequestExpired;
        } else {
            bmError = null;
        }
        BidMachineEvents.eventFinish(trackingObject,
                                     TrackEventType.MediationLoss,
                                     getType(),
                                     bmError);
    }

    boolean isCompleted() {
        return isApiRequestCompleted.get();
    }

    boolean isCanceled() {
        return isApiRequestCanceled.get();
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    public void destroy() {
        if (isDestroyed) {
            return;
        }
        isDestroyed = true;

        Logger.log(toString() + ": destroy");

        cancel();
        unsubscribeExpireTracker();
        BidMachineEvents.clear(trackingObject);
        BidMachineFetcher.release(this);
        notifyRequestDestroyed();

        priceFloorParams = null;
        targetingParams = null;
        sessionAdParams = null;
        networkConfigMap = null;
        bidPayload = null;
        userRestrictionParams = null;
        extraParams = null;

        adResult = null;
        seatBidResult = null;
        bidResult = null;
        auctionResult = null;

        unifiedAdRequestParams = null;

        BidMachineEvents.eventFinish(trackingObject,
                                     TrackEventType.AuctionRequestDestroy,
                                     getType(),
                                     null);
    }

    void cancel() {
        if (isCompleted() || isCanceled()) {
            return;
        }
        Utils.onBackgroundThread(new Runnable() {
            @Override
            public void run() {
                if (currentApiRequest != null) {
                    try {
                        currentApiRequest.cancel();
                    } catch (Throwable t) {
                        Logger.log(t);
                    }
                    currentApiRequest = null;
                } else {
                    processApiRequestCancel();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    @VisibleForTesting
    void processExpired() {
        isExpired = true;
        unsubscribeExpireTracker();
        if (adRequestListeners != null) {
            for (AdRequestListener<SelfType> listener : adRequestListeners) {
                listener.onRequestExpired((SelfType) this);
            }
        }
        for (AdRequestListener adRequestListener : BidMachineImpl.get().getAdRequestListeners()) {
            adRequestListener.onRequestExpired(this);
        }
        BidMachineEvents.eventFinish(trackingObject,
                                     TrackEventType.AuctionRequestExpired,
                                     getType(),
                                     null);
    }

    /**
     * @return true if Ads is expired
     */
    @SuppressWarnings("WeakerAccess")
    public boolean isExpired() {
        return isExpired;
    }

    @SuppressWarnings("WeakerAccess")
    public void addListener(@Nullable AdRequestListener<SelfType> listener) {
        if (listener != null) {
            if (adRequestListeners == null) {
                adRequestListeners = new CopyOnWriteArrayList<>();
            }
            adRequestListeners.add(listener);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public void removeListener(@Nullable AdRequestListener<SelfType> listener) {
        if (adRequestListeners != null && listener != null) {
            adRequestListeners.remove(listener);
        }
    }

    void addInternalListener(@Nullable InternalAdRequestListener<SelfType> listener) {
        if (listener != null) {
            if (internalAdRequestListeners == null) {
                internalAdRequestListeners = new CopyOnWriteArrayList<>();
            }
            internalAdRequestListeners.add(listener);
        }
    }

    void removeInternalListener(@Nullable InternalAdRequestListener<SelfType> listener) {
        if (internalAdRequestListeners != null && listener != null) {
            internalAdRequestListeners.remove(listener);
        }
    }

    @SuppressWarnings("unchecked")
    void notifyRequestDestroyed() {
        if (internalAdRequestListeners != null) {
            for (InternalAdRequestListener<SelfType> listener : internalAdRequestListeners) {
                listener.onRequestDestroyed((SelfType) this);
            }
        }
    }

    void onShown() {
        isAdWasShown = true;
        unsubscribeExpireTracker();
    }

    protected boolean isAdWasShown() {
        return isAdWasShown;
    }

    void onExpired() {
        unsubscribeExpireTracker();
    }

    @VisibleForTesting
    void extractTrackUrls(@Nullable Response.Seatbid.Bid bid) {
        if (bid == null) {
            return;
        }
        trackUrls = new EnumMap<>(TrackEventType.class);
        OrtbUtils.addEvent(trackUrls, TrackEventType.MediationWin, bid.getPurl());
        OrtbUtils.addEvent(trackUrls, TrackEventType.MediationLoss, bid.getLurl());
    }

    private void subscribeExpireTracker() {
        final long expTime = expirationTime * 1000;
        if (expTime > 0 && !isExpireTrackerSubscribed) {
            isExpireTrackerSubscribed = true;
            Utils.onBackgroundThread(expiredRunnable, expTime);
        }
    }

    private void unsubscribeExpireTracker() {
        isExpireTrackerSubscribed = false;
        Utils.cancelBackgroundThreadTask(expiredRunnable);
    }

    @SuppressWarnings("unchecked")
    @VisibleForTesting
    void processApiRequestSuccess(@Nullable Response response) {
        if (isCanceled()) {
            return;
        }
        isApiRequestCompleted.set(true);
        currentApiRequest = null;
        Utils.cancelBackgroundThreadTask(timeOutRunnable);
        Logger.log(toString() + ": api request success");

        if (response != null && response.getSeatbidCount() > 0) {
            final Response.Seatbid seatbid = response.getSeatbid(0);
            if (seatbid == null || seatbid.getBidCount() == 0) {
                Logger.log(toString() + ": Seatbid not found or not valid");
                processRequestFail(BMError.requestError("Seatbid not found or not valid"));
                return;
            }
            final Response.Seatbid.Bid bid = seatbid.getBid(0);
            if (bid == null) {
                Logger.log(toString() + ": Bid not found or not valid");
                processRequestFail(BMError.requestError("Bid not found or not valid"));
                return;
            }
            Any media = bid.getMedia();
            if (media == null || !media.is(Ad.class)) {
                Logger.log(toString() + ": Media not found or not valid");
                processRequestFail(BMError.requestError("Media not found or not valid"));
                return;
            }
            try {
                Ad ad = bid.getMedia().unpack(Ad.class);
                if (ad != null) {
                    NetworkConfig networkConfig = getType().obtainNetworkConfig(ad);
                    if (networkConfig == null) {
                        Logger.log(toString() + ": NetworkConfig not found");
                        processRequestFail(BMError.requestError("NetworkConfig not found"));
                        return;
                    }
                    adResult = ad;
                    bidResult = bid;
                    seatBidResult = seatbid;
                    auctionResult = new AuctionResultImpl(getType(),
                                                          seatbid,
                                                          bid,
                                                          ad,
                                                          networkConfig);
                    expirationTime = Utils.getOrDefault(bid.getExp(),
                                                        Response.Seatbid.Bid.getDefaultInstance()
                                                                .getExp(),
                                                        DEF_EXPIRATION_TIME);
                    extractTrackUrls(bid);
                    subscribeExpireTracker();
                    Logger.log(toString() + ": Request finished (" + auctionResult + ")");
                    if (adRequestListeners != null) {
                        for (AdRequestListener listener : adRequestListeners) {
                            listener.onRequestSuccess(this, auctionResult);
                        }
                    }
                    for (AdRequestListener listener : BidMachineImpl.get()
                            .getAdRequestListeners()) {
                        listener.onRequestSuccess(this, auctionResult);
                    }
                    BidMachineEvents.eventFinish(trackingObject,
                                                 TrackEventType.AuctionRequest,
                                                 getType(),
                                                 null);
                    return;
                } else {
                    Logger.log(toString() + ": Ad not found or not valid");
                }
            } catch (Throwable t) {
                Logger.log(t);
            }
        } else {
            Logger.log(toString() + ": Response not found or not valid");
        }
        processRequestFail(BMError.Internal);
    }

    private void processApiRequestFail(@Nullable BMError error) {
        if (isCanceled()) {
            return;
        }
        isApiRequestCompleted.set(true);
        currentApiRequest = null;
        Utils.cancelBackgroundThreadTask(timeOutRunnable);
        Logger.log(toString() + ": api request fail - " + error);

        if (error == null) {
            error = BMError.noFillError(null);
            error.setTrackError(false);
        } else {
            error.setTrackError(error != BMError.NoContent);
        }
        processRequestFail(error);
    }

    @SuppressWarnings("unchecked")
    @VisibleForTesting
    void processRequestFail(@NonNull BMError error) {
        if (adRequestListeners != null) {
            for (AdRequestListener listener : adRequestListeners) {
                listener.onRequestFailed(this, error);
            }
        }
        for (AdRequestListener adRequestListener : BidMachineImpl.get().getAdRequestListeners()) {
            adRequestListener.onRequestFailed(this, error);
        }
        BidMachineEvents.eventFinish(trackingObject,
                                     TrackEventType.AuctionRequest,
                                     getType(),
                                     error);
    }

    private void processApiRequestCancel() {
        isApiRequestCanceled.set(true);
        Utils.cancelBackgroundThreadTask(timeOutRunnable);

        BidMachineEvents.eventFinish(trackingObject,
                                     TrackEventType.AuctionRequestCancel,
                                     getType(),
                                     null);
        BidMachineEvents.clearEvent(trackingObject, TrackEventType.AuctionRequest);
    }

    protected abstract boolean isPlacementObjectValid(@NonNull Placement placement) throws Throwable;

    @NonNull
    protected abstract UnifiedAdRequestParamsType createUnifiedAdRequestParams(@NonNull TargetingParams targetingParams,
                                                                               @NonNull DataRestrictions dataRestrictions);

    @NonNull
    final UnifiedAdRequestParamsType obtainUnifiedRequestParams() {
        if (unifiedAdRequestParams == null) {
            BidMachineImpl bidMachine = BidMachineImpl.get();
            TargetingParams targetingParams = RequestParams.resolveParams(this.targetingParams,
                                                                          bidMachine.getTargetingParams());
            UserRestrictionParams userRestrictionParams = RequestParams.resolveParams(this.userRestrictionParams,
                                                                                      bidMachine.getUserRestrictionParams());
            unifiedAdRequestParams = createUnifiedAdRequestParams(targetingParams,
                                                                  userRestrictionParams);
        }
        return unifiedAdRequestParams;
    }

    @NonNull
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[@" + Integer.toHexString(hashCode()) + "]";
    }


    public interface AdRequestListener<AdRequestType extends AdRequest> {

        /**
         * Called when AdRequest was obtained successfully
         *
         * @param request       - AdRequest instance
         * @param auctionResult - AuctionResult info
         */
        void onRequestSuccess(@NonNull AdRequestType request, @NonNull AuctionResult auctionResult);

        /**
         * Called when AdRequest failed to load
         *
         * @param request - AdRequest instance
         * @param error   - BMError with additional info about error
         */
        void onRequestFailed(@NonNull AdRequestType request, @NonNull BMError error);

        /**
         * Called when AdRequest expired
         *
         * @param request - AdRequest instance
         */
        void onRequestExpired(@NonNull AdRequestType request);

    }

    interface InternalAdRequestListener<AdRequestType extends AdRequest> {

        void onRequestDestroyed(@NonNull AdRequestType request);

    }

    private static class ExpiredRunnable implements Runnable {

        private final WeakReference<AdRequest> weakAdRequest;

        public ExpiredRunnable(@NonNull AdRequest adRequest) {
            this.weakAdRequest = new WeakReference<>(adRequest);
        }

        @Override
        public void run() {
            AdRequest adRequest = weakAdRequest.get();
            if (adRequest != null) {
                adRequest.processExpired();
            }
        }

    }

    protected static abstract class AdRequestBuilderImpl<
            SelfType extends RequestBuilder,
            ReturnType extends AdRequest>
            implements RequestBuilder<SelfType, ReturnType> {

        protected ReturnType params;

        protected AdRequestBuilderImpl() {
        }

        @Override
        @SuppressWarnings("unchecked")
        public SelfType setPriceFloorParams(PriceFloorParams priceFloorParams) {
            prepareRequest();
            params.priceFloorParams = priceFloorParams;
            return (SelfType) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SelfType setTargetingParams(TargetingParams userParams) {
            prepareRequest();
            params.targetingParams = userParams;
            return (SelfType) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SelfType setSessionAdParams(SessionAdParams sessionAdParams) {
            prepareRequest();
            params.sessionAdParams = sessionAdParams;
            return (SelfType) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SelfType setNetworks(@Nullable List<NetworkConfig> networkConfigList) {
            fillNetworkConfigs(networkConfigList);
            return (SelfType) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SelfType setNetworks(@Nullable String jsonData) {
            if (!TextUtils.isEmpty(jsonData)) {
                List<NetworkConfig> networkConfigList = new ArrayList<>();
                try {
                    JSONArray networkConfigJsonArray = new JSONArray(jsonData);
                    for (int i = 0; i < networkConfigJsonArray.length(); i++) {
                        JSONObject networkConfigJsonObject = networkConfigJsonArray.getJSONObject(i);
                        NetworkConfig networkConfig = NetworkConfigFactory.create(
                                BidMachineImpl.get().getAppContext(),
                                networkConfigJsonObject);
                        if (networkConfig != null) {
                            networkConfigList.add(networkConfig);
                        }
                    }
                } catch (Exception e) {
                    Logger.log(e);
                }
                fillNetworkConfigs(networkConfigList);
            }
            return (SelfType) this;
        }

        @SuppressWarnings("unchecked")
        @VisibleForTesting
        void fillNetworkConfigs(@Nullable List<NetworkConfig> networkConfigList) {
            if (networkConfigList != null && networkConfigList.size() > 0) {
                prepareRequest();

                params.networkConfigMap = new HashMap<String, NetworkConfig>();
                for (NetworkConfig networkConfig
                        : BidMachineImpl.get().getInitNetworkConfigList()) {
                    params.networkConfigMap.put(networkConfig.getKey(), networkConfig);
                }
                for (NetworkConfig networkConfig : networkConfigList) {
                    String networkKey = networkConfig.getKey();
                    if (NetworkRegistry.isNetworkInitialized(networkKey,
                                                             RegisterSource.Publisher)) {
                        params.networkConfigMap.put(networkConfig.getKey(), networkConfig);
                    } else {
                        Logger.log(String.format(
                                "%s: %s was removed from AdRequest. Please register network before initialize BidMachine",
                                toString(),
                                networkKey));
                    }
                }
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public SelfType setLoadingTimeOut(int timeOutMs) {
            prepareRequest();
            params.timeOut = timeOutMs;
            return (SelfType) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SelfType setBidPayload(@Nullable String bidPayload) {
            prepareRequest();
            params.bidPayload = bidPayload;
            return (SelfType) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SelfType setPlacementId(@Nullable String placementId) {
            prepareRequest();
            params.placementId = placementId;
            return (SelfType) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SelfType setListener(AdRequestListener<ReturnType> listener) {
            prepareRequest();
            params.addListener(listener);
            return (SelfType) this;
        }

//        @Override
//        @SuppressWarnings("unchecked")
//        public SelfType setExtraParams(ExtraParams extraParams) {
//            prepareRequest();
//            params.extraParams = extraParams;
//            return (SelfType) this;
//        }

        @Override
        public ReturnType build() {
            try {
                prepareRequest();
                return params;
            } finally {
                params = null;
            }
        }

        protected void prepareRequest() {
            if (params == null) {
                params = createRequest();
            }
        }

        protected abstract ReturnType createRequest();

    }

    protected class BaseUnifiedAdRequestParams extends UnifiedAdRequestParamsImpl {

        public BaseUnifiedAdRequestParams(@NonNull TargetingParams targetingParams,
                                          @NonNull DataRestrictions dataRestrictions) {
            super(targetingParams, dataRestrictions);
        }

        @Nullable
        @Override
        public AdRequest getAdRequest() {
            return AdRequest.this;
        }

    }

}