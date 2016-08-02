package com.appmobileos.android.utils;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;

import static com.client.stb.api.utils.BradburyLogger.*;

import com.client.chakratab.fragments.PaddleFragmentBase;
import com.client.stb.api.model.Item;
import com.client.stb.api.model.VideoItem;

/**
 * @author: v.egorov
 * @date: 03.07.2014
 */


public class UIUtils {
    private static final String TAG_DEBUG = makeLogTag(UIUtils.class);
    /**
     * The {@link android.support.v4.view.ViewPager} all items hold in main thread.
     * Big count items will be block main ui.
     * <a href="http://stackoverflow.com/questions/18740916/viewpager-setcurrentitem-freezes-ui-thread">Problem ViewPager</a>
     **/
    public static final int SCROLL_FORWARD_ITEMS = 10;

    public static final int DEFAUTL_COLOR_PALLETE = Color.WHITE;

    public static void updateStatusBarVisible(Window window, boolean show) {
        if (show) {
            if (Build.VERSION.SDK_INT < 16) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        } else {
            if (Build.VERSION.SDK_INT < 16) {
                window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                //  window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
        }

    }

    public static void setDeepClipChildren(ViewGroup root, boolean clip) {
        root.setClipChildren(clip);
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                setDeepClipChildren((ViewGroup) child, clip);
            }

        }
    }


    public static Rect foundVisibleRect(Activity activity) {
        if (activity == null) return new Rect();
        Rect visibleRect = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(visibleRect);
        return visibleRect;
    }


    public static boolean isBeyondScreenListView(ListView listView, Rect visibleRect) {
        if (listView == null || visibleRect == null) return false;
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null || listView.getCount() == 0) return false;
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        logDebug(TAG_DEBUG, "Visible rect = " + visibleRect + " total height = " + totalHeight);
        return totalHeight > visibleRect.height();
    }

    public static boolean isPaddleFragment(Fragment fragment) {
        if (fragment == null) return false;
        return fragment instanceof PaddleFragmentBase;
    }

    public static boolean isDarkColor(String textColor) {
        if (TextUtils.isEmpty(textColor)) return false;
        int color = Color.parseColor("#" + textColor);
        float brightness = brightness(color);
        return brightness < 0.2;
    }

    /**
     * Returns the brightness component of a color int.
     *
     * @return A value between 0.0f and 1.0f
     * @hide Pending API council
     */
    public static float brightness(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (float) (0.299 * r + 0.587 * g + 0.114 * b) / 1000;
    }


    /**
     * Returns color. This method check correct parameter pallete in {@link VideoItem}
     *
     * @return color from object or white color if had problem
     */
    public static int getPalleteColor(VideoItem item) {
        return getPalleteColor(item == null ? null : item.getPallete());
    }

    /**
     * @see {@link #getPalleteColor(VideoItem)}
     **/
    public static int getPalleteColor(String color) {
        if (color == null || TextUtils.isEmpty(color)) return DEFAUTL_COLOR_PALLETE;
        return Color.parseColor("#" + color);
    }


    /**
     * Scroll list forward at {@link #SCROLL_FORWARD_ITEMS} positions.
     * Need use with {@link #getEndlessCount(int)}
     *
     * @param itemsCount   size data
     * @param realPosition start position
     */
    public static int scrollForwardPosition(int itemsCount, int realPosition) {
        return ((itemsCount * SCROLL_FORWARD_ITEMS) / 2) + realPosition;
    }

    /**
     * This is not real endless count. This is method increase real count in {@link #SCROLL_FORWARD_ITEMS}
     * It need use only with method {@link #scrollForwardPosition(int, int)}
     *
     * @param itemsCount size data
     */
    public static int getEndlessCount(int itemsCount) {
        return itemsCount * SCROLL_FORWARD_ITEMS;
    }

    /**
     * This is method create prefix episode by Bradbury design pattern
     *
     * @param episode
     * @return string by pattern "S_seasonE_episodeNumber: " or empty string
     */
    public static String createPrefixEpisodeTitle(VideoItem episode) {
        if (episode == null) return "";
        if (!episode.getType().equals(Item.EPISODE_TYPE)) {
            logWarning(TAG_DEBUG, " Incorrect type. Support only episode, but now " + episode.getType());
            return "";
        }
        String season = Integer.toString(episode.getSeason());
        if (season.length() == 1) season = "0" + season;
        String episodeText = episode.getSeriesNumber() == null || episode.getSeriesNumber().size() == 0 ? "*" : Integer.toString(episode.getSeriesNumber().get(0));
        if (episodeText.length() == 1) episodeText = "0" + episodeText;
        if (season.length() == 1) season = "0" + season;
        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder
                .append("S")
                .append(season)
                .append("E")
                .append(episodeText)
                .append(": ");
        return titleBuilder.toString();
    }
}
