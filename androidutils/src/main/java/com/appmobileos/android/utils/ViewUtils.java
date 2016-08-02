package com.appmobileos.android.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;

import com.spbtv.tele2.R;
import com.spbtv.tele2.models.app.Indent;

import java.util.ArrayList;
import java.util.List;

import static com.spbtv.tele2.util.BradburyLogger.logDebug;
import static com.spbtv.tele2.util.BradburyLogger.makeLogTag;
import static com.spbtv.tele2.util.CollectionUtil.isCollectionNullOrEmpty;

/**
 * Created by Ayder on 07.04.2016.
 */
public class ViewUtils {
    public static final int VIEW_POSITION_NOT_FOUNT = -1;
    private static final String LOG_DEBUG = makeLogTag(ViewUtils.class);

    public static void removeOnGlobalLayoutCompat(View v, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (v == null || listener == null) return;
        ViewTreeObserver observer = v.getViewTreeObserver();
        boolean isAlive = observer.isAlive();
        logDebug(LOG_DEBUG, " isAlive: " + isAlive);
        if (isAlive) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                observer.removeOnGlobalLayoutListener(listener);
            } else {
                observer.removeGlobalOnLayoutListener(listener);
            }
        }
    }

    public static String convertDurationToMinutes(String duration) {
        StringBuilder result = new StringBuilder();
        String[] parts = duration.split(":");
        int partsCount = parts.length;
        if (partsCount == 2) {
            result.append(parts[0]).append(" мин.");
        } else if (partsCount == 3) {
            result.append(Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1])).append(" мин.");
        } else {
            result.append(duration);
        }
        return result.toString();
    }

    public static int getDisplayWidthPx(WindowManager windowManager) {
        if (windowManager == null) return -1;
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static int getDisplayHeightPx(WindowManager windowManager) {
        if (windowManager == null) return -1;
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public static String getDesignPhoneFormat(String phone) {
        if (TextUtils.isEmpty(phone) && phone.length() < 9) return "";
        return "+" + phone.substring(0, 1) + " " + phone.substring(1, 4) + " "
                + phone.substring(4, 7) + " " + phone.substring(7, 9) + " " + phone.substring(9);
    }

    public static int calculateHeightAspect4_3(WindowManager manager) {
        final int width = getDisplayWidthPx(manager);
        return (int) Math.round(width / 1.33);
    }

    public static int calculateTopMarginLiveSurface(WindowManager manager) {
        final int height = getDisplayHeightPx(manager);
        //30% from height display
        return (int) Math.round(height * 0.3);
    }

    public static int foundPositionView(ViewGroup viewGroup, @IdRes int idView) {
        if (viewGroup == null || idView <= 0) return -1;
        int countChild = viewGroup.getChildCount();
        for (int i = 0; i < countChild; i++) {
            View v = viewGroup.getChildAt(i);
            if (v.getId() == idView) return i;
        }
        return VIEW_POSITION_NOT_FOUNT;
    }

    /**
     * Gets orientation from resources {@link Resources#getConfiguration()}
     */
    public static int orientation(@NonNull Resources resources) {
        return resources.getConfiguration().orientation;
    }

    /**
     * Gets orientation from resources {@link Resources#getConfiguration()}
     */
    public static boolean orientationPortrait(@NonNull Resources resources) {
        return resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * Gets orientation from resources {@link Resources#getConfiguration()}
     */
    public static boolean orientationLandscape(@NonNull Resources resources) {
        return resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * Format number phone by bradbury pattern
     *
     * @deprecated use {@link com.spbtv.tele2.view.format.MaskedEditText}
     */
    @Deprecated
    public static String getFormattedPhoneText(String phonePrefix, String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(phonePrefix)) {
            return "";
        }
        phoneNumber = phoneNumber.substring(phonePrefix.length(), phoneNumber.length()).replaceAll(" ", "");
        int len = phoneNumber.length();
        if (len <= 3) return phoneNumber;
        if (len <= 6) return phoneNumber.replaceFirst("(\\d{3})(\\d+)", "$1 $2");
        if (len <= 8) return phoneNumber.replaceFirst("(\\d{3})(\\d{3})(\\d+)", "$1 $2 $3");
        return phoneNumber.replaceFirst("(\\d{3})(\\d{3})(\\d{2})(\\d+)", "$1 $2 $3 $4");
    }

    public static int getNavigationBarHeightLandscape(Context context) {
        Resources resources = context.getResources();
        boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
        int id = resources.getIdentifier("navigation_bar_height_landscape",
                "dimen", "android");
        if (id > 0 && !hasMenuKey) {
            return resources.getDimensionPixelSize(id);
        }
        return 0;
    }

    public static String getRentText(Context context, Indent indent) {
        String dateToText = indent.getEndAt();
        if (dateToText == null) {
            return "";
        }
        return context.getString(R.string.detail_video_film_rent_text, DateUtil.convertUtcToGMT(context, dateToText));
    }

    /**
     * A method to find height of the status bar
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    public static Pair<Integer, Integer> calculateSizePoster(Context ctx) {
        return calculateSizePoster(ctx, -1);
    }

    public static Pair<Integer, Integer> calculateSizePoster(Context ctx, int allDisplay) {
        Resources res = ctx.getResources();
        TypedValue typedValue = new TypedValue();
        res.getValue(R.dimen.aspect_ration_design_poster, typedValue, true);
        float aspectRation = typedValue.getFloat();
        WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        if (allDisplay <= 0) {
            allDisplay = ViewUtils.getDisplayWidthPx(windowManager);
        }
        //how many numbers selections need to shown
        int countItems = res.getInteger(R.integer.number_vod_items_main_screen);
        // 2 * because left and right
        int edgePadding = 2 * res.getDimensionPixelSize(R.dimen.horizontal_edge_padding_block);
        //items for example 8 but space between items 7
        int betweenItemsPadding = (countItems - 1) * res.getDimensionPixelSize(R.dimen.horizontal_spacing_between_block_items);
        int spaceForPosters = allDisplay - (edgePadding + betweenItemsPadding);
        int widthOnePoster = spaceForPosters / countItems;
        int heightOnePoster = (int) (widthOnePoster / aspectRation);
        logDebug(LOG_DEBUG, "calculateSizePoster allDisplay: " + allDisplay);
        return new Pair<>(widthOnePoster, heightOnePoster);
    }

    public static Pair<Integer, Integer> calculateSizeSelection(Context ctx) {
        Resources res = ctx.getResources();
        TypedValue typedValue = new TypedValue();
        res.getValue(R.dimen.aspect_ration_design_selection, typedValue, true);
        float aspectRation = typedValue.getFloat();
        WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        int allDisplay = ViewUtils.getDisplayWidthPx(windowManager);
        //how many numbers selections need to shown
        int countItems = res.getInteger(R.integer.number_selection_items_main_screen);
        // 2 * because left and right
        int edgePadding = 2 * res.getDimensionPixelSize(R.dimen.horizontal_edge_padding_block);
        //items for example 8 but space between items 7
        int betweenItemsPadding = (countItems - 1) * res.getDimensionPixelSize(R.dimen.horizontal_spacing_between_block_items);
        int spaceForPosters = allDisplay - (edgePadding + betweenItemsPadding);
        int widthOnePoster = spaceForPosters / countItems;
        int heightOnePoster = (int) (widthOnePoster / aspectRation);
        return new Pair<>(widthOnePoster, heightOnePoster);
    }


    public static Pair<Integer, Integer> calculateSizeCarouselBanner(Context ctx) {
        Resources res = ctx.getResources();
        TypedValue typedValue = new TypedValue();
        res.getValue(R.dimen.aspect_ration_design_carousel_banner, typedValue, true);
        float aspectRation = typedValue.getFloat();
        WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        // 2 * because left and right
        int padding = 2 * res.getDimensionPixelSize(R.dimen.banners_view_pager_horizontal_padding);
        int allDisplay = ViewUtils.getDisplayWidthPx(windowManager);
        int widthOnePoster = allDisplay - padding;
        int heightOnePoster = (int) (widthOnePoster / aspectRation);
        return new Pair<>(widthOnePoster, heightOnePoster);
    }


    public static boolean isHasNavigationBar(Context ctx) {
        Resources resources = ctx.getResources();
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            return resources.getBoolean(id);
        } else {
            // Check for keys
            boolean hasMenuKey = ViewConfiguration.get(ctx).hasPermanentMenuKey();
            boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            return !hasMenuKey && !hasBackKey;
        }
    }

    private int getNavigationBarHeight(Context context) {
        if (isHasNavigationBar(context)) {
            Resources resources = context.getResources();
            int id = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (id > 0) {
                return resources.getDimensionPixelSize(id);
            }
        }
        return 0;
    }

    public static <T> List<T> checkCarouselCountItems(List<T> input) {
        if (!isCollectionNullOrEmpty(input)) {
            //why 4 ? see source https://github.com/antonyt/InfiniteViewPager
            if (input.size() >= 4) return input;
            List<T> newData = new ArrayList<>();
            do {
                newData.addAll(input);
            } while (newData.size() < 4);
            return newData;
        }
        return input;
    }

    public static void changeStatusBarColor(@NonNull Window window, @ColorRes int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(window.getContext().getApplicationContext(), color));
        }
    }

    public static boolean eventInView(MotionEvent event, View targetView) {
        if (event == null || targetView == null) return false;
        final float clickedX = event.getRawX();
        final float clickedY = event.getRawY();
        int[] locationPreview = new int[2];
        targetView.getLocationOnScreen(locationPreview);
        final int locationXPreview = locationPreview[0];
        final int locationRightPreview = locationXPreview + targetView.getWidth();
        final int locationYPreview = locationPreview[1];
        final int locationBottomPreview = locationYPreview + targetView.getHeight();
        return (clickedX > locationXPreview && clickedX < locationRightPreview) && (clickedY > locationYPreview && clickedY < locationBottomPreview);
    }
}
