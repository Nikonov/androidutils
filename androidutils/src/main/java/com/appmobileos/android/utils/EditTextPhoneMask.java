package com.appmobileos.android.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

/**
 * Created by Andrey Nikonov on 23.05.16.
 * Copy past from stack overflow <a href="http://stackoverflow.com/questions/29659764/dynamic-mask-using-textwatcher">phone format</>
 */
public class EditTextPhoneMask implements TextWatcher {
    private static final String sMaskPhone = "## ### ### ## ##"; //for example: +7 903 177 85 15
    private final Button mPointBtn;
    private final EditText mEditText;
    private final PhoneNumberUtil mPhoneUtil = PhoneNumberUtil.getInstance();
    private Phonenumber.PhoneNumber mCurrentPhoneNumber;
    private boolean isUpdating;
    private String old = "";


    public EditTextPhoneMask(Button mPointBtn, EditText mEditText) {
        this.mPointBtn = mPointBtn;
        this.mEditText = mEditText;
        mEditText.setText("+7");
    }

    public String unmask(String s) {
        if (s.contains("+7")) {
            s = s.replace("+7", "");
        }
        String onlyNumber = s.replaceAll("[^0-9]*", "");
        return "+7".concat(onlyNumber);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String str = unmask(s.toString());
        String mascara = "";
        if (isUpdating) {
            old = str;
            isUpdating = false;
            return;
        }
        int i = 0;
        for (char m : sMaskPhone.toCharArray()) {
            if ((m != '#' && str.length() > old.length()) || (m != '#' && str.length() < old.length() && str.length() != i)) {
                mascara += m;
                continue;
            }
            try {
                mascara += str.charAt(i);
            } catch (Exception e) {
                break;
            }
            i++;
        }
        isUpdating = true;
        mEditText.setText(mascara);
        mEditText.setSelection(mascara.length());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        try {
            mCurrentPhoneNumber = mPhoneUtil.parse(s.toString().replaceAll(" ", ""), "RU");
            boolean isValidNumber = mPhoneUtil.isValidNumber(mCurrentPhoneNumber);
            mPointBtn.setEnabled(isValidNumber);
        } catch (NumberParseException e) {
            mPointBtn.setEnabled(false);
        }
    }

    public String getPhone() {
        if (mCurrentPhoneNumber != null && mPhoneUtil.isValidNumber(mCurrentPhoneNumber)) {
            String international = mPhoneUtil.format(mCurrentPhoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
            international = international.replaceAll("[^0-9]+", "");
            return international;
        }
        return null;
    }

    @Override
    public void afterTextChanged(Editable s) {
        //unused
    }
}
