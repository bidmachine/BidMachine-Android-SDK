package io.bidmachine.nativead.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Used in native ads to show video content.
 */
public class NativeMediaView extends RelativeLayout {

    private static final float ASPECT_MULTIPLIER_WIDTH_TO_HEIGHT = 9f / 16;
    private static final float ASPECT_MULTIPLIER_HEIGHT_TO_WIDTH = 16f / 9;

    public NativeMediaView(Context context) {
        super(context);
    }

    public NativeMediaView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NativeMediaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public NativeMediaView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        final int measWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int measHeight = MeasureSpec.getSize(heightMeasureSpec);

        final int curWidth = getMeasuredWidth();
        final int curHeight = getMeasuredHeight();

        int finalWidth;
        if (widthMode == MeasureSpec.EXACTLY) {
            finalWidth = measWidth;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            // Cap width at max width.
            finalWidth = Math.min(measWidth, curWidth);
        } else {
            // MeasWidth is meaningless. Stay with current width.
            finalWidth = curWidth;
        }
        // Set height based on width + height constraints.
        int finalHeight = (int) (ASPECT_MULTIPLIER_WIDTH_TO_HEIGHT * finalWidth);
        // Check if the layout is giving us bounds smaller than we want, conform to those if needed.
        if (heightMode == MeasureSpec.EXACTLY && measHeight < finalHeight) {
            finalHeight = measHeight;
            finalWidth = (int) (ASPECT_MULTIPLIER_HEIGHT_TO_WIDTH * finalHeight);
        }
        if (Math.abs(finalHeight - curHeight) >= 2
                || Math.abs(finalWidth - curWidth) >= 2) {
            getLayoutParams().width = finalWidth;
            getLayoutParams().height = finalHeight;
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}