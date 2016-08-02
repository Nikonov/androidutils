package com.appmobileos.android.utils;

import android.annotation.SuppressLint;
import android.view.View;

public class ImmersiveModeUtils {
    /**
     * Hide nav bar and status bar
     */
    @SuppressLint("InlinedApi")
    public static void enableImmersiveMode(View decorView) {
        if (decorView == null) return;
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    /**
     * Show nav bar and status bar
     */
    @SuppressLint("InlinedApi")
    public static void disableImmersiveMode(View decorView) {
        if (decorView == null) return;
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}
