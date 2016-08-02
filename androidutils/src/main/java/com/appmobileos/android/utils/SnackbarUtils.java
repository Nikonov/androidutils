package com.appmobileos.android.utils;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.spbtv.tele2.R;

import java.util.Locale;

public class SnackbarUtils {

    public static void showEmailSnackbar(@NonNull View view, @NonNull CharSequence text,
                                         @Snackbar.Duration int duration) {
        getCommonSnackbar(view, text, duration).show();
    }

    public static void showReEntrySnackbar(@NonNull View view) {
        getCommonSnackbar(view, view.getContext().getString(R.string.auth_re_entry_message), Snackbar.LENGTH_SHORT).show();
    }

    public static void showGeneralErrorSnackbar(@NonNull View view) {
        getCommonSnackbar(view, view.getContext().getString(R.string.general_error_curtain), Snackbar.LENGTH_SHORT).show();
    }

    public static void showIncorrectSmsCodeSnackbar(@NonNull View view) {
        getCommonSnackbar(view, view.getContext().getString(R.string.auth_incorrect_sms_code_message), Snackbar.LENGTH_SHORT).show();
    }
    public static void showTooManySmsCodeSnackbar(@NonNull View view) {
        getCommonSnackbar(view, view.getContext().getString(R.string.auth_too_many_sms_code_message), Snackbar.LENGTH_SHORT).show();
    }
    public static void showSuspendChannelSnackbar(@NonNull View view) {
        getCommonSnackbar(view, view.getContext().getString(R.string.error_suspend_channel), Snackbar.LENGTH_SHORT).show();
    }

    public static void showSnackbarWithText(@NonNull View view, String text) {
        getCommonSnackbar(view, text, Snackbar.LENGTH_SHORT).show();
    }

    public static void showServiceSuspendSnackbar(@NonNull View view, String title) {
        getCommonSnackbar(view, String.format(Locale.getDefault(),
                view.getContext().getString(R.string.service_suspend_info_format), title), Snackbar.LENGTH_SHORT).show();
    }

    public static void showVideoSuspendStatusSnackbar(@NonNull View view) {
        getCommonSnackbar(view, view.getContext().getString(R.string.service_suspend_info), Snackbar.LENGTH_SHORT).show();
    }

    private static Snackbar getCommonSnackbar(@NonNull View view, @NonNull CharSequence text, @Snackbar.Duration int duration) {
        Snackbar snackbar = Snackbar.make(view, text, duration);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorWhite));
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        textView.setSingleLine(false);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        return snackbar;
    }

    public static void showServiceDisableSnackbar(@NonNull View view, @NonNull CharSequence text) {
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorWhite));
        snackbar.show();
    }

}
