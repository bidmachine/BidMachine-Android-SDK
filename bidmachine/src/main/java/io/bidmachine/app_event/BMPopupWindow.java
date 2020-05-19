package io.bidmachine.app_event;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;

import java.lang.reflect.Method;

import io.bidmachine.core.Utils;

class BMPopupWindow {

    private static final String TAG = "BMPopupWindow";

    private PopupWindow popUpWindow;

    void showView(@NonNull final Activity activity,
                  @NonNull final View view,
                  final int widthDp,
                  final int heightDp) {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                hide();

                float screenDensity = Utils.getScreenDensity(activity);
                popUpWindow = new PopupWindow(view,
                                              Math.round(widthDp * screenDensity),
                                              Math.round(heightDp * screenDensity));
                setPopUpWindowLayoutType(popUpWindow,
                                         WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL);
                Window window = activity.getWindow();
                View rootView = window.getDecorView().getRootView();
                popUpWindow.getContentView().setSystemUiVisibility(window.getAttributes().flags);
                popUpWindow.showAtLocation(rootView, getLayoutGravity(), 0, 0);
            }
        });
    }

    void hide() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                if (popUpWindow != null) {
                    popUpWindow.dismiss();
                    popUpWindow = null;
                }
            }
        });
    }

    private void setPopUpWindowLayoutType(@NonNull PopupWindow popupWindow, int layoutType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            popUpWindow.setWindowLayoutType(layoutType);
        } else {
            try {
                Method method = PopupWindow.class.getDeclaredMethod("setWindowLayoutType",
                                                                    int.class);
                method.setAccessible(true);
                method.invoke(popupWindow, layoutType);
            } catch (Exception exception) {
                Log.e(TAG, "Unable to set popUpWindow window layout type");
            }
        }
    }

    private int getLayoutGravity() {
        return Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
    }

}
