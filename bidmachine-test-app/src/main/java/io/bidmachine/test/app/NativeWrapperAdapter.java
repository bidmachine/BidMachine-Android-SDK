package io.bidmachine.test.app;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;

import io.bidmachine.nativead.NativeAd;
import io.bidmachine.nativead.view.NativeMediaView;

import static androidx.recyclerview.widget.RecyclerView.AdapterDataObserver;
import static androidx.recyclerview.widget.RecyclerView.ViewHolder;

/**
 * Wrapper adapter that used to show Native Ad in recycler view with fixed step.
 */
public class NativeWrapperAdapter extends RecyclerView.Adapter<ViewHolder> {

    private static final int VIEW_HOLDER_NATIVE_NEWS_FEED = 100;
    private static final int VIEW_HOLDER_NATIVE_CONTENT_STREAM = 110;

    private RecyclerView.Adapter<ViewHolder> userAdapter;
    private int nativeStep;

    private final SparseArray<NativeAd> nativeAdList = new SparseArray<>();

    /**
     * @param userAdapter User adapter.
     * @param nativeStep  Show step {@link NativeAd}.
     */
    NativeWrapperAdapter(RecyclerView.Adapter<ViewHolder> userAdapter, int nativeStep) {
        this.userAdapter = userAdapter;
        this.nativeStep = nativeStep + 1;
        userAdapter.registerAdapterDataObserver(new AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                NativeWrapperAdapter.this.notifyDataSetChanged();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                NativeWrapperAdapter.this.notifyItemRangeInserted(positionStart, itemCount);
                showNativeAds();
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_HOLDER_NATIVE_NEWS_FEED) {
            View view = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_native_news_feed, parent, false);
            return new NativeNewsFeedViewHolder(view);
        } else if (viewType == VIEW_HOLDER_NATIVE_CONTENT_STREAM) {
            View view = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_native_content_stream, parent, false);
            return new NativeContentStreamHolder(view);
        } else {
            return userAdapter.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (holder instanceof NativeAdViewHolder) {
            ((NativeAdViewHolder) holder).register(nativeAdList.get(position));
        } else {
            userAdapter.onBindViewHolder(holder, getPositionInUserAdapter(position));
        }
    }

    @Override
    public int getItemCount() {
        int resultCount = 0;
        resultCount += getShownNativeAdsCount();
        resultCount += getPublisherItemCount();
        return resultCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (isNativeAdPosition(position)) {
            if (nativeAdList.indexOfKey(position) % 2 == 0) {
                return VIEW_HOLDER_NATIVE_NEWS_FEED;
            } else {
                return VIEW_HOLDER_NATIVE_CONTENT_STREAM;
            }
        } else {
            return userAdapter.getItemViewType(getPositionInUserAdapter(position));
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof NativeAdViewHolder) {
            ((NativeAdViewHolder) holder).unregister();
        }
    }

    void addNativeAd(NativeAd nativeAd) {
        int insertPosition = findNextAdPosition();
        if (nativeAdList.get(insertPosition) == null) {
            nativeAdList.put(insertPosition, nativeAd);
            notifyItemInserted(insertPosition);
        }
    }

    /**
     * Destroys all used native ads.
     */
    void destroyNativeAds() {
        for (int i = 0; i < nativeAdList.size(); i++) {
            NativeAd nativeAd = nativeAdList.valueAt(i);
            nativeAd.destroy();
        }
        nativeAdList.clear();
    }

    private void showNativeAds() {
        int insertPosition = findNextAdPosition();
        NativeAd nativeAd = nativeAdList.get(insertPosition);
        if (nativeAd != null) {
            notifyItemInserted(insertPosition);
        }
    }

    private int getShownNativeAdsCount() {
        int countOfShownNative = getPublisherItemCount() / nativeStep;
        return Math.min(nativeAdList.size(), countOfShownNative);
    }

    /**
     * @return User items count.
     */
    private int getPublisherItemCount() {
        if (userAdapter != null) {
            return userAdapter.getItemCount();
        }
        return 0;
    }

    /**
     * @param position Index in wrapper adapter.
     * @return {@code true} if item's position is {@link io.bidmachine.nativead.NativeAd}.
     */
    private boolean isNativeAdPosition(int position) {
        return nativeAdList.get(position) != null;
    }

    /**
     * Method to search position in user adapter.
     *
     * @param position Index in wrapper adapter.
     * @return Index in user adapter.
     */
    private int getPositionInUserAdapter(int position) {
        int countOfShownNative = position / nativeStep;
        return position - Math.min(nativeAdList.size(), countOfShownNative);
    }

    /**
     * Method to find next suitable position for {@link io.bidmachine.nativead.NativeAd}.
     *
     * @return Position for next native ad view.
     */
    private int findNextAdPosition() {
        if (nativeAdList.size() > 0) {
            return nativeAdList.keyAt(nativeAdList.size() - 1) + nativeStep;
        }
        return nativeStep - 1;
    }

    static class NativeNewsFeedViewHolder extends NativeAdViewHolder {

        private View iconView;
        private TextView titleView;
        private TextView descriptionView;
        private TextView callToActionView;

        NativeNewsFeedViewHolder(View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.icon_view);
            titleView = itemView.findViewById(R.id.title_view);
            descriptionView = itemView.findViewById(R.id.description_view);
            callToActionView = itemView.findViewById(R.id.call_to_action_view);
        }

        @Override
        void fillNative(NativeAd nativeAd) {
            titleView.setText(nativeAd.getTitle());
            descriptionView.setText(nativeAd.getDescription());
            callToActionView.setText(nativeAd.getCallToAction());
            nativeAd.registerView((ViewGroup) itemView,
                                  iconView,
                                  null,
                                  new HashSet<View>() {{
                                      add(callToActionView);
                                  }});
        }

    }

    static class NativeContentStreamHolder extends NativeAdViewHolder {

        private View iconView;
        private TextView titleView;
        private NativeMediaView nativeMediaView;
        private TextView callToActionView;

        NativeContentStreamHolder(View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.icon_view);
            titleView = itemView.findViewById(R.id.title_view);
            nativeMediaView = itemView.findViewById(R.id.native_media_view);
            callToActionView = itemView.findViewById(R.id.call_to_action_view);
        }

        @Override
        void fillNative(NativeAd nativeAd) {
            titleView.setText(nativeAd.getTitle());
            callToActionView.setText(nativeAd.getCallToAction());
            nativeAd.registerView((ViewGroup) itemView,
                                  iconView,
                                  nativeMediaView,
                                  new HashSet<View>() {{
                                      add(iconView);
                                      add(callToActionView);
                                  }});
        }

    }

    /**
     * Abstract view holders to create NativeAdView.
     */
    abstract static class NativeAdViewHolder extends ViewHolder {

        private NativeAd nativeAd;

        NativeAdViewHolder(View itemView) {
            super(itemView);
        }

        void register(NativeAd nativeAd) {
            this.nativeAd = nativeAd;
            fillNative(nativeAd);
        }

        abstract void fillNative(NativeAd nativeAd);

        void unregister() {
            nativeAd.unregisterView();
        }

    }

}