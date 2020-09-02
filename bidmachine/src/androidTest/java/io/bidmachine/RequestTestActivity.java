package io.bidmachine;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

public class RequestTestActivity extends Activity {

    public FrameLayout parentFrame;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentFrame = new FrameLayout(this);
        setContentView(parentFrame);
    }

}
