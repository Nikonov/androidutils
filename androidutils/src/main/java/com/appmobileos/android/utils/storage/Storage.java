package com.appmobileos.android.utils.storage;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by anikonov on 2/20/14.
 */
public class Storage {
    private static boolean mExternalStorageAvailable = false;
    private static boolean mExternalStorageWriteable = false;

    /**
     * Initialize storage before using
     */
    public static void init() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
    }

    /**
     * @param appContext app context
     * @return File corresponding to application's external storage directory
     */
    public static File getExternalStorageFile(Context appContext) {
        if (mExternalStorageWriteable)
            return appContext.getExternalFilesDir(null);
        else
            return getInternalStorageFile(appContext);
    }

    /**
     * @param appContext app context
     * @return path or null if external storage is not available
     */
    public static String getExternalStoragePath(Context appContext) {
        if (mExternalStorageWriteable)
            return appContext.getExternalFilesDir(null).getAbsolutePath();
        else
            return getInternalStoragePath(appContext);
    }

    /**
     * @param appContext app context
     * @return File corresponding to application's internal storage directory
     */
    public static File getInternalStorageFile(Context appContext) {
        return appContext.getFilesDir();
    }

    /**
     * @param appContext
     * @return
     */
    public static String getInternalStoragePath(Context appContext) {
        return appContext.getFilesDir().getAbsolutePath();
    }

    /**
     * @return return true if external storage is available
     */
    public static boolean isExternalStorageAvailable() {
        return mExternalStorageAvailable;
    }

    /**
     * @return return true if external storage is available for writing
     */
    public static boolean isExternalStorageWriteable() {
        return mExternalStorageWriteable;
    }

    public static File getHomeDir(Context context) {
        init();
        String tempPath = null;
        File root;
        if (mExternalStorageWriteable) {
            File homeExternal = context.getExternalFilesDir(null);
            if (homeExternal != null) {
                tempPath = homeExternal.getAbsolutePath();
            }
        } else {
            File homeApp = context.getFilesDir();
            if (homeApp != null) {
                tempPath = homeApp.getAbsolutePath();
            }
        }
        root = tempPath == null ? context.getFilesDir() : new File(tempPath);
        if (!root.exists()) root.mkdirs();
        return root;
    }



}
