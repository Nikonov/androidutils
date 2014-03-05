package com.appmobileos.android.utils.time;

import android.provider.ContactsContract;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by anikonov on 2/20/14.
 */
public class Times {

    public static final String[] formats = new String[]{
            "yyyy-MM-dd",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss.SSSZ",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
    };

    public static String getDataInFormat(String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(System.currentTimeMillis());
    }
}
