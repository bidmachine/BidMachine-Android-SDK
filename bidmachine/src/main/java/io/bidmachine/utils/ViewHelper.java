package io.bidmachine.utils;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

public class ViewHelper {

    public static boolean belongTo(@NonNull ViewGroup parent, @NonNull View child) {
        ViewParent viewParent = child.getParent();
        if (viewParent instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) viewParent;
            return viewGroup.equals(parent) || belongTo(parent, viewGroup);
        } else {
            return false;
        }
    }

}