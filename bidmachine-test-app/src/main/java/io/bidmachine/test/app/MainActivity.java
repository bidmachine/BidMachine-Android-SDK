package io.bidmachine.test.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.wefika.flowlayout.FlowLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import io.bidmachine.AdContentType;
import io.bidmachine.AdsFormat;
import io.bidmachine.BidMachine;
import io.bidmachine.MediaAssetType;
import io.bidmachine.ads.networks.AmazonConfig;
import io.bidmachine.ads.networks.adcolony.AdColonyConfig;
import io.bidmachine.ads.networks.criteo.CriteoConfig;
import io.bidmachine.ads.networks.facebook.FacebookConfig;
import io.bidmachine.ads.networks.my_target.MyTargetConfig;
import io.bidmachine.ads.networks.tapjoy.TapjoyConfig;
import io.bidmachine.banner.BannerSize;
import io.bidmachine.banner.BannerView;
import io.bidmachine.nativead.NativeAd;
import io.bidmachine.nativead.view.NativeAdContentLayout;
import io.bidmachine.nativead.view.NativeMediaView;
import io.bidmachine.test.app.params.AdsParamsFragment;
import io.bidmachine.test.app.params.AppParamsFragment;
import io.bidmachine.test.app.params.ExtraParamsFragment;
import io.bidmachine.test.app.params.PublisherParamsFragment;
import io.bidmachine.test.app.params.TargetingParamsFragment;
import io.bidmachine.test.app.params.UserRestrictionsParamsFragment;
import io.bidmachine.test.app.utils.TestActivityWrapper;

public class MainActivity extends AppCompatActivity {

    private static final String SAVED_StaticMode = "StaticMode";

    private ViewGroup bannerFrame;
    private LinearLayout nativeAdParent;
    private RadioGroup bannerSizesParent;
    private RadioGroup interstitialFormatParent;
    private FlowLayout nativeMediaAssetParent;
    private TextView txtLocation;

    private RequestHelper requestHelper;
    private boolean isStaticMode;
    private boolean isResumed;

    private static final OptionalNetwork[] optionalNetworks = {

            new OptionalNetwork("AdColony",
                                new AdColonyConfig("app185a7e71e1714831a49ec7")
                                        .withMediationConfig(AdsFormat.InterstitialVideo,
                                                             "vz06e8c32a037749699e7050")
                                        .withMediationConfig(AdsFormat.RewardedVideo,
                                                             "vz1fd5a8b2bf6841a0a4b826"),
                                "adcolony.json"),

            new OptionalNetwork("Amazon",
                                new AmazonConfig("a9_onboarding_app_id")
                                        .withMediationConfig(AdsFormat.Banner_320x50,
                                                             "5ab6a4ae-4aa5-43f4-9da4-e30755f2b295")
                                        .withMediationConfig(AdsFormat.Banner_300x250,
                                                             "54fb2d08-c222-40b1-8bbe-4879322dc04b")
                                        .withMediationConfig(AdsFormat.Banner_728x90,
                                                             "bed17ec3-b185-453e-b2a8-4a3c6bb9234d")
                                        .withMediationConfig(AdsFormat.InterstitialStatic,
                                                             "4e918ac0-5c68-4fe1-8d26-4e76e8f74831")
                                        .withMediationConfig(AdsFormat.InterstitialVideo,
                                                             "4acc26e6-3ada-4ee8-bae0-753c1e0ad278"),
                                "amazon.json"),

            new OptionalNetwork("Criteo",
                                new CriteoConfig("B-000000")
                                        .withMediationConfig(AdsFormat.Banner_320x50,
                                                             "30s6zt3ayypfyemwjvmp")
                                        .withMediationConfig(AdsFormat.Interstitial,
                                                             "6yws53jyfjgoq1ghnuqb"),
                                "criteo.json"),

            new OptionalNetwork("Facebook",
                                new FacebookConfig("1525692904128549")
                                        .withMediationConfig(AdsFormat.Banner,
                                                             "1525692904128549_2386746951356469")
                                        .withMediationConfig(AdsFormat.Banner_300x250,
                                                             "1525692904128549_2386746951356469")
                                        .withMediationConfig(AdsFormat.InterstitialStatic,
                                                             "1525692904128549_2386743441356820")
                                        .withMediationConfig(AdsFormat.RewardedVideo,
                                                             "1525692904128549_2386753464689151"),
                                "facebook.json"),

            new OptionalNetwork("myTarget",
                                new MyTargetConfig()
                                        .withMediationConfig(AdsFormat.Banner, "437933")
                                        .withMediationConfig(AdsFormat.Banner_320x50, "437933")
                                        .withMediationConfig(AdsFormat.Banner_300x250, "64526")
                                        .withMediationConfig(AdsFormat.Banner_728x90, "81620")
                                        .withMediationConfig(AdsFormat.InterstitialStatic, "365991")
                                        .withMediationConfig(AdsFormat.RewardedVideo, "482205"),
                                "my_target.json"),

            new OptionalNetwork("Tapjoy",
                                new TapjoyConfig(
                                        "tmyN5ZcXTMyjeJNJmUD5ggECAbnEGtJREmLDd0fvqKBXcIr7e1dvboNKZI4y")
                                        .withMediationConfig(AdsFormat.InterstitialVideo,
                                                             "video_without_cap_pb")
                                        .withMediationConfig(AdsFormat.RewardedVideo,
                                                             "rewarded_video_without_cap_pb"),
                                "tapjoy.json")
    };

    private final Collection<OptionalNetwork> checkedOptionalNetworks =
            new HashSet<>(Arrays.asList(optionalNetworks));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final SpannableStringBuilder appInfoBuilder = new SpannableStringBuilder();
        appInfoBuilder.append("Version: ");
        appendBold(appInfoBuilder, BidMachine.VERSION).append("   ");

        ((TextView) findViewById(R.id.txtAppInfo)).setText(appInfoBuilder);
        bannerFrame = findViewById(R.id.bannerFrame);
        nativeAdParent = findViewById(R.id.nativeAdParent);

        ((SwitchCompat) findViewById(R.id.switchTestMode))
                .setOnCheckedChangeListener((buttonView, isChecked) ->
                                                    BidMachine.setTestMode(isChecked));

        bannerSizesParent = findViewById(R.id.bannerSizesParent);
        for (BannerSize size : BannerSize.values()) {
            final RadioButton radioButton = new RadioButton(this);
            int id = ("rbtn" + size.name()).hashCode();
            radioButton.setId(id < 0 ? -id : id);
            radioButton.setText(size.name()
                                        .replace("Size_", "")
                                        .replace("_", " x "));
            radioButton.setTag(size);
            bannerSizesParent.addView(radioButton,
                                      new RadioGroup.LayoutParams(0,
                                                                  ViewGroup.LayoutParams.MATCH_PARENT,
                                                                  1f));
        }
        if (savedInstanceState == null) {
            bannerSizesParent.check(bannerSizesParent.getChildAt(0).getId());
        }

        interstitialFormatParent = findViewById(R.id.interstitialFormatParent);
        for (AdContentType format : AdContentType.values()) {
            final RadioButton radioButton = new RadioButton(this);
            int id = ("rbtn" + format.name()).hashCode();
            radioButton.setId(id < 0 ? -id : id);
            radioButton.setText(format.name());
            radioButton.setTag(format);
            interstitialFormatParent.addView(radioButton,
                                             new RadioGroup.LayoutParams(0,
                                                                         ViewGroup.LayoutParams.MATCH_PARENT,
                                                                         1f));
        }
        if (savedInstanceState == null) {
            interstitialFormatParent.check(interstitialFormatParent.getChildAt(0).getId());
        }

        nativeMediaAssetParent = findViewById(R.id.nativeMediaAssetParent);
        for (MediaAssetType mediaAssetType : MediaAssetType.values()) {
            CheckBox checkBox = new CheckBox(this);
            int id = ("cbmat" + mediaAssetType.name()).hashCode();
            checkBox.setId(id < 0 ? -id : id);
            checkBox.setText(mediaAssetType.name());
            checkBox.setTag(mediaAssetType);
            nativeMediaAssetParent.addView(checkBox,
                                           new FlowLayout.LayoutParams(
                                                   ViewGroup.LayoutParams.WRAP_CONTENT,
                                                   ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        ((SwitchCompat) findViewById(R.id.switchStaticMode))
                .setOnCheckedChangeListener((buttonView, isChecked) -> setStaticMode(isChecked));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final ArrayList<String> requiredPermissions = new ArrayList<String>() {{
                add(Manifest.permission.ACCESS_COARSE_LOCATION);
                add(Manifest.permission.ACCESS_FINE_LOCATION);
            }};
            final Iterator<String> requiredPermissionsIterator = requiredPermissions.iterator();
            while (requiredPermissionsIterator.hasNext()) {
                String permission = requiredPermissionsIterator.next();
                if (ContextCompat.checkSelfPermission(this, permission) ==
                        PackageManager.PERMISSION_GRANTED) {
                    requiredPermissionsIterator.remove();
                }
            }
            if (!requiredPermissions.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                                                  requiredPermissions.toArray(new String[0]),
                                                  1);
            }
        }

        txtLocation = findViewById(R.id.txtLocation);
        txtLocation.setOnClickListener(v -> updateCurrentLocation());

        getSupportFragmentManager().addOnBackStackChangedListener(this::syncBackIcon);
        syncBackIcon();

        if (savedInstanceState != null) {
            isStaticMode = savedInstanceState.getBoolean(SAVED_StaticMode);
        }
        setStaticMode(isStaticMode);
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(SAVED_StaticMode, isStaticMode);
    }

    @Override
    protected void onResume() {
        super.onResume();

        isResumed = true;
        //Required for display toast only!!!
        requestHelper.setActivity(this);
        updateCurrentLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();

        isResumed = false;
        //Required for display toast only!!!
        requestHelper.setActivity(null);
    }

    private void setStaticMode(boolean staticMode) {
        isStaticMode = staticMode;
        if (isStaticMode) {
            requestHelper = RequestHelper.obtainStaticInstance(this);
        } else {
            requestHelper = new RequestHelper(this);
        }
        syncRequestHelper(requestHelper);
    }

    private void syncRequestHelper(RequestHelper requestHelper) {
        if (isResumed) requestHelper.setActivity(this);
        bannerFrame.removeAllViews();
        final BannerView bannerView = requestHelper.getBannerView();
        if (bannerView.getParent() instanceof ViewGroup) {
            ((ViewGroup) bannerView.getParent()).removeView(bannerView);
        }
        bannerFrame.addView(bannerView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void syncBackIcon() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(
                    getSupportFragmentManager().getBackStackEntryCount() > 0);
        }
    }

    /*
    Global params
     */

    public void showTargetingParams(View view) {
        showParamsFragment(new TargetingParamsFragment(), "TargetingParamsFragment");
    }

    public void showUserRestrictionsParams(View view) {
        showParamsFragment(new UserRestrictionsParamsFragment(), "UserRestrictionsParamsFragment");
    }

    public void showExtraParams(View view) {
        showParamsFragment(new ExtraParamsFragment(), "ExtraParamsFragment");
    }

    private void showParamsFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.parent, fragment, tag)
                .addToBackStack(tag)
                .commitAllowingStateLoss();
    }

    public void loadBanner(View view) {
        BannerSize bannerSize = (BannerSize) bannerSizesParent
                .findViewById(bannerSizesParent.getCheckedRadioButtonId()).getTag();
        requestHelper.loadBanner(bannerSize, false);
    }

    public void showBanner(View view) {
        requestHelper.showBanner();
    }

    public void requestBanner(View view) {
        BannerSize bannerSize = (BannerSize) bannerSizesParent
                .findViewById(bannerSizesParent.getCheckedRadioButtonId()).getTag();
        requestHelper.requestBanner(bannerSize);
    }

    public void showRequestedBanner(View view) {
        requestHelper.showPendingBanner();
    }

    public void hideBanner(View view) {
        requestHelper.hideBanner();
    }

    public void loadInterstitial(View view) {
        final AdContentType contentType = (AdContentType) interstitialFormatParent
                .findViewById(interstitialFormatParent.getCheckedRadioButtonId()).getTag();
        requestHelper.loadInterstitial(contentType, false);
    }

    public void showInterstitial(View view) {
        requestHelper.showInterstitial();
    }

    public void requestInterstitial(View view) {
        final AdContentType contentType = (AdContentType) interstitialFormatParent
                .findViewById(interstitialFormatParent.getCheckedRadioButtonId()).getTag();
        requestHelper.requestInterstitial(contentType);
    }

    public void showRequestedInterstitial(View view) {
        requestHelper.showPendingInterstitial();
    }

    public void loadRewarded(View view) {
        requestHelper.loadRewarded(false);
    }

    public void showRewarded(View view) {
        requestHelper.showRewarded();
    }

    public void requestRewarded(View view) {
        requestHelper.requestRewarded();
    }

    public void showRequestedRewarded(View view) {
        requestHelper.showPendingRewarded();
    }

    /*
    Native
     */

    public void loadNative(View view) {
        MediaAssetType[] mediaAssetTypes = collectMediaAssetType();
        requestHelper.loadNative(mediaAssetTypes);
    }

    public void showNative(View view) {
        hideNative(null);

        NativeAd nativeAd = requestHelper.getLastNative();
        if (nativeAd != null) {
            if (nativeAd.isLoaded() && nativeAd.canShow()) {
                inflateNativeAdWithBind(nativeAd);
            } else {
                Utils.showToast(this,
                                String.format("Can't show native: isLoaded - %s, canShow - %s",
                                              nativeAd.isLoaded(),
                                              nativeAd.canShow()));
            }
        } else {
            Utils.showToast(this, "NativeAd is null");
        }
    }

    public void requestNative(View view) {
        MediaAssetType[] mediaAssetTypes = collectMediaAssetType();
        requestHelper.requestNative(mediaAssetTypes);
    }

    public void loadRequestedNative(View view) {
        requestHelper.loadPendingNative();
    }

    private void inflateNativeAd(NativeAd ad) {
        if (ad == null) {
            return;
        }
        final NativeAdContentLayout nativeAdContentLayout = (NativeAdContentLayout) getLayoutInflater()
                .inflate(R.layout.include_native_ads, nativeAdParent, false);

        final TextView tvTitle = nativeAdContentLayout.findViewById(R.id.txtTitle);
        tvTitle.setText(ad.getTitle());
        nativeAdContentLayout.setTitleView(tvTitle);

        final TextView tvDescription = nativeAdContentLayout.findViewById(R.id.txtDescription);
        tvDescription.setText(ad.getDescription());
        nativeAdContentLayout.setDescriptionView(tvDescription);

        final RatingBar ratingBar = nativeAdContentLayout.findViewById(R.id.ratingBar);
        if (ad.getRating() == 0) {
            ratingBar.setVisibility(View.INVISIBLE);
        } else {
            ratingBar.setVisibility(View.VISIBLE);
            ratingBar.setRating(ad.getRating());
            ratingBar.setStepSize(0.1f);
        }
        nativeAdContentLayout.setRatingView(ratingBar);

        final Button ctaButton = nativeAdContentLayout.findViewById(R.id.btnCta);
        ctaButton.setText(ad.getCallToAction());
        nativeAdContentLayout.setCallToActionView(ctaButton);

        final ImageView icon = nativeAdContentLayout.findViewById(R.id.icon);
        nativeAdContentLayout.setIconView(icon);

        final View providerView = ad.getProviderView(nativeAdParent.getContext());
        if (providerView != null) {
            if (providerView.getParent() != null && providerView.getParent() instanceof ViewGroup) {
                ((ViewGroup) providerView.getParent()).removeView(providerView);
            }
            FrameLayout providerViewContainer = nativeAdContentLayout.findViewById(R.id.providerView);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            providerViewContainer.addView(providerView, layoutParams);
        }
        nativeAdContentLayout.setProviderView(providerView);

        NativeMediaView nativeMediaView = nativeAdContentLayout.findViewById(R.id.mediaView);
        nativeAdContentLayout.setMediaView(nativeMediaView);

        nativeAdContentLayout.registerViewForInteraction(ad);
        nativeAdContentLayout.setVisibility(View.VISIBLE);

        nativeAdParent.removeAllViews();
        nativeAdParent.addView(nativeAdContentLayout);
    }

    private void inflateNativeAdWithBind(NativeAd ad) {
        if (ad == null) {
            return;
        }
        final NativeAdContentLayout nativeAdContentLayout = (NativeAdContentLayout) getLayoutInflater()
                .inflate(R.layout.include_native_ads, nativeAdParent, false);
        nativeAdContentLayout.bind(ad);
        nativeAdContentLayout.registerViewForInteraction(ad);
        nativeAdContentLayout.setVisibility(View.VISIBLE);
        nativeAdParent.removeAllViews();
        nativeAdParent.addView(nativeAdContentLayout);
    }

    public void hideNative(View view) {
        int childCount = nativeAdParent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            NativeAdContentLayout child = (NativeAdContentLayout) nativeAdParent.getChildAt(i);
            child.unregisterViewForInteraction();
            child.destroy();
        }
        nativeAdParent.removeAllViews();
    }

    public void isRegisteredNative(View view) {
        boolean isRegistered = false;
        if (nativeAdParent.getChildCount() > 0) {
            isRegistered = ((NativeAdContentLayout) nativeAdParent.getChildAt(0)).isRegistered();
        }
        Utils.showToast(this, String.valueOf(isRegistered));
    }

    public void openNativeActivity(View view) {
        if (!BidMachine.isInitialized()) {
            Utils.showToast(this, "Initialize BidMachine first");
            return;
        }
        Intent intent = NativeActivity.createIntent(this, collectMediaAssetType());
        startActivity(intent);
    }

    private MediaAssetType[] collectMediaAssetType() {
        List<MediaAssetType> mediaAssetTypeList = new ArrayList<>();
        for (int i = 0; i < nativeMediaAssetParent.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) nativeMediaAssetParent.getChildAt(i);
            if (checkBox.isChecked()) {
                mediaAssetTypeList.add((MediaAssetType) checkBox.getTag());
            }
        }
        return mediaAssetTypeList.toArray(new MediaAssetType[0]);
    }

    private SpannableStringBuilder appendBold(SpannableStringBuilder builder, Object text) {
        int startLength = builder.length();
        builder.append(String.valueOf(text));
        builder.setSpan(new StyleSpan(Typeface.BOLD),
                        startLength,
                        builder.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new ForegroundColorSpan(Color.BLACK),
                        startLength,
                        builder.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!isStaticMode) {
            requestHelper.destroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        updateCurrentLocation();
    }

    public void initSdk(View view) {
        if (((SwitchCompat) findViewById(R.id.switchUseNetworksJson)).isChecked()) {
            JSONArray networkArray = new JSONArray();
            for (OptionalNetwork network : checkedOptionalNetworks) {
                InputStream inputStream = null;
                try {
                    inputStream = getAssets().open(network.assetFile);
                    String networkConfig = io.bidmachine.core.Utils.streamToString(inputStream);
                    networkArray.put(new JSONObject(networkConfig));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    io.bidmachine.core.Utils.close(inputStream);
                }
            }
            BidMachine.registerNetworks(this, networkArray.toString());
        } else {
            for (OptionalNetwork network : checkedOptionalNetworks) {
                BidMachine.registerNetworks(network.networkConfig);
            }
        }
        String initUrl = ParamsHelper.getInstance(this).getInitUrl();
        if (!TextUtils.isEmpty(initUrl)) {
            BidMachine.setEndpoint(initUrl);
        }
        BidMachine.initialize(new TestActivityWrapper(this),
                              ParamsHelper.getInstance(this).getSellerId(),
                              () -> Utils.showToast(this, "onInitialized"));
    }

    public void showAppParams(View view) {
        showParamsFragment(new AppParamsFragment(), "AppParamsFragment");
    }

    public void showPublisherParams(View view) {
        showParamsFragment(new PublisherParamsFragment(), "PublisherParamsFragment");
    }

    public void showBannerParams(View view) {
        showParamsFragment(AdsParamsFragment.create(ParamsHelper.AdsType.Banner),
                           "BannerAdsFragment");
    }

    public void showInterstitialParams(View view) {
        showParamsFragment(AdsParamsFragment.create(ParamsHelper.AdsType.Interstitial),
                           "InterstitialAdsFragment");
    }

    public void showRewardedParams(View view) {
        showParamsFragment(AdsParamsFragment.create(ParamsHelper.AdsType.Rewarded),
                           "RewardedAdsFragment");
    }

    public void showNativeParams(View view) {
        showParamsFragment(AdsParamsFragment.create(ParamsHelper.AdsType.Native),
                           "NativeAdsFragment");
    }

    @SuppressLint("SetTextI18n")
    private void updateCurrentLocation() {
        DecimalFormat df = new DecimalFormat("####0.000");
        Location location = Utils.getLocation(this);
        if (location != null) {
            txtLocation.setText(new StringBuilder()
                                        .append("Latitude: ")
                                        .append(df.format(location.getLatitude()))
                                        .append(", Longitude: ")
                                        .append(df.format(location.getLongitude())));
        } else {
            txtLocation.setText("Location not available");
        }
    }

    public void configureNetworks(View view) {
        String[] titles = new String[optionalNetworks.length];
        boolean[] checkedItems = new boolean[optionalNetworks.length];
        for (int i = 0; i < optionalNetworks.length; i++) {
            OptionalNetwork network = optionalNetworks[i];
            titles[i] = network.displayName;
            checkedItems[i] = checkedOptionalNetworks.contains(network);
        }
        new AlertDialog.Builder(this)
                .setTitle("Select networks")
                .setMultiChoiceItems(titles,
                                     checkedItems,
                                     (dialog, which, isChecked) -> {
                                         OptionalNetwork network = optionalNetworks[which];
                                         if (isChecked) {
                                             checkedOptionalNetworks.add(network);
                                         } else {
                                             checkedOptionalNetworks.remove(network);
                                         }
                                     })
                .setPositiveButton("Done", null)
                .show();
    }

}