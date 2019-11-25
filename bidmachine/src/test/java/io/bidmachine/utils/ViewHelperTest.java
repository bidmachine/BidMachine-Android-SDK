package io.bidmachine.utils;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ViewHelperTest {

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
    }

    @Test
    public void belongTo_positive1() {
        FrameLayout container = new FrameLayout(context);
        View view = new View(context);
        container.addView(view);
        boolean result = ViewHelper.belongTo(container, view);
        assertTrue(result);
    }

    @Test
    public void belongTo_positive2() {
        FrameLayout container = new FrameLayout(context);
        FrameLayout viewContainer = new FrameLayout(context);
        View view = new View(context);
        viewContainer.addView(view);
        container.addView(viewContainer);
        boolean result = ViewHelper.belongTo(container, view);
        assertTrue(result);
    }

    @Test
    public void belongTo_negative1() {
        FrameLayout container = new FrameLayout(context);
        View view = new View(context);
        boolean result = ViewHelper.belongTo(container, view);
        assertFalse(result);
    }

    @Test
    public void belongTo_negative2() {
        FrameLayout container = new FrameLayout(context);
        FrameLayout viewContainer = new FrameLayout(context);
        View view = new View(context);
        viewContainer.addView(view);
        boolean result = ViewHelper.belongTo(container, view);
        assertFalse(result);
    }

}