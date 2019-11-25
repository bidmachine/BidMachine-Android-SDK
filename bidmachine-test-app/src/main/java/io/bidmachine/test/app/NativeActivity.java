package io.bidmachine.test.app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.bidmachine.MediaAssetType;
import io.bidmachine.nativead.NativeAd;
import io.bidmachine.nativead.NativeListener;
import io.bidmachine.nativead.NativeRequest;
import io.bidmachine.utils.BMError;

public class NativeActivity extends AppCompatActivity implements NativeListener {

    private static final int DEFAULT_PACK_SIZE = 10;
    private static final int DEFAULT_NATIVE_COUNT = 4;

    private RecyclerView rvNative;
    private NativeAdapter nativeAdapter;
    private NativeWrapperAdapter nativeWrapperAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native);

        SwipeRefreshLayout srlUpdateNative = findViewById(R.id.srl_update_native);
        srlUpdateNative.setOnRefreshListener(() -> {
            if (nativeAdapter != null) {
                if (nativeWrapperAdapter != null) {
                    nativeWrapperAdapter.destroyNativeAds();
                }
                nativeAdapter.clearContent();
                addPackToAdapter();
                loadNativeAds();
            }
            srlUpdateNative.setRefreshing(false);
        });

        nativeAdapter = new NativeAdapter();
        nativeWrapperAdapter = new NativeWrapperAdapter(nativeAdapter, 9);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setSmoothScrollbarEnabled(true);

        rvNative = findViewById(R.id.rv_native);
        rvNative.setAdapter(nativeWrapperAdapter);
        rvNative.setItemAnimator(new DefaultItemAnimator());
        rvNative.setLayoutManager(linearLayoutManager);
        rvNative.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    int visibleItemCount = linearLayoutManager.getChildCount();
                    int totalItemCount = linearLayoutManager.getItemCount();
                    int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
                        addPackToAdapter();
                    }
                }
            }
        });
        addPackToAdapter();
        loadNativeAds();
    }

    private void loadNativeAds() {
        int i = 0;
        while (i < DEFAULT_NATIVE_COUNT) {
            loadNative();
            i++;
        }
    }

    private void addPackToAdapter() {
        List<String> publisherList = new ArrayList<>();
        int lastValue = nativeAdapter.getItemCount();
        for (int i = 1; i <= DEFAULT_PACK_SIZE; i++) {
            publisherList.add(String.valueOf(i + lastValue));
        }
        rvNative.post(() -> nativeAdapter.addInfo(publisherList));
    }

    private void loadNative() {
        NativeRequest nativeRequest = new NativeRequest.Builder()
                .setMediaAssetTypes(MediaAssetType.All)
                .build();
        NativeAd nativeAd = new NativeAd(this);
        nativeAd.setListener(this);
        nativeAd.load(nativeRequest);
    }

    @Override
    public void onAdLoaded(@NonNull NativeAd ad) {
        rvNative.post(() -> nativeWrapperAdapter.addNativeAd(ad));
        Utils.showToast(this, "NativeAd onAdLoaded");
    }

    @Override
    public void onAdLoadFailed(@NonNull NativeAd ad, @NonNull BMError error) {
        Utils.showToast(this, "NativeAd onAdLoadFailed");
    }

    @Override
    public void onAdShown(@NonNull NativeAd ad) {
        Utils.showToast(this, "NativeAd onAdShown");
    }

    @Override
    public void onAdImpression(@NonNull NativeAd ad) {
        Utils.showToast(this, "NativeAd onAdImpression");
    }

    @Override
    public void onAdClicked(@NonNull NativeAd ad) {
        Utils.showToast(this, "NativeAd onAdClicked");
    }

    @Override
    public void onAdExpired(@NonNull NativeAd ad) {
        Utils.showToast(this, "NativeAd onAdExpired");
    }

}