package com.appmobileos.android.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @author: v.egorov
 * @date: 02.06.2014
 */

public class SystemUtils {


    @SuppressLint("InlinedApi")
    public static void hideSystemUI(Window window, View decorView) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        } else {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }

    }

    @SuppressLint("InlinedApi")
    public static void showSystemUI(Window window, View decorView) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    /**
     * Calculate running time with start point
     *
     * @param startTime startTime in nanoseconds. Use method nanoTime in System class
     * @return "runtime code was executed(mm:ss:millis): value1:value2:value3"
     */
    public static String runningTimeFinish(long startTime) {
        final long duration = (System.nanoTime() - startTime);
        return " runtime code was executed(mm:ss:millis):"
                + TimeUnit.NANOSECONDS.toMinutes(duration) + ":"
                + TimeUnit.NANOSECONDS.toSeconds(duration) + ":"
                + TimeUnit.NANOSECONDS.toMillis(duration);
    }

    /**
     * Calculate running time with start point
     *
     * @param startTime startTime in milliseconds. Use method {@link System#currentTimeMillis()}
     * @return "runtime code was executed(mm:ss:millis): value1:value2:value3" or (start time = 0) if startTime==0
     */
    public static String timeRunning(long startTime) {
        if (startTime == 0) return " startTime:0 can't count ";
        final long duration = (System.currentTimeMillis() - startTime);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss:SSS");
        return " elapsed time (mm:ss:millis):" + simpleDateFormat.format(new Date(duration));
    }

    /**
     * Write to console current time at device
     *
     * @return "runtime code was executed(mm:ss:millis): value1:value2:value3"
     */
    public static String writeConsoleCurrentTime() {
        final long nowTime = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date(nowTime));
    }
}
