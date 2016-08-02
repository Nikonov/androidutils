package com.appmobileos.android.utils;

import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Andrey Nikonov on 19.07.16.
 * Craslytics <a href="https://try.crashlytics.com/">Craslytics</>
 */
public class CrashlyticsUtil {
    /**
     * Send exception with craslytics, works only if crashlytica initialized
     *
     * @param e will sent
     */
    public static void sendException(@Nullable Throwable e) {
        if (Fabric.isInitialized() && Crashlytics.getInstance() != null) {
            if (e != null) {
                Crashlytics.logException(e);
            } else {
                Crashlytics.log(" Can't send exception because throwable == null ");
            }
        }
    }
}
