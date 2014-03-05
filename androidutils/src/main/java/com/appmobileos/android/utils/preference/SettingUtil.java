package com.appmobileos.android.utils.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;



import java.io.IOException;

/**
 * Created by anikonov on 2/27/14.
 */
public class SettingUtil {
    private static final String TAG = "SettingsUtil";
/*
    public static String madeUrlWithIpFromSettings(Context context, String urlWithoutIp) {
        if (context == null) return null;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String currentIp = preferences.getString(context.getString(R.string.key_preference_ip), context.getString(R.string.preference_default_ip));
        return "http://" + currentIp + urlWithoutIp;
    }*/

/*
    public static IMerchandiser findCurrentMerchandiser(Context context) {
        if (context == null) return null;
        IMerchandiser user = null;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String userString = preferences.getString(context.getString(R.string.key_preference_user), null);
        if (userString != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                user = mapper.readValue(userString, Merchandiser.class);
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, " USER FORM SETTINGS = " + user.getId());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return user;
    }*/

/*    public static IMarket findCurrentMarket(Context context) {
        if (context == null) return null;
        IMarket market = null;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String userString = preferences.getString(context.getString(R.string.key_preference_market), null);
        if (userString != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                market = mapper.readValue(userString, Market.class);
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, " MARKET FORM SETTINGS = " + market.getId());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return market;
    }*/
}
