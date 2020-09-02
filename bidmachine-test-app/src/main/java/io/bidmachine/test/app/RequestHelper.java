package io.bidmachine.test.app;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.bidmachine.AdContentType;
import io.bidmachine.MediaAssetType;
import io.bidmachine.banner.BannerListener;
import io.bidmachine.banner.BannerRequest;
import io.bidmachine.banner.BannerSize;
import io.bidmachine.banner.BannerView;
import io.bidmachine.interstitial.InterstitialAd;
import io.bidmachine.interstitial.InterstitialListener;
import io.bidmachine.interstitial.InterstitialRequest;
import io.bidmachine.models.AuctionResult;
import io.bidmachine.nativead.NativeAd;
import io.bidmachine.nativead.NativeListener;
import io.bidmachine.nativead.NativeRequest;
import io.bidmachine.rewarded.RewardedAd;
import io.bidmachine.rewarded.RewardedListener;
import io.bidmachine.rewarded.RewardedRequest;
import io.bidmachine.test.app.utils.TestActivityWrapper;
import io.bidmachine.test.app.utils.TestContextWrapper;
import io.bidmachine.utils.BMError;

public class RequestHelper {

    private static RequestHelper staticInstance;

    public static RequestHelper obtainStaticInstance(Context context) {
        if (staticInstance == null) {
            staticInstance = new RequestHelper(context);
        }
        return staticInstance;
    }

    private Context context;
    private Activity activity;
    private BannerView bannerView;

    public RequestHelper(final Context context) {
        if (context instanceof Activity) {
            this.context = new TestActivityWrapper((Activity) context);
        } else {
            this.context = new TestContextWrapper(context);
        }
        bannerView = new BannerView(this.context);
        bannerView.setBackgroundColor(this.context.getResources().getColor(android.R.color.darker_gray));
        bannerView.setVisibility(View.GONE);
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public BannerView getBannerView() {
        return bannerView;
    }

    /*
    Banner
     */

    private BannerRequest pendingBannerRequest;

    public void showBanner() {
        if (bannerView.isLoaded() && bannerView.canShow()) {
            bannerView.setVisibility(View.VISIBLE);
        } else {
            Utils.showToast(peekContext(), "Can't show banner: isLoaded=" + bannerView.isLoaded()
                    + ", canShow: " + bannerView.canShow());
        }
    }

    public void showPendingBanner() {
        if (pendingBannerRequest == null) {
            Utils.showToast(peekContext(), "Pending Banner not requested");
        } else {
            loadBanner(pendingBannerRequest, true);
            pendingBannerRequest = null;
        }
    }

    public void loadBanner(BannerSize bannerSize, final boolean show) {
        final BannerRequest request = ParamsHelper.getInstance(activity, ParamsHelper.AdsType.Banner)
                .createParams(new BannerRequest.Builder().setSize(bannerSize))
                .build();

        loadBanner(request, show);
    }

    public void loadBanner(BannerRequest request, final boolean show) {
        hideBanner();

        bannerView.setListener(new BannerListener() {
            @Override
            public void onAdLoaded(@NonNull BannerView ad) {
                if (show) bannerView.setVisibility(View.VISIBLE);
                Utils.showToast(peekContext(), "onAdLoaded");
            }

            @Override
            public void onAdLoadFailed(@NonNull BannerView ad, @NonNull BMError error) {
                Utils.showToast(peekContext(), "onAdLoadFailed: " + error.getMessage());
            }

            @Override
            public void onAdShown(@NonNull BannerView ad) {
                Utils.showToast(peekContext(), "onAdShown");
            }

            @Override
            public void onAdClicked(@NonNull BannerView ad) {
                Utils.showToast(peekContext(), "onAdClicked");
            }

            @Override
            public void onAdExpired(@NonNull BannerView ad) {
                Utils.showToast(peekContext(), "onAdExpired");
            }

            @Override
            public void onAdImpression(@NonNull BannerView ad) {
                Utils.showToast(peekContext(), "onAdImpression");
            }
        }).load(request);
    }

    public void requestBanner(BannerSize bannerSize) {
        pendingBannerRequest = ParamsHelper.getInstance(activity, ParamsHelper.AdsType.Banner)
                .createParams(new BannerRequest.Builder().setSize(bannerSize)
                        .setListener(new BannerRequest.AdRequestListener() {
                            @Override
                            public void onRequestSuccess(@NonNull BannerRequest request, @NonNull AuctionResult auctionResult) {
                                Utils.showToast(peekContext(), "onRequestSuccess: " + auctionResult);
                            }

                            @Override
                            public void onRequestFailed(@NonNull BannerRequest request, @NonNull BMError error) {
                                Utils.showToast(peekContext(), "onRequestFailed: " + error.getMessage());
                                pendingBannerRequest = null;
                            }

                            @Override
                            public void onRequestExpired(@NonNull BannerRequest request) {
                                Utils.showToast(peekContext(), "onRequestExpired: " + request);
                            }
                        }))
                .build();

        pendingBannerRequest.request(peekContext());
    }

    public void hideBanner() {
        if (bannerView != null) {
            bannerView.setVisibility(View.GONE);
            bannerView.destroy();
        }
    }

    /*
    Interstitial
     */

    private InterstitialAd currentInterstitialAd;
    private InterstitialRequest pendingInterstitialRequest;

    public void showInterstitial() {
        if (currentInterstitialAd != null) {
            currentInterstitialAd.show();
        } else {
            Utils.showToast(peekContext(), "Interstitial load not triggered");
        }
    }

    public void showPendingInterstitial() {
        if (pendingInterstitialRequest == null) {
            Utils.showToast(peekContext(), "Pending Interstitial not requested");
        } else {
            loadInterstitial(pendingInterstitialRequest, true);
            pendingInterstitialRequest = null;
        }
    }

    public void loadInterstitial(AdContentType contentType, final boolean show) {
        final InterstitialRequest request = ParamsHelper.getInstance(activity, ParamsHelper.AdsType.Interstitial)
                .createParams(new InterstitialRequest.Builder())
                .setAdContentType(contentType)
                .build();

        loadInterstitial(request, show);
    }

    public void loadInterstitial(InterstitialRequest request, final boolean show) {
        if (currentInterstitialAd != null) {
            currentInterstitialAd.destroy();
        }

        currentInterstitialAd = new InterstitialAd(context)
                .setListener(new InterstitialListener() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        if (show) ad.show();
                        Utils.showToast(peekContext(), "onAdLoaded");
                    }

                    @Override
                    public void onAdLoadFailed(@NonNull InterstitialAd ad, @NonNull BMError error) {
                        Utils.showToast(peekContext(), "onAdLoadFailed: " + error.getMessage());
                    }

                    @Override
                    public void onAdShown(@NonNull InterstitialAd ad) {
                        Utils.showToast(peekContext(), "onAdShown");
                    }

                    @Override
                    public void onAdClicked(@NonNull InterstitialAd ad) {
                        Utils.showToast(peekContext(), "onAdClicked");
                    }

                    @Override
                    public void onAdExpired(@NonNull InterstitialAd ad) {
                        Utils.showToast(peekContext(), "onAdExpired");
                    }

                    @Override
                    public void onAdShowFailed(@NonNull InterstitialAd ad, @NonNull BMError error) {
                        Utils.showToast(peekContext(), "onAdShowFailed: " + error.getMessage());
                    }

                    @Override
                    public void onAdImpression(@NonNull InterstitialAd ad) {
                        Utils.showToast(peekContext(), "onAdImpression");
                    }

                    @Override
                    public void onAdClosed(@NonNull InterstitialAd ad, boolean finished) {
                        Utils.showToast(peekContext(), "onAdClosed: finished=" + finished);
                    }
                }).load(request);
    }

    public void requestInterstitial(AdContentType contentType) {
        pendingInterstitialRequest = ParamsHelper.getInstance(activity, ParamsHelper.AdsType.Interstitial)
                .createParams(new InterstitialRequest.Builder()
                        .setListener(new InterstitialRequest.AdRequestListener() {
                            @Override
                            public void onRequestSuccess(@NonNull InterstitialRequest request, @NonNull AuctionResult auctionResult) {
                                Utils.showToast(peekContext(), "onRequestSuccess: " + auctionResult);
                            }

                            @Override
                            public void onRequestFailed(@NonNull InterstitialRequest request, @NonNull BMError error) {
                                Utils.showToast(peekContext(), "onRequestFailed: " + error.getMessage());
                                pendingInterstitialRequest = null;
                            }

                            @Override
                            public void onRequestExpired(@NonNull InterstitialRequest request) {
                                Utils.showToast(peekContext(), "onRequestExpired: " + request);
                            }
                        }))
                .setAdContentType(contentType)
                .build();

        pendingInterstitialRequest.request(peekContext());
    }

    /*
    Rewarded
     */

    private RewardedAd currentRewardedAd;
    private RewardedRequest pendingRewardedRequest;

    public void showRewarded() {
        if (currentRewardedAd != null) {
            currentRewardedAd.show();
        } else {
            Utils.showToast(peekContext(), "Rewarded load not triggered");
        }
    }

    public void showPendingRewarded() {
        if (pendingRewardedRequest == null) {
            Utils.showToast(peekContext(), "Pending Rewarded not requested");
        } else {
            loadRewarded(pendingRewardedRequest, true);
            pendingRewardedRequest = null;
        }
    }

    public void loadRewarded(final boolean show) {
        final RewardedRequest request = ParamsHelper.getInstance(activity, ParamsHelper.AdsType.Rewarded)
                .createParams(new RewardedRequest.Builder())
                .build();
        loadRewarded(request, show);
    }

    public void loadRewarded(RewardedRequest request, final boolean show) {
        if (currentRewardedAd != null) {
            currentRewardedAd.destroy();
        }
        currentRewardedAd = new RewardedAd(peekContext())
                .setListener(new RewardedListener() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        if (show) ad.show();
                        Utils.showToast(peekContext(), "onAdLoaded");
                    }

                    @Override
                    public void onAdLoadFailed(@NonNull RewardedAd ad, @NonNull BMError error) {
                        Utils.showToast(peekContext(), "onAdLoadFailed: " + error.getMessage());
                    }

                    @Override
                    public void onAdShown(@NonNull RewardedAd ad) {
                        Utils.showToast(peekContext(), "onAdShown");
                    }

                    @Override
                    public void onAdClicked(@NonNull RewardedAd ad) {
                        Utils.showToast(peekContext(), "onAdClicked");
                    }

                    @Override
                    public void onAdExpired(@NonNull RewardedAd ad) {
                        Utils.showToast(peekContext(), "onAdExpired");
                    }

                    @Override
                    public void onAdShowFailed(@NonNull RewardedAd ad, @NonNull BMError error) {
                        Utils.showToast(peekContext(), "onAdShowFailed: " + error.getMessage());
                    }

                    @Override
                    public void onAdImpression(@NonNull RewardedAd ad) {
                        Utils.showToast(peekContext(), "onAdImpression");
                    }

                    @Override
                    public void onAdClosed(@NonNull RewardedAd ad, boolean finished) {
                        Utils.showToast(peekContext(), "onAdClosed: finished=" + finished);
                    }

                    @Override
                    public void onAdRewarded(@NonNull RewardedAd ad) {
                        Utils.showToast(peekContext(), "onAdRewarded");
                    }
                }).load(request);
    }

    public void requestRewarded() {
        pendingRewardedRequest = ParamsHelper.getInstance(activity, ParamsHelper.AdsType.Rewarded)
                .createParams(new RewardedRequest.Builder()
                        .setListener(new RewardedRequest.AdRequestListener() {
                            @Override
                            public void onRequestSuccess(@NonNull RewardedRequest request, @NonNull AuctionResult auctionResult) {
                                Utils.showToast(peekContext(), "onRequestSuccess: " + auctionResult);
                            }

                            @Override
                            public void onRequestFailed(@NonNull RewardedRequest request, @NonNull BMError error) {
                                Utils.showToast(peekContext(), "onRequestFailed: " + error.getMessage());
                                pendingRewardedRequest = null;
                            }

                            @Override
                            public void onRequestExpired(@NonNull RewardedRequest request) {
                                Utils.showToast(peekContext(), "onRequestExpired: " + request);
                            }
                        }))
                .build();

        pendingRewardedRequest.request(peekContext());
    }

    /*
    Native
     */

    private NativeAd currentNativeAd;
    private NativeRequest pendingNativeRequest;

    public void loadNative(@Nullable MediaAssetType... mediaAssetTypes) {
        NativeRequest.Builder builder = new NativeRequest.Builder();
        if (mediaAssetTypes != null) {
            builder.setMediaAssetTypes(mediaAssetTypes);
        }
        NativeRequest request = ParamsHelper.getInstance(activity, ParamsHelper.AdsType.Native)
                .createParams(builder)
                .build();
        loadNative(request);
    }

    public void loadNative(NativeRequest request) {
        hideNative();

        currentNativeAd = new NativeAd(peekContext())
                .setListener(new NativeListener() {
                    @Override
                    public void onAdLoaded(@NonNull NativeAd ad) {
                        Utils.showToast(peekContext(), "onAdLoaded");
                    }

                    @Override
                    public void onAdLoadFailed(@NonNull NativeAd ad, @NonNull BMError error) {
                        Utils.showToast(peekContext(), "onAdLoadFailed: " + error.getMessage());
                    }

                    @Override
                    public void onAdShown(@NonNull NativeAd ad) {
                        Utils.showToast(peekContext(), "onAdShown");
                    }

                    @Override
                    public void onAdImpression(@NonNull NativeAd ad) {
                        Utils.showToast(peekContext(), "onAdImpression");
                    }

                    @Override
                    public void onAdClicked(@NonNull NativeAd ad) {
                        Utils.showToast(peekContext(), "onAdClicked");
                    }

                    @Override
                    public void onAdExpired(@NonNull NativeAd ad) {
                        Utils.showToast(peekContext(), "onAdExpired");
                    }
                }).load(request);
    }

    public void loadPendingNative() {
        if (pendingNativeRequest == null) {
            Utils.showToast(peekContext(), "Pending Native not requested");
        } else {
            loadNative(pendingNativeRequest);
        }
    }

    public void requestNative(@Nullable MediaAssetType... mediaAssetTypes) {
        NativeRequest.Builder builder = new NativeRequest.Builder();
        if (mediaAssetTypes != null && mediaAssetTypes.length > 0) {
            builder.setMediaAssetTypes(mediaAssetTypes);
        }
        builder.setListener(new NativeRequest.AdRequestListener() {
            @Override
            public void onRequestSuccess(@NonNull NativeRequest request,
                                         @NonNull AuctionResult auctionResult) {
                Utils.showToast(peekContext(), "onRequestSuccess: " + auctionResult);
            }

            @Override
            public void onRequestFailed(@NonNull NativeRequest request,
                                        @NonNull BMError error) {
                Utils.showToast(peekContext(), "onRequestFailed: " + error.getMessage());
                pendingNativeRequest = null;
            }

            @Override
            public void onRequestExpired(@NonNull NativeRequest request) {
                Utils.showToast(peekContext(), "onRequestExpired: " + request);
            }
        });
        pendingNativeRequest = ParamsHelper.getInstance(activity, ParamsHelper.AdsType.Native)
                .createParams(builder)
                .build();

        pendingNativeRequest.request(peekContext());
    }

    public NativeAd getLastNative() {
        return currentNativeAd;
    }

    public void hideNative() {
        if (currentNativeAd != null) {
            currentNativeAd.unregisterView();
            currentNativeAd.destroy();
        }
    }


    public void destroy() {
        if (bannerView != null) {
            bannerView.destroy();
        }
        if (currentInterstitialAd != null) {
            currentInterstitialAd.destroy();
            currentInterstitialAd = null;
        }
        if (currentRewardedAd != null) {
            currentRewardedAd.destroy();
            currentRewardedAd = null;
        }
        if (currentNativeAd != null) {
            currentNativeAd.destroy();
            currentNativeAd = null;
        }
    }

    private Context peekContext() {
        return activity != null ? activity : context;
    }

}
