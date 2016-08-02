package com.appmobileos.android.utils;

import android.os.Build;

public class DeviceInfoUtils {

    public static String getDeviceName(){
        return Build.DEVICE.concat("_").concat(Build.MODEL);
    }
}
