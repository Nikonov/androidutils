package com.appmobileos.android.utils.image;

import android.graphics.BitmapFactory;
import android.util.Log;

import com.appmobileos.android.utils.BuildConfig;

/**
 * Created by anikonov on 4/10/14.
 */
public class ImageResize {
    private static final String TAG = "ImageResize";

    /**
     * This is method based on the exercise "Loading Large Bitmaps Efficiently".
     * <a href="http://developer.android.com/training/displaying-bitmaps/load-bitmap.html#load-bitmap">More information</a>
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int realHeight = options.outHeight;
        final int realWidth = options.outWidth;
        int inSampleSize = 1;
        if (realHeight > reqHeight || realHeight > reqWidth) {
            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) realHeight / (float) reqHeight);
            final int widthRatio = Math.round((float) realWidth / (float) reqWidth);
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "REAL SIZE = " + realWidth + "x" + realHeight +
                        " smaller version = " + reqWidth + "x" + reqHeight +
                        " RATIO height = " + heightRatio + " RADIO width = " + widthRatio);
            }
            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }
}
