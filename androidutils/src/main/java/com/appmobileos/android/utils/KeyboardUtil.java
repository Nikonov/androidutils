package com.appmobileos.android.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Andrey Nikonov on 24.04.16.
 */
public class KeyboardUtil {
    /**
     * Close software keyboard
     */
    public static void closeSoftwareKeyboard(View focusView, Context context) {
        if (focusView != null && context != null) {
            Context appCtx = context.getApplicationContext();
            InputMethodManager inputManager = (InputMethodManager) appCtx.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
        }

    }

    /**
     * Open software keyboard
     */
    public static void openSoftwareKeyboard(View focusView, Context context) {
        if (focusView != null && context != null) {
            Context appCtx = context.getApplicationContext();
            focusView.requestFocus();
            InputMethodManager inputManager = (InputMethodManager) appCtx.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(focusView, 0);
        }
    }

}
