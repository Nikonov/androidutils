package com.appmobileos.android.utils;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import static com.client.stb.api.utils.BradburyLogger.*;

/**
 * Created by Andrey Nikonov on 18.01.16.
 * Helper class
 */
public class LoaderUtils {
    private static final String LOG_DEBUG = makeLogTag(LoaderUtils.class);

    /**
     * Initialization or restart loader. If {@link Loader} not null the method checks status {@link Loader#isReset()}
     * and will be call {@link LoaderManager#restartLoader(int, Bundle, LoaderManager.LoaderCallbacks)}
     * or {@link LoaderManager#initLoader(int, Bundle, LoaderManager.LoaderCallbacks)}
     *
     * @param manager  {@link LoaderManager} must be not null
     * @param loader   {@link Loader} can be null
     * @param loaderId {@link Loader#getId()} id loader
     * @param bundle   {@link Bundle} arguments can be null
     * @param callback {@link LoaderManager.LoaderCallbacks} must be not null
     * @throws NullPointerException if parameters null
     * @see #initOrRestartLoader(LoaderManager, Loader, int, LoaderManager.LoaderCallbacks)
     */
    public static <D> void initOrRestartLoader(@NonNull LoaderManager manager, Loader<D> loader, int loaderId, Bundle bundle, @NonNull LoaderManager.LoaderCallbacks<Cursor> callback) {
        if (manager == null || callback == null) {
            throw new NullPointerException("The parameters is bad. Manager: " + manager + " callback: " + callback);
        }
        logDebug(LOG_DEBUG, " loader: " + (loader == null ? "null" : "id: " + loader.getId() + " reset: " + loader.isReset()));
        if (loader != null && !loader.isReset()) {
            manager.restartLoader(loaderId, bundle, callback);
        } else {
            manager.initLoader(loaderId, bundle, callback);
        }
    }

    /**
     * The same {@link #initOrRestartLoader(LoaderManager, Loader, int, Bundle, LoaderManager.LoaderCallbacks)} but bundle parameter null.
     *
     * @see #initOrRestartLoader(LoaderManager, Loader, int, Bundle, LoaderManager.LoaderCallbacks)
     */
    public static <D> void initOrRestartLoader(@NonNull LoaderManager manager, Loader<D> loader, int loaderId, @NonNull LoaderManager.LoaderCallbacks<Cursor> callback) {
        initOrRestartLoader(manager, loader, loaderId, null, callback);
    }
}
