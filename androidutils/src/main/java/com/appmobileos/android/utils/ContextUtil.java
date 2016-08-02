package com.appmobileos.android.utils;

import android.content.Context;

import static com.client.stb.api.utils.BradburyLogger.*;

/**
 * Created by Andrey Nikonov on 15.05.15.
 */
public class ContextUtil {
    private static final String LOG_DEBUG = makeLogTag(ContextUtil.class);

    public static Context getApplicationContext(Context context) {
        return context == null ? null : context.getApplicationContext();
    }

    public static Context getApplicationContextOrThrow(Context context) {
        if (context == null) throw new NullPointerException(" Context must be not null ");
        return context.getApplicationContext();
    }

    public static boolean isContextNotNull(Context context) {
        if (context == null) {
            logError(LOG_DEBUG, " Context == null");
            return false;
        }
        return true;
    }
}
