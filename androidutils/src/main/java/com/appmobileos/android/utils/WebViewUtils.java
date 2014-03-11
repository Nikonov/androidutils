package com.appmobileos.android.utils;

import android.util.Log;

/**
 * Created by a.nikonov on 29.10.13.
 */
public class WebViewUtils {
    private static final String TAG = "WebViewUtils";
    /**
     * This is function helper which help create correct string for execute in WebView
     *
     * @param javaScriptFunctionName this is name function which have in JavaScript code
     * @param sendJsonFile           if true first argument must be JSON. Support send only one JSON file and many string arguments.
     * @param argumentFunction       there are arguments which will be add in JavaScript function.
     * @return string type which can be execute in WebView
     */
    public static String createJsExecuteString(String javaScriptFunctionName, boolean sendJsonFile, String... argumentFunction) {
        String mJsRequest = "javascript:" + javaScriptFunctionName + (sendJsonFile ? "('" : "(\"");
        StringBuilder mArguments = new StringBuilder();
        String mFinalRequest;
        if (argumentFunction!=null && argumentFunction.length > 0) {
            for (int i = 0; i < argumentFunction.length; i++) {
                if (i == argumentFunction.length - 1) {
                    String endBlock = "\")";
                    if (sendJsonFile) {
                        if (sendJsonFile && !(argumentFunction.length > 1)) {
                            endBlock = "')";
                        }
                    }
                    mArguments.append(argumentFunction[i]).append(endBlock);
                } else {
                    mArguments.append(argumentFunction[i]).append((sendJsonFile && i == 0 ? "',\"" : "\",\""));
                }
            }
            mFinalRequest = mJsRequest + mArguments.toString();
        } else {
            mFinalRequest = mJsRequest + (sendJsonFile ? "')" : "\")");
        }
        Log.i(TAG, " What create createJsExecuteString = " + mFinalRequest);
        return mFinalRequest;
    }
}
