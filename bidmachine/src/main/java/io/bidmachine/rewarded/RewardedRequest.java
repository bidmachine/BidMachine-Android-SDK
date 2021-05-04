package io.bidmachine.rewarded;

import androidx.annotation.NonNull;

import com.explorestack.protobuf.adcom.Placement;

import io.bidmachine.AdRequest;
import io.bidmachine.AdsType;
import io.bidmachine.FullScreenAdRequest;
import io.bidmachine.utils.ProtoUtils;

public final class RewardedRequest extends FullScreenAdRequest<RewardedRequest> {

    private RewardedRequest() {
        super(AdsType.Rewarded);
    }

    @Override
    protected void onBuildPlacement(Placement.Builder builder) {
        super.onBuildPlacement(builder);

        builder.setReward(true);
    }

    @Override
    protected boolean isPlacementObjectValid(@NonNull Placement placement) throws Throwable {
        return ProtoUtils.isRewardedPlacement(placement);
    }

    public static final class Builder extends FullScreenRequestBuilder<Builder, RewardedRequest> {

        @Override
        protected RewardedRequest createRequest() {
            return new RewardedRequest();
        }

    }

    public interface AdRequestListener extends AdRequest.AdRequestListener<RewardedRequest> {

    }

}