package com.appmobileos.android.utils.preference;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextThemeWrapper;


import com.appmobileos.android.utils.BuildConfig;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Created by anikonov on 2/27/14.
 */
public class SettingUtil {
    private static final String TAG = "SettingsUtil";

    public static Object findObjectFromSettings(SharedPreferences preferences, String key_preference, Class type) {
        if (preferences == null) return null;
        Object object = null;
        String userString = preferences.getString(key_preference, null);
        if (userString != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                object = mapper.readValue(userString, type);
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, " OBJECT FORM SETTINGS = " + object.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

}
