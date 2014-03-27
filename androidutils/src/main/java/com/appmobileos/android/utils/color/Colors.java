package com.appmobileos.android.utils.color;


import android.graphics.Color;
import java.util.Random;

/**
 * Created by anikonov on 12/5/13.
 */
public class Colors {

    public static int randomColor() {
        Random rnd = new Random();
        return Color.argb(255, randInt(rnd, 60, 256), randInt(rnd, 60, 256), randInt(rnd, 60, 256));
    }

    public static int randInt(Random rand, int min, int max) {
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
    public static int colorGray() {
        int rgbNum = 255 - (int) ((15/50.0)*255.0);
        return Color.argb(255, rgbNum, rgbNum, rgbNum);
    }
}
