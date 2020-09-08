package io.bidmachine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

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

    private String lastBundle;
    private String lastAdDomain;
    private int clickCount;
    private int videoImpressionCount;
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
        if (lastBundle != null) {
            userExtBuilder.putFields(ProtoExtConstants.Context.User.LAST_BUNDLE,
                                     Value.newBuilder()
                                             .setStringValue(lastBundle)
                                             .build());
        }
        if (lastAdDomain != null) {
            userExtBuilder.putFields(ProtoExtConstants.Context.User.LAST_AD_DOMAIN,
                                     Value.newBuilder()
                                             .setStringValue(lastAdDomain)
                                             .build());
        }
    }

    @Nullable
    Integer getSessionDuration() {
        return sessionDuration;
    }

    @Override
    public SessionAdParams setSessionDuration(Integer sessionDuration) {
        if (sessionDuration >= 0) {
            this.sessionDuration = sessionDuration;
        }
        return this;
    }

    @Nullable
    Integer getImpressionCount() {
        return impressionCount;
    }

    @Override
    public SessionAdParams setImpressionCount(Integer impressionCount) {
        if (impressionCount >= 0) {
            this.impressionCount = impressionCount;
        }
        return this;
    }

    @Nullable
    Float getClickRate() {
        return clickRate;
    }

    @Override
    public SessionAdParams setClickRate(Float clickRate) {
        if (clickRate >= 0 && clickRate <= 100) {
            this.clickRate = clickRate;
        }
        return this;
    }

    @Nullable
    Boolean getUserClickedOnLastAd() {
        return isUserClickedOnLastAd;
    }

    @Override
    public SessionAdParams setIsUserClickedOnLastAd(Boolean isUserClickedOnLastAd) {
        this.isUserClickedOnLastAd = isUserClickedOnLastAd;
        return this;
    }

    @Nullable
    Float getCompletionRate() {
        return completionRate;
    }

    @Override
    public SessionAdParams setCompletionRate(Float completionRate) {
        if (completionRate >= 0 && completionRate <= 100) {
            this.completionRate = completionRate;
        }
        return this;
    }

    public String getLastBundle() {
        return lastBundle;
    }

    public void setLastBundle(String lastBundle) {
        this.lastBundle = lastBundle;
    }

    public String getLastAdDomain() {
        return lastAdDomain;
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

    int getClickCount() {
        return clickCount;
    }

    void addClick() {
        clickCount++;
        updateClickRate();
        isUserClickedOnLastAd = true;
    }

    private void updateClickRate() {
        if (impressionCount == null || impressionCount == 0) {
            return;
        }
        clickRate = (float) clickCount / impressionCount * 100;
    }

    public int getVideoImpressionCount() {
        return videoImpressionCount;
    }

    void addVideoImpression() {
        videoImpressionCount++;
        updateCompletionRate();
    }

    public int getCompletedVideosCount() {
        return completedVideosCount;
    }

    void addCompletedVideo() {
        completedVideosCount++;
        updateCompletionRate();
    }

    private void updateCompletionRate() {
        if (videoImpressionCount == 0) {
            return;
        }
        completionRate = (float) completedVideosCount / videoImpressionCount * 100;
    }

    @VisibleForTesting
    void clear() {
        sessionDuration = null;
        impressionCount = null;
        clickRate = null;
        isUserClickedOnLastAd = null;
        completionRate = null;
        lastBundle = null;
        lastAdDomain = null;
        clickCount = 0;
        videoImpressionCount = 0;
        completedVideosCount = 0;
    }

}