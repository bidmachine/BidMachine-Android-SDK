package io.bidmachine;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.bidmachine.banner.BannerSize;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedNativeAdRequestParams;

public enum AdsFormat {
    Banner("banner",
           new AdsFormatMatcher<UnifiedBannerAdRequestParams>(AdsType.Banner) {
               @Override
               boolean isMatch(@NonNull UnifiedBannerAdRequestParams adRequestParams,
                               @NonNull AdContentType adContentType) {
                   return true;
               }
           }),
    Banner_320x50("banner_320x50",
                  new AdsFormatMatcher<UnifiedBannerAdRequestParams>(AdsType.Banner) {
                      @Override
                      boolean isMatch(@NonNull UnifiedBannerAdRequestParams adRequestParams,
                                      @NonNull AdContentType adContentType) {
                          return adRequestParams.getBannerSize() == BannerSize.Size_320x50;
                      }
                  }),
    Banner_300x250("banner_300x250",
                   new AdsFormatMatcher<UnifiedBannerAdRequestParams>(AdsType.Banner) {
                       @Override
                       boolean isMatch(@NonNull UnifiedBannerAdRequestParams adRequestParams,
                                       @NonNull AdContentType adContentType) {
                           return adRequestParams.getBannerSize() == BannerSize.Size_300x250;
                       }
                   }),
    Banner_728x90("banner_728x90",
                  new AdsFormatMatcher<UnifiedBannerAdRequestParams>(AdsType.Banner) {
                      @Override
                      boolean isMatch(@NonNull UnifiedBannerAdRequestParams adRequestParams,
                                      @NonNull AdContentType adContentType) {
                          return adRequestParams.getBannerSize() == BannerSize.Size_728x90;
                      }
                  }),
    Interstitial("interstitial",
                 new AdsFormatMatcher<UnifiedFullscreenAdRequestParams>(AdsType.Interstitial) {
                     @Override
                     boolean isMatch(@NonNull UnifiedFullscreenAdRequestParams adRequestParams,
                                     @NonNull AdContentType adContentType) {
                         return true;
                     }
                 }),
    InterstitialVideo("interstitial_video",
                      new AdsFormatMatcher<UnifiedFullscreenAdRequestParams>(AdsType.Interstitial) {
                          @Override
                          boolean isMatch(@NonNull UnifiedFullscreenAdRequestParams adRequestParams,
                                          @NonNull AdContentType adContentType) {
                              return adContentType == AdContentType.Video;
                          }
                      }),
    InterstitialStatic("interstitial_static",
                       new AdsFormatMatcher<UnifiedFullscreenAdRequestParams>(AdsType.Interstitial) {
                           @Override
                           boolean isMatch(@NonNull UnifiedFullscreenAdRequestParams adRequestParams,
                                           @NonNull AdContentType adContentType) {
                               return adContentType == AdContentType.Static;
                           }
                       }),
    Rewarded("rewarded",
             new AdsFormatMatcher<UnifiedFullscreenAdRequestParams>(AdsType.Rewarded) {
                 @Override
                 boolean isMatch(@NonNull UnifiedFullscreenAdRequestParams adRequestParams,
                                 @NonNull AdContentType adContentType) {
                     return true;
                 }
             }),
    RewardedVideo("rewarded_video",
                  new AdsFormatMatcher<UnifiedFullscreenAdRequestParams>(AdsType.Rewarded) {
                      @Override
                      boolean isMatch(@NonNull UnifiedFullscreenAdRequestParams adRequestParams,
                                      @NonNull AdContentType adContentType) {
                          return adContentType == AdContentType.Video;
                      }
                  }),
    RewardedStatic("rewarded_static",
                   new AdsFormatMatcher<UnifiedFullscreenAdRequestParams>(AdsType.Rewarded) {
                       @Override
                       boolean isMatch(@NonNull UnifiedFullscreenAdRequestParams adRequestParams,
                                       @NonNull AdContentType adContentType) {
                           return adContentType == AdContentType.Static;
                       }
                   }),
    Native("native",
           new AdsFormatMatcher<UnifiedNativeAdRequestParams>(AdsType.Native) {
               @Override
               boolean isMatch(@NonNull UnifiedNativeAdRequestParams adRequestParams,
                               @NonNull AdContentType adContentType) {
                   return true;
               }
           });

    @NonNull
    private String remoteName;
    @NonNull
    private AdsFormatMatcher matcher;

    AdsFormat(@NonNull String remoteName, @NonNull AdsFormatMatcher matcher) {
        this.remoteName = remoteName;
        this.matcher = matcher;
    }

    @SuppressWarnings("unchecked")
    <T extends UnifiedAdRequestParams> boolean isMatch(@NonNull AdsType adsType,
                                                       @NonNull T adRequestParams,
                                                       @NonNull AdContentType adContentType) {
        return matcher.isMatch(adsType, adRequestParams, adContentType);
    }

    @Nullable
    static AdsFormat byRemoteName(@Nullable String name) {
        if (!TextUtils.isEmpty(name)) {
            for (AdsFormat type : values()) {
                if (type.remoteName.equals(name)) {
                    return type;
                }
            }
        }
        return null;
    }

    private static abstract class AdsFormatMatcher<T extends UnifiedAdRequestParams> {

        @NonNull
        private AdsType adsType;

        AdsFormatMatcher(@NonNull AdsType adsType) {
            this.adsType = adsType;
        }

        final boolean isMatch(@NonNull AdsType adsType,
                              @NonNull T adRequestParams,
                              @NonNull AdContentType adContentType) {
            return adsType == this.adsType && isMatch(adRequestParams, adContentType);
        }

        abstract boolean isMatch(@NonNull T adRequestParams,
                                 @NonNull AdContentType adContentType);

    }
}
