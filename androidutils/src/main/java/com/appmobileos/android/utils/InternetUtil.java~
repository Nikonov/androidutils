package com.appmobileos.android.utils;

import android.util.Log;

import java.io.File;
import java.util.Map;

/**
 * Created by anikonov on 4/1/14.
 */
public class InternetUtil {
    private static final String TAG = "InternetUtil";

    public static String createFileBlockMultiPartType(String boundary, File fileSend, String nameParam) {
        if (BuildConfig.DEBUG) Log.i(TAG, "FILE NAME SEND  = " + fileSend.getName());
        StringBuilder builder = new StringBuilder();
        builder.append("--" + boundary).append("\r\n")
                .append("Content-Disposition: form-data; name=\"" + nameParam + "\"; filename=\"" + fileSend.getName() + "\"").append("\r\n")
                .append("Content-Type: application/octet-stream").append("\r\n")
                .append("Content-Transfer-Encoding: binary").append("\r\n\r\n");
        return builder.toString();
    }

    public static String createTextBlockMultiPartType(String boundary, Map<String, Object> data) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> param : data.entrySet()) {
            builder.append("--" + boundary).append("\r\n")
                    .append("Content-Disposition: form-data; name=\"" + param.getKey() + "\"").append("\r\n")
                    .append("Content-Type: application/x-www-form-urlencoded").append("\r\n")
                    .append("Content-Transfer-Encoding: 8bit").append("\r\n\r\n")
                    .append(param.getValue()).append("\r\n");
        }
        return builder.toString();

    }
}
