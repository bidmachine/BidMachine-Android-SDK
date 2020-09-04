package io.bidmachine;

import android.support.annotation.NonNull;

import com.explorestack.protobuf.Struct;
import com.explorestack.protobuf.Value;

import io.bidmachine.core.Utils;
import io.bidmachine.models.ISessionAdParams;
import io.bidmachine.models.RequestParams;

public class SessionAdParams extends RequestParams<SessionAdParams> implements ISessionAdParams<SessionAdParams> {

    private Integer sessionDuration;
    private Integer impressionCount;
    private Float clickRate;
    private Boolean isUserClickedOnLastAd;
    private Float completionRate;

    private String lastAdDomain;
    private int clickCount;
    private int completedVideosCount;

    @Override
    public void merge(@NonNull SessionAdParams instance) {
        sessionDuration = Utils.oneOf(sessionDuration, instance.sessionDuration);
        impressionCount = Utils.oneOf(impressionCount, instance.impressionCount);
        clickRate = Utils.oneOf(clickRate, instance.clickRate);
        isUserClickedOnLastAd = Utils.oneOf(isUserClickedOnLastAd, instance.isUserClickedOnLastAd);
        completionRate = Utils.oneOf(completionRate, instance.completionRate);
    }

    void fillUserExtension(Struct.Builder userExtBuilder) {
        if (sessionDuration != null) {
            userExtBuilder.putFields(ProtoExtConstants.Context.User.SESSION_DURATION,
                                     Value.newBuilder()
                                             .setNumberValue(sessionDuration)
                                             .build());
        }
        if (impressionCount != null) {
            userExtBuilder.putFields(ProtoExtConstants.Context.User.IMPRESSION_DEPTH,
                                     Value.newBuilder()
                                             .setNumberValue(impressionCount)
                                             .build());
        }
        if (clickRate != null) {
            userExtBuilder.putFields(ProtoExtConstants.Context.User.CLICK_RATE,
                                     Value.newBuilder()
                                             .setNumberValue(clickRate)
                                             .build());
        }
        if (isUserClickedOnLastAd != null) {
            userExtBuilder.putFields(ProtoExtConstants.Context.User.LAST_CLICK,
                                     Value.newBuilder()
                                             .setNumberValue(isUserClickedOnLastAd ? 1 : 0)
                                             .build());
        }
        if (completionRate != null) {
            userExtBuilder.putFields(ProtoExtConstants.Context.User.COMPLETION_RATE,
                                     Value.newBuilder()
                                             .setNumberValue(completionRate)
                                             .build());
        }
        if (lastAdDomain != null) {
            userExtBuilder.putFields(ProtoExtConstants.Context.User.LAST_AD_DOMAIN,
                                     Value.newBuilder()
                                             .setStringValue(lastAdDomain)
                                             .build());
        }
    }

    @Override
    public SessionAdParams setSessionDuration(Integer sessionDuration) {
        if (sessionDuration >= 0) {
            this.sessionDuration = sessionDuration;
        }
        return this;
    }

    @Override
    public SessionAdParams setImpressionCount(Integer impressionCount) {
        if (impressionCount >= 0) {
            this.impressionCount = impressionCount;
        }
        return this;
    }

    @Override
    public SessionAdParams setClickRate(Float clickRate) {
        if (clickRate >= 0 && clickRate <= 100) {
            this.clickRate = clickRate;
        }
        return this;
    }

    @Override
    public SessionAdParams setIsUserClickedOnLastAd(Boolean isUserClickedOnLastAd) {
        this.isUserClickedOnLastAd = isUserClickedOnLastAd;
        return this;
    }

    @Override
    public SessionAdParams setCompletionRate(Float completionRate) {
        if (completionRate >= 0 && completionRate <= 100) {
            this.completionRate = completionRate;
        }
        return this;
    }

    void setLastAdDomain(String lastAdDomain) {
        this.lastAdDomain = lastAdDomain;
    }

    void addImpression() {
        if (impressionCount == null) {
            impressionCount = 1;
        } else {
            impressionCount++;
        }
        updateClickRate();
    }

    void addClick() {
        clickCount++;
        updateClickRate();
    }

    private void updateClickRate() {
        if (impressionCount == null || impressionCount == 0) {
            return;
        }
        clickRate = (float) clickCount / impressionCount * 100;
    }

    void addCompletedVideos() {
        completedVideosCount++;
        updateCompletionRate();
    }

    private void updateCompletionRate() {
        if (impressionCount == null || impressionCount == 0) {
            return;
        }
        completionRate = (float) completedVideosCount / impressionCount * 100;
    }

}