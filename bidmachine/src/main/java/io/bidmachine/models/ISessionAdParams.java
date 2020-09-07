package io.bidmachine.models;

import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;

public interface ISessionAdParams<SelfType> {

    /**
     * The total duration of time a user has spent so far in a specific app session expressed in seconds
     *
     * @param sessionDuration total session duration. Must not be negative.
     */
    SelfType setSessionDuration(@IntRange(from = 0) Integer sessionDuration);

    /**
     * The count of impressions for a specific placement type in a given app session.
     * The impression depth is reset once the session ends.
     *
     * @param impressionCount count of impressions. Must not be negative.
     */
    SelfType setImpressionCount(@IntRange(from = 0) Integer impressionCount);

    /**
     * The percentage of clicks/impressions per user per placement type over a given number of impressions
     *
     * @param clickRate percentage of clicks/impressions. Must be between 0 and 100.
     */
    SelfType setClickRate(@FloatRange(from = 0.0, to = 100.0) Float clickRate);

    /**
     * A boolean value indicating if the user clicked on the last impression in a given session per placement type
     *
     * @param isUserClickedOnLastAd value indicating if the user clicked on the last impression
     */
    SelfType setIsUserClickedOnLastAd(Boolean isUserClickedOnLastAd);

    /**
     * The percentage of successful completions/impressions for a user per placement type for a given number of impressions.
     * This only applies to Rewarded and Video placement types.
     *
     * @param completionRate percentage of successful completions/impressions. Must be between 0 and 100.
     */
    SelfType setCompletionRate(@FloatRange(from = 0.0, to = 100.0) Float completionRate);

}