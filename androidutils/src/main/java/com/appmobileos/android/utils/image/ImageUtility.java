package com.appmobileos.android.utils.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.View;


import com.appmobileos.android.utils.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

final public class ImageUtility {
    public static final int READ_BUFFER_SIZE = 32 * 1024;  //32KB
    public static final String TAG = "ImageUtility";

    /**
     * @param path
     * @param sampleSize 1 = 100%, 2 = 50%(1/2), 4 = 25%(1/4), ...
     * @return
     */
    public static Bitmap getBitmapFromLocalPath(String path, int sampleSize) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            return BitmapFactory.decodeFile(path, options);
        } catch (Exception e) {
            //  Logger.e(e.toString());
        }

        return null;
    }

    /**
     * @param bytes
     * @return
     */
    public static Bitmap getBitmapFromBytes(byte[] bytes) {
        try {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            //  Logger.e(e.toString());
        }

        return null;
    }

    /**
     * @param bitmap
     * @param quality 1 ~ 100
     * @return
     */
    public static byte[] compressBitmap(Bitmap bitmap, int quality) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, baos);

            return baos.toByteArray();
        } catch (Exception e) {
            //  Logger.e(e.toString());
        }

        return null;
    }

    /**
     * @param srcBitmap
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap getResizedBitmap(Bitmap srcBitmap, int newWidth, int newHeight) {
        try {
            return Bitmap.createScaledBitmap(srcBitmap, newWidth, newHeight, true);
        } catch (Exception e) {
            //  Logger.e(e.toString());
        }

        return null;
    }

    /**
     * captures given view and converts it to a bitmap
     *
     * @param view
     * @return
     */
    public static Bitmap captureViewToBitmap(View view) {
        Bitmap result = null;

        try {
            result = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
            view.draw(new Canvas(result));
        } catch (Exception e) {
            //Logger.e(e.toString());
        }

        return result;
    }

    /**
     * @param original
     * @param format
     * @param quality
     * @param outputLocation
     * @return
     */
    public static boolean saveBitmap(Bitmap original, Bitmap.CompressFormat format, int quality, String outputLocation) {
        if (original == null)
            return false;

        try {
            return original.compress(format, quality, new FileOutputStream(outputLocation));
        } catch (Exception e) {
            //  Logger.e(e.toString());
        }

        return false;
    }

    private static Bitmap codec(Bitmap src, Bitmap.CompressFormat format,
                                int quality) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        src.compress(format, quality, os);

        byte[] array = os.toByteArray();
        return BitmapFactory.decodeByteArray(array, 0, array.length);
    }

    /**
     * @param filepath
     * @param widthLimit
     * @param heightLimit
     * @param totalSize
     * @return
     */
    public static Bitmap getResizedBitmap(String filepath, int widthLimit, int heightLimit, int totalSize) {
        int outWidth = 0;
        int outHeight = 0;
        int resize = 1;
        InputStream input = null;

        try {
            input = new FileInputStream(new File(filepath));

            BitmapFactory.Options getSizeOpt = new BitmapFactory.Options();
            getSizeOpt.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, getSizeOpt);
            outWidth = getSizeOpt.outWidth;
            outHeight = getSizeOpt.outHeight;

            while ((outWidth / resize) > widthLimit || (outHeight / resize) > heightLimit) {
                resize *= 2;
            }
            resize = resize * (totalSize + 15) / 15;

            BitmapFactory.Options resizeOpt = new BitmapFactory.Options();
            resizeOpt.inSampleSize = resize;

            input.close();
            input = null;

            input = new FileInputStream(new File(filepath));
            Bitmap bitmapImage = BitmapFactory.decodeStream(input, null, resizeOpt);
            return bitmapImage;
        } catch (Exception e) {
            //  Logger.e(e.toString());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    //  Logger.e(e.toString());
                }
            }
        }
        return null;
    }

    /**
     * @param sizes obtained from camera.getParameters().getSupportedPreviewSizes()
     * @param w
     * @param h
     * @return
     */
    public static Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int realHeight = options.outHeight;
        final int realWidth = options.outWidth;
        int inSampleSize = 1;
        if (realHeight > reqHeight || realHeight > reqWidth) {
            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) realHeight / (float) reqHeight);
            final int widthRatio = Math.round((float) realWidth / (float) reqWidth);
            if(BuildConfig.DEBUG){
                Log.i(TAG, "REAL SIZE = " + realWidth + "x" + realHeight +
                        " smaller version = " + reqWidth + "x" + reqHeight +
                        " RATIO height = " + heightRatio + " RADIO width = " + widthRatio);
            }
            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * This is method based on the exercise "Loading Large Bitmaps Efficiently".
     * <a href="http://developer.android.com/training/displaying-bitmaps/load-bitmap.html#load-bitmap">More information</a>
     */
    public static Bitmap decodeSampledBitmapFromFile(String pathFile,
                                                     int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathFile, options);
        // Calculate inSampleSize
        options.inSampleSize = ImageUtility.calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathFile, options);
    }

    public static int[] infoImage(String filePath) {
        int[] result = new int[2];
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        result[0] = options.outHeight;
        result[1] = options.outWidth;
        return result;
    }

    /**
     * @param original
     * @param radius
     * @return
     */
    public Bitmap getBlurredBitmap(Bitmap original, int radius) {
        if (radius < 1)
            return null;

        int width = original.getWidth();
        int height = original.getHeight();
        int wm = width - 1;
        int hm = height - 1;
        int wh = width * height;
        int div = radius + radius + 1;
        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, p1, p2, yp, yi, yw;
        int vmin[] = new int[Math.max(width, height)];
        int vmax[] = new int[Math.max(width, height)];
        int dv[] = new int[256 * div];
        for (i = 0; i < 256 * div; i++)
            dv[i] = i / div;

        int[] blurredBitmap = new int[wh];
        original.getPixels(blurredBitmap, 0, width, 0, 0, width, height);

        yw = 0;
        yi = 0;

        for (y = 0; y < height; y++) {
            rsum = 0;
            gsum = 0;
            bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = blurredBitmap[yi + Math.min(wm, Math.max(i, 0))];
                rsum += (p & 0xff0000) >> 16;
                gsum += (p & 0x00ff00) >> 8;
                bsum += p & 0x0000ff;
            }
            for (x = 0; x < width; x++) {
                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                    vmax[x] = Math.max(x - radius, 0);
                }
                p1 = blurredBitmap[yw + vmin[x]];
                p2 = blurredBitmap[yw + vmax[x]];

                rsum += ((p1 & 0xff0000) - (p2 & 0xff0000)) >> 16;
                gsum += ((p1 & 0x00ff00) - (p2 & 0x00ff00)) >> 8;
                bsum += (p1 & 0x0000ff) - (p2 & 0x0000ff);
                yi++;
            }
            yw += width;
        }

        for (x = 0; x < width; x++) {
            rsum = gsum = bsum = 0;
            yp = -radius * width;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;
                rsum += r[yi];
                gsum += g[yi];
                bsum += b[yi];
                yp += width;
            }
            yi = x;
            for (y = 0; y < height; y++) {
                blurredBitmap[yi] = 0xff000000 | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];
                if (x == 0) {
                    vmin[y] = Math.min(y + radius + 1, hm) * width;
                    vmax[y] = Math.max(y - radius, 0) * width;
                }
                p1 = x + vmin[y];
                p2 = x + vmax[y];

                rsum += r[p1] - r[p2];
                gsum += g[p1] - g[p2];
                bsum += b[p1] - b[p2];

                yi += width;
            }
        }

        return Bitmap.createBitmap(blurredBitmap, width, height, Bitmap.Config.RGB_565);
    }

}