package io.bidmachine.nativead.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

class NativeAdContainer extends FrameLayout {

    FrameLayout innerContainer;

    public NativeAdContainer(Context context) {
        super(context);
        init(context);
    }

    public NativeAdContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NativeAdContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NativeAdContainer(Context context,
                             AttributeSet attrs,
                             int defStyleAttr,
                             int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        innerContainer = new FrameLayout(context);
        addView(innerContainer,
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    public void addView(View child) {
        if (child != innerContainer) {
            innerContainer.addView(child);
        } else {
            super.addView(child);
        }
    }

    @Override
    public void addView(View child, int index) {
        if (child != innerContainer) {
            innerContainer.addView(child, index);
        } else {
            super.addView(child, index);
        }
    }

    @Override
    public void addView(View child, int width, int height) {
        if (child != innerContainer) {
            innerContainer.addView(child, width, height);
        } else {
            super.addView(child, width, height);
        }
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (child != innerContainer) {
            innerContainer.addView(child, params);
        } else {
            super.addView(child, params);
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child != innerContainer) {
            innerContainer.addView(child, index, params);
        } else {
            super.addView(child, index, params);
        }
    }

    @Override
    public void removeView(View child) {
        if (innerContainer != null) {
            innerContainer.removeView(child);
        }
    }

    @Override
    public void bringChildToFront(View child) {
        if (innerContainer != null) {
            innerContainer.bringChildToFront(child);
        }
    }

    @Override
    public void removeAllViews() {
        if (innerContainer != null) {
            innerContainer.removeAllViews();
        }
    }

}