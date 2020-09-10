package io.bidmachine;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.explorestack.protobuf.Any;
import com.explorestack.protobuf.InvalidProtocolBufferException;
import com.explorestack.protobuf.Message;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.adcom.Context;
import com.explorestack.protobuf.adcom.Placement;
import com.explorestack.protobuf.openrtb.Request;
import com.explorestack.protobuf.openrtb.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.bidmachine.core.Logger;
import io.bidmachine.core.NetworkRequest;
import io.bidmachine.displays.PlacementBuilder;
import io.bidmachine.models.AuctionResult;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.RequestBuilder;
import io.bidmachine.models.RequestParams;
import io.bidmachine.protobuf.HeaderBiddingType;
import io.bidmachine.protobuf.RequestExtension;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.utils.BMError;

import static io.bidmachine.Utils.getOrDefault;
import static io.bidmachine.core.Utils.oneOf;

public abstract class AdRequest<SelfType extends AdRequest, UnifiedAdRequestParamsType extends UnifiedAdRequestParams> {

    private static final long DEF_EXPIRATION_TIME = TimeUnit.MINUTES.toSeconds(29);

    private final String trackingId = UUID.randomUUID().toString();

    @NonNull
    private final AdsType adsType;

    PriceFloorParams priceFloorParams;
    TargetingParams targetingParams;
    Map<String, NetworkConfig> networkConfigMap;
    int timeOut = -1;
    boolean headerBiddingEnabled = true;

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
    private ArrayList<AdRequestListener<SelfType>> adRequestListeners;
    @Nullable
    private UnifiedAdRequestParamsType unifiedAdRequestParams;
    @Nullable
    @VisibleForTesting
    Map<TrackEventType, List<String>> trackUrls;

    private long expirationTime = -1;

    private AtomicBoolean isApiRequestCanceled = new AtomicBoolean(false);
    private AtomicBoolean isApiRequestCompleted = new AtomicBoolean(false);
    private boolean isExpired;
    private boolean isExpireTrackerSubscribed;

    private final Runnable expiredRunnable = new Runnable() {
        @Override
        public void run() {
            processExpired();
        }
    };

    private final Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isApiRequestCompleted.get()) {
                cancel();
                processRequestFail(BMError.TimeoutError);
            }
        }
    };

    private final TrackingObject trackingObject = new TrackingObject() {
        @Override
        public Object getTrackingKey() {
            return trackingId;
        }

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
    Object build(final android.content.Context context, AdsType adsType) {
        final String sellerId = BidMachineImpl.get().getSellerId();
        if (TextUtils.isEmpty(sellerId)) {
            return BMError.paramError("Seller Id not provided");
        }
        assert sellerId != null;

        BMError implVerifyError = verifyRequest();
        if (implVerifyError != null) {
            return implVerifyError;
        }

        final BidMachineImpl bidMachine = BidMachineImpl.get();

        AdvertisingPersonalData.syncUpdateInfo(context);

        final Request.Builder requestBuilder = Request.newBuilder();
        final TargetingParams targetingParams =
                RequestParams.resolveParams(this.targetingParams, bidMachine.getTargetingParams());
        final BlockedParams blockedParams = targetingParams.getBlockedParams();
        final UserRestrictionParams userRestrictionParams =
                RequestParams.resolveParams(this.userRestrictionParams, bidMachine.getUserRestrictionParams());
        unifiedAdRequestParams = createUnifiedAdRequestParams(targetingParams, userRestrictionParams);

        //PriceFloor params
        final PriceFloorParams priceFloorParams = oneOf(this.priceFloorParams, bidMachine.getPriceFloorParams());
        final Map<String, Double> priceFloorsMap =
                priceFloorParams.getPriceFloors() == null || priceFloorParams.getPriceFloors().size() == 0
                        ? bidMachine.getPriceFloorParams().getPriceFloors() : priceFloorParams.getPriceFloors();

        if (priceFloorsMap == null) {
            return BMError.paramError("PriceFloors not provided");
        }

        final ArrayList<Message.Builder> placements = new ArrayList<>();
        adsType.collectDisplayPlacements(new SimpleContextProvider(context),
                                         this,
                                         unifiedAdRequestParams,
                                         placements,
                                         networkConfigMap);

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

        final Placement.Builder placementBuilder = Placement.newBuilder();
        placementBuilder.setSsai(0);
        placementBuilder.setSdk(BidMachine.NAME);
        placementBuilder.setSdkver(BidMachine.VERSION);
        placementBuilder.setSecure(!io.bidmachine.core.Utils.canUseCleartextTraffic());
        for (Message.Builder displayBuilder : placements) {
            if (displayBuilder instanceof Placement.DisplayPlacement.Builder) {
                placementBuilder.setDisplay((Placement.DisplayPlacement.Builder) displayBuilder);
            } else if (displayBuilder instanceof Placement.VideoPlacement.Builder) {
                placementBuilder.setVideo((Placement.VideoPlacement.Builder) displayBuilder);
            } else {
                throw new IllegalArgumentException("Unsupported display type: " + displayBuilder);
            }
        }

        onBuildPlacement(placementBuilder);
        itemBuilder.setSpec(Any.pack(placementBuilder.build()));

        requestBuilder.addItem(itemBuilder.build());

        //Context
        final Context.Builder contextBuilder = Context.newBuilder();

        //Context -> App
        final Context.App.Builder appBuilder = Context.App.newBuilder();
        Publisher publisher = bidMachine.getPublisher();
        if (publisher != null) {
            publisher.build(appBuilder);
        }
        targetingParams.build(context, appBuilder);

        contextBuilder.setApp(appBuilder);

        //Context -> Restrictions
        if (blockedParams != null) {
            final Context.Restrictions.Builder restrictionsBuilder = Context.Restrictions.newBuilder();
            blockedParams.build(restrictionsBuilder);
            contextBuilder.setRestrictions(restrictionsBuilder);
        }

        //Context -> User
        final Context.User.Builder userBuilder = Context.User.newBuilder();
        userRestrictionParams.build(userBuilder);
        if (userRestrictionParams.canSendUserInfo()) {
            targetingParams.build(userBuilder);
        }
        contextBuilder.setUser(userBuilder);

        //Context -> Regs
        final Context.Regs.Builder regsBuilder = Context.Regs.newBuilder();
        userRestrictionParams.build(regsBuilder);
        contextBuilder.setRegs(regsBuilder);

        //Context -> Device
        final Context.Device.Builder deviceBuilder = Context.Device.newBuilder();
        bidMachine.getDeviceParams().build(context, deviceBuilder, targetingParams,
                bidMachine.getTargetingParams(), userRestrictionParams);
        contextBuilder.setDevice(deviceBuilder);

        requestBuilder.setContext(Any.pack(contextBuilder.build()));

        requestBuilder.setTest(bidMachine.isTestMode());
        requestBuilder.addCur("USD");
        requestBuilder.setAt(2);
        requestBuilder.setTmax(10000);

        //Request
        final RequestExtension.Builder requestExtensionBuilder = RequestExtension.newBuilder();
        requestExtensionBuilder.setSellerId(sellerId);
        requestExtensionBuilder.setHeaderBiddingType(
                headerBiddingEnabled
                        ? HeaderBiddingType.HEADER_BIDDING_TYPE_ENABLED
                        : HeaderBiddingType.HEADER_BIDDING_TYPE_DISABLED);
        requestExtensionBuilder.setBmIfv(bidMachine.obtainIFV(context));

        requestBuilder.addExtProto(Any.pack(requestExtensionBuilder.build()));

        return requestBuilder.build();
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
        BidMachineEvents.eventStart(trackingObject, TrackEventType.AuctionRequest, getType());
        try {
            unsubscribeExpireTracker();
            if (currentApiRequest != null) {
                currentApiRequest.cancel();
            }
            Logger.log(toString() + ": api request start");
            AdRequestExecutor.get().execute(new Runnable() {
                @Override
                public void run() {
                    Object requestBuildResult = build(context, getType());
                    if (requestBuildResult instanceof Request) {
                        ApiRequest.Builder<Request, Response> currentApiRequestBuilder = new ApiRequest.Builder<Request, Response>()
                                .url(BidMachineImpl.get().getAuctionUrl())
                                .setRequestData((Request) requestBuildResult)
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
                        if (isApiRequestCanceled.get()) {
                            return;
                        }
                        currentApiRequest = currentApiRequestBuilder.request();
                    } else {
                        processRequestFail(requestBuildResult instanceof BMError
                                ? (BMError) requestBuildResult
                                : BMError.Internal);
                    }
                }
            });
            if (timeOut > 0) {
                io.bidmachine.core.Utils.onBackgroundThread(timeOutRunnable, timeOut);
            }
        } catch (Exception e) {
            Logger.log(e);
            processRequestFail(BMError.Internal);
        }
    }

    public void notifyMediationWin() {
        if (!isApiRequestCompleted.get()) {
            return;
        }
        BidMachineEvents.eventFinish(trackingObject, TrackEventType.MediationWin, getType(), null);
    }

    public void notifyMediationLoss() {
        if (!isApiRequestCompleted.get()) {
            return;
        }
        BidMachineEvents.eventFinish(trackingObject, TrackEventType.MediationLoss, getType(), null);
    }

    void cancel() {
        if (isApiRequestCompleted.get() || isApiRequestCanceled.get()) {
            return;
        }
        if (currentApiRequest != null) {
            currentApiRequest.cancel();
            currentApiRequest = null;
        } else {
            processApiRequestCancel();
        }
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
        BidMachineEvents.eventFinish(
                trackingObject,
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
        if (adRequestListeners == null) {
            adRequestListeners = new ArrayList<>(2);
        }
        if (listener != null) {
            adRequestListeners.add(listener);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public void removeListener(@Nullable AdRequestListener<SelfType> listener) {
        if (adRequestListeners != null && listener != null) {
            adRequestListeners.remove(listener);
        }
    }

    void onShown() {
        unsubscribeExpireTracker();
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
        if (expTime > 0) {
            if (!isExpireTrackerSubscribed) {
                isExpireTrackerSubscribed = true;
                io.bidmachine.core.Utils.onBackgroundThread(expiredRunnable, expTime);
            }
        }
    }

    private void unsubscribeExpireTracker() {
        isExpireTrackerSubscribed = false;
        io.bidmachine.core.Utils.cancelBackgroundThreadTask(expiredRunnable);
    }

    @SuppressWarnings("unchecked")
    private void processApiRequestSuccess(@Nullable Response response) {
        if (isApiRequestCanceled.get()) {
            return;
        }
        isApiRequestCompleted.set(true);
        currentApiRequest = null;
        io.bidmachine.core.Utils.cancelBackgroundThreadTask(timeOutRunnable);
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
                    auctionResult = new AuctionResultImpl(getType(), seatbid, bid, ad, networkConfig);
                    expirationTime = getOrDefault(bid.getExp(),
                            Response.Seatbid.Bid.getDefaultInstance().getExp(),
                            DEF_EXPIRATION_TIME);
                    extractTrackUrls(bid);
                    subscribeExpireTracker();
                    Logger.log(toString() + ": Request finished (" + auctionResult + ")");
                    if (adRequestListeners != null) {
                        for (AdRequestListener listener : adRequestListeners) {
                            listener.onRequestSuccess(this, auctionResult);
                        }
                    }
                    for (AdRequestListener listener : BidMachineImpl.get().getAdRequestListeners()) {
                        listener.onRequestSuccess(this, auctionResult);
                    }
                    BidMachineEvents.eventFinish(
                            trackingObject,
                            TrackEventType.AuctionRequest,
                            getType(),
                            null);
                    return;
                } else {
                    Logger.log(toString() + ": Ad not found or not valid");
                }
            } catch (InvalidProtocolBufferException e) {
                Logger.log(e);
            }
        } else {
            Logger.log(toString() + ": Response not found or not valid");
        }
        processRequestFail(BMError.Internal);
    }

    private void processApiRequestFail(@Nullable BMError error) {
        if (isApiRequestCanceled.get()) {
            return;
        }
        isApiRequestCompleted.set(true);
        currentApiRequest = null;
        io.bidmachine.core.Utils.cancelBackgroundThreadTask(timeOutRunnable);
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
    private void processRequestFail(@NonNull BMError error) {
        if (adRequestListeners != null) {
            for (AdRequestListener listener : adRequestListeners) {
                listener.onRequestFailed(this, error);
            }
        }
        for (AdRequestListener adRequestListener : BidMachineImpl.get().getAdRequestListeners()) {
            adRequestListener.onRequestFailed(this, error);
        }
        BidMachineEvents.eventFinish(
                trackingObject,
                TrackEventType.AuctionRequest,
                getType(),
                error);
    }

    private void processApiRequestCancel() {
        isApiRequestCanceled.set(true);
        io.bidmachine.core.Utils.cancelBackgroundThreadTask(timeOutRunnable);

        BidMachineEvents.eventFinish(
                trackingObject,
                TrackEventType.AuctionRequestCancel,
                getType(),
                null);
        BidMachineEvents.clearEvent(trackingObject, TrackEventType.AuctionRequest);
    }

    @NonNull
    protected abstract UnifiedAdRequestParamsType createUnifiedAdRequestParams(@NonNull TargetingParams targetingParams,
                                                                               @NonNull DataRestrictions dataRestrictions);

    @Nullable
    final UnifiedAdRequestParamsType getUnifiedRequestParams() {
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
                        NetworkConfig networkConfig = NetworkConfig.create(
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
                for (NetworkConfig networkConfig : networkConfigList) {
                    params.networkConfigMap.put(networkConfig.getKey(), networkConfig);
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
        public SelfType disableHeaderBidding() {
            prepareRequest();
            params.headerBiddingEnabled = false;
            return (SelfType) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SelfType enableHeaderBidding() {
            prepareRequest();
            params.headerBiddingEnabled = true;
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