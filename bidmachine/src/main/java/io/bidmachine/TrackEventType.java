package io.bidmachine;

import androidx.annotation.Nullable;

import com.explorestack.protobuf.adcom.EventType;

import io.bidmachine.protobuf.ActionType;
import io.bidmachine.protobuf.EventTypeExtended;

public enum TrackEventType {

    InitLoading(-1,
            EventTypeExtended.EVENT_TYPE_EXTENDED_SESSION_INITIALIZED_VALUE,
            ActionType.ACTION_TYPE_SESSION_INITIALIZING_VALUE),
    AuctionRequest(-1,
            EventTypeExtended.EVENT_TYPE_EXTENDED_REQUEST_LOADED_VALUE,
            ActionType.ACTION_TYPE_REQUEST_LOADING_VALUE),
    AuctionRequestCancel(-1,
            EventTypeExtended.EVENT_TYPE_EXTENDED_REQUEST_CANCELED_VALUE,
            ActionType.ACTION_TYPE_REQUEST_CANCELING_VALUE),
    AuctionRequestExpired(-1,
            EventTypeExtended.EVENT_TYPE_EXTENDED_REQUEST_EXPIRED_VALUE,
            EventTypeExtended.EVENT_TYPE_EXTENDED_REQUEST_EXPIRED_VALUE),
    AuctionRequestDestroy(-1,
            EventTypeExtended.EVENT_TYPE_EXTENDED_REQUEST_DESTROYED_VALUE,
            EventTypeExtended.EVENT_TYPE_EXTENDED_REQUEST_DESTROYED_VALUE),
    Load(-1,
            EventTypeExtended.EVENT_TYPE_EXTENDED_AD_LOADED_VALUE,
            ActionType.ACTION_TYPE_AD_LOADING_VALUE),
    Impression(-1,
            EventTypeExtended.EVENT_TYPE_EXTENDED_VIEWABLE_VALUE,
            ActionType.ACTION_TYPE_VIEWING_VALUE),
    Show(EventType.EVENT_TYPE_IMPRESSION_VALUE,
            EventTypeExtended.EVENT_TYPE_EXTENDED_IMPRESSION_VALUE,
            ActionType.ACTION_TYPE_SHOWING_VALUE),
    Click(-1,
            EventTypeExtended.EVENT_TYPE_EXTENDED_CLICK_VALUE,
            ActionType.ACTION_TYPE_CLICKING_VALUE),
    Close(-1,
            EventTypeExtended.EVENT_TYPE_EXTENDED_CLOSED_VALUE,
            ActionType.ACTION_TYPE_CLOSING_VALUE),
    Expired(-1,
            EventTypeExtended.EVENT_TYPE_EXTENDED_AD_EXPIRED_VALUE,
            EventTypeExtended.EVENT_TYPE_EXTENDED_AD_EXPIRED_VALUE),
    Error(-1,
            EventTypeExtended.EVENT_TYPE_EXTENDED_ERROR_VALUE,
            EventTypeExtended.EVENT_TYPE_EXTENDED_ERROR_VALUE),
    Destroy(-1,
            EventTypeExtended.EVENT_TYPE_EXTENDED_AD_DESTROYED_VALUE,
            ActionType.ACTION_TYPE_AD_DESTROYING_VALUE),
    TrackingError(-1,
            EventTypeExtended.EVENT_TYPE_EXTENDED_TRACKING_ERROR_VALUE,
            EventTypeExtended.EVENT_TYPE_EXTENDED_TRACKING_ERROR_VALUE),
    HeaderBiddingNetworksPrepare(-1,
            EventTypeExtended.EVENT_TYPE_EXTENDED_ALL_HB_NETWORKS_PREPARED_VALUE,
            EventTypeExtended.EVENT_TYPE_EXTENDED_ALL_HB_NETWORKS_PREPARED_VALUE),
    HeaderBiddingNetworkInitialize(-1,
            EventTypeExtended.EVENT_TYPE_EXTENDED_HB_NETWORK_INITIALIZED_VALUE,
            ActionType.ACTION_TYPE_HB_INITIALIZING_VALUE),
    HeaderBiddingNetworkPrepare(-1,
            EventTypeExtended.EVENT_TYPE_EXTENDED_HB_NETWORK_PREPARED_VALUE,
            ActionType.ACTION_TYPE_HB_PREPARING_VALUE),
    MediationWin(-1, -1, -1),
    MediationLoss(-1, -1, -1);

    private int ortbValue;
    private int ortbExtValue;
    private int ortbActionValue;

    TrackEventType(int ortbValue, int ortbExtValue, int ortbActionValue) {
        this.ortbValue = ortbValue;
        this.ortbExtValue = ortbExtValue;
        this.ortbActionValue = ortbActionValue;
    }

    @Nullable
    public static TrackEventType fromNumber(int number) {
        for (TrackEventType eventType : values()) {
            if (eventType.ortbValue == number || eventType.ortbExtValue == number) {
                return eventType;
            }
        }
        return null;
    }

    public int getOrtbActionValue() {
        return ortbActionValue;
    }

}