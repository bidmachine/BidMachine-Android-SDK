package io.bidmachine.displays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;

import io.bidmachine.core.Logger;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.protobuf.headerbidding.HeaderBiddingAd;
import io.bidmachine.unified.UnifiedMediationParams;

class HeaderBiddingAdObjectParams extends AdObjectParams {

    @NonNull
    private final HeaderBiddingAd headerBiddingAd;
    private final HeaderBiddingUnifiedMediationParams mediationParams = new HeaderBiddingUnifiedMediationParams();

    HeaderBiddingAdObjectParams(@NonNull Response.Seatbid seatbid,
                                @NonNull Response.Seatbid.Bid bid,
                                @NonNull Ad ad,
                                @NonNull HeaderBiddingAd headerBiddingAd) {
        super(seatbid, bid, ad);

        this.headerBiddingAd = headerBiddingAd;
    }

    @Nullable
    private String getParam(@Nullable String key) {
        if (key == null) {
            return null;
        }
        String value = headerBiddingAd.getServerParamsOrDefault(key, null);
        if (value == null) {
            value = headerBiddingAd.getClientParamsOrDefault(key, null);
        }
        return value;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @NonNull
    @Override
    public UnifiedMediationParams toMediationParams() {
        return mediationParams;
    }


    private class HeaderBiddingUnifiedMediationParams extends UnifiedMediationParams {

        @Nullable
        @Override
        public String getString(@Nullable String key, String fallback) {
            String value = getParam(key);
            return value != null ? value : fallback;
        }

        @Override
        public int getInt(@Nullable String key, int fallback) {
            String value = getParam(key);
            if (value != null) {
                try {
                    return Integer.parseInt(value);
                } catch (Exception e) {
                    Logger.log(e);
                }
            }
            return fallback;
        }

        @Nullable
        @Override
        public Integer getInteger(@Nullable String key, @Nullable Integer fallback) {
            String value = getParam(key);
            if (value != null) {
                try {
                    return Integer.parseInt(value);
                } catch (Exception e) {
                    Logger.log(e);
                }
            }
            return fallback;
        }

        @Override
        public boolean getBool(@Nullable String key, boolean fallback) {
            String value = getParam(key);
            if (value != null) {
                try {
                    return Boolean.parseBoolean(value);
                } catch (Exception e) {
                    Logger.log(e);
                }
            }
            return fallback;
        }

        @Override
        public double getDouble(@Nullable String key, double fallback) {
            String value = getParam(key);
            if (value != null) {
                try {
                    return Double.parseDouble(value);
                } catch (Exception e) {
                    Logger.log(e);
                }
            }
            return fallback;
        }

        @Override
        public float getFloat(@Nullable String key, float fallback) {
            String value = getParam(key);
            if (value != null) {
                try {
                    return Float.parseFloat(value);
                } catch (Exception e) {
                    Logger.log(e);
                }
            }
            return fallback;
        }

        @Override
        public boolean contains(@Nullable String key) {
            return key != null &&
                    (headerBiddingAd.containsServerParams(key)
                            || headerBiddingAd.containsClientParams(key));
        }

    }

}