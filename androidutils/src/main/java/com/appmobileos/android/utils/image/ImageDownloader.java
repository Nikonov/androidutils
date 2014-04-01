package com.appmobileos.android.utils.image;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.appmobileos.android.utils.BuildConfig;
import com.appmobileos.android.utils.file.FileFilters;
import com.nineoldandroids.animation.ObjectAnimator;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Helper class which fast load image in gridView or listView
 */
public class ImageDownloader {
    private static final String TAG = "ImageDownloader";
    public static final int COLOR_BLACK_OPACITY_5_PERCENTAGE = 0x1A000000;
    public static final int COUNT_IMAGES_FOUND_IN_SUBDIRECTORY = 5;
    private int mImageWidth = 100; //default value width in px
    private int mImageHeight = 100; //default value height in px
    private static final int IMAGE_VIEW_COUNT_IMAGES = 4;
    private static final int FADE_IN_TIME = 500;
    private boolean mFadeInBitmap = true;
    private Random mRandom;
    private Resources mResources;
    private Bitmap mLoadingBitmap;

    private HashMap<String, List<Integer>> mPreviousValue = new HashMap<String, List<Integer>>(IMAGE_VIEW_COUNT_IMAGES);

    /**
     * Modes loading images:
     * <p>LOADING_IMAGE - Loading one image</p>
     * <p>LOADING_RANDOM_IMAGE_FROM_DIRECTORY - Loading random image from directory</p>
     * <p>LOADING_RANDOM_IMAGE_FROM_DIRECTORY_RECURSION - Loading random image from directory and if not found images (in root directory)
     * will be open subdirectory and search images. If found COUNT_IMAGES_FOUND_IN_SUBDIRECTORY images, search images stop</p>
     */
    public enum MODE_LOADING_IMAGES {
        LOADING_IMAGE,
        LOADING_RANDOM_IMAGE_FROM_DIRECTORY,
        LOADING_RANDOM_IMAGE_FROM_DIRECTORY_RECURSION,

    }

    /**
     * @param imageSize target image size (width and height will be the same).
     *                  if value < 0, will be set default value 100px.
     */
    public ImageDownloader(int imageSize, Context context) {
        if (context == null)
            throw new NullPointerException("Context == null");
        mResources = context.getResources();
        if (imageSize >= 0) {
            setImageSize(imageSize);
        }
        mLoadingBitmap = initDefaultLoadingImage(imageSize, imageSize);
    }

    /**
     * Set bitmap in ImageView
     *
     * @param path           If selected MODE_LOADING_IMAGES.LOADING_IMAGE (last param in current method ) path must be absolute path image,
     *                       if selected MODE_LOADING_IMAGES.LOADING_RANDOM_IMAGE_FROM_DIRECTORY path must be directory where will be selected random image
     * @param imageView      current ImageView
     * @param loading_images mode loading image
     * @see com.appmobileos.android.utils.image.ImageDownloader.MODE_LOADING_IMAGES
     */
    public void loaderBitmap(String path, ImageView imageView, MODE_LOADING_IMAGES loading_images) {
        if (cancelPotentialDownloaderTask(path, imageView)) {
            BitmapDownloaderTask task = new BitmapDownloaderTask(imageView, loading_images);
            DownloadedDrawable downloadDrawable = new DownloadedDrawable(mResources, mLoadingBitmap, task);
            imageView.setImageDrawable(downloadDrawable);
            task.execute(path);
        }
    }

    /**
     * Create bitmap from path
     *
     * @param pathImageFile image path
     */
    private Bitmap createBitmap(String pathImageFile) {
        Bitmap bitmap = null;
        if (pathImageFile != null) {
            bitmap = ImageUtility.decodeSampledBitmapFromFile(pathImageFile, mImageWidth, mImageHeight);
        }
        return bitmap;
    }

    /**
     * Search image files in directory and random selected щту of them
     *
     * @param pathDirectory   absolute path directory where will be search image files
     * @param enableRecursion If true will be open subdirectory and search image files in them.
     *                        This is working if in pathDirectory don't include image files
     */
    private Bitmap createRandomBitmap(String pathDirectory, boolean enableRecursion) {
        Bitmap resultImage = null;
        if (pathDirectory != null) {
            File rootDir = new File(pathDirectory);
            File[] listImages = rootDir.listFiles(new FileFilters.ImageFileFilter());
            if (listImages != null && listImages.length > 0) {
                int countImages = listImages.length;
                int positionInArray = countImages == 1 ? 0 : randomPosition(countImages, pathDirectory);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "CountImages = " + countImages + "  FINAL go position = " + positionInArray + " and path = " + listImages[positionInArray].getAbsolutePath());
                }
                resultImage = ImageUtility.decodeSampledBitmapFromFile(listImages[positionInArray].getAbsolutePath(), mImageWidth, mImageHeight);
            } else {
                //open subdirectories and search images files
                if (enableRecursion) {
                    resultImage = createRandomBitmapRecursion(pathDirectory);
                }
            }
        }
        return resultImage;
    }

    private Bitmap createRandomBitmapRecursion(String path) {
        Bitmap resultImage = null;
        List<String> imagesPath = new ArrayList<String>(COUNT_IMAGES_FOUND_IN_SUBDIRECTORY);
        if (path != null) {
            foundImagesFiles(path, imagesPath);
            int countImages = imagesPath.size();
            if (countImages == 0) return null;
            if (mRandom == null) mRandom = new Random();
            int resultPosition = mRandom.nextInt(countImages);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "<======CountImages = " + countImages + "  FINAL go position = " + resultPosition + " and path = " + imagesPath.get(resultPosition));
            }
            resultImage = ImageUtility.decodeSampledBitmapFromFile(imagesPath.get(resultPosition), mImageWidth, mImageHeight);

        }
        return resultImage;
    }

    /**
     * Found image files in directory and subdirectory
     *
     * @param path   root path dir where will be search image files
     * @param images place when will be save fount images path
     */
    private void foundImagesFiles(String path, List<String> images) {
        int countRecursion = 0;
        if (path != null) {
            File rootDir = new File(path);
            File[] listImagesFiles = rootDir.listFiles(new FileFilters.ImageFileFilter());
            if (listImagesFiles != null) {
                for (File imageFile : listImagesFiles) {
                    images.add(imageFile.getAbsolutePath());

                }
            }
            if (images.size() >= COUNT_IMAGES_FOUND_IN_SUBDIRECTORY) return;
            //opens folders
            File[] listFolders = rootDir.listFiles(new FileFilters.FolderFilter());
            if (listFolders != null) {
                for (File file : listFolders) {
                    foundImagesFiles(file.getAbsolutePath(), images);
                }
            }
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "foundImagesFiles recursion circle = " + countRecursion++);
            }
        }
    }

    private int randomPosition(int countImages, String path) {
        if (countImages == 0) throw new IllegalArgumentException("count image = 0");
        int resultPosition;
        if (mRandom == null) mRandom = new Random();
        resultPosition = mRandom.nextInt(countImages);
        List<Integer> previousValue = mPreviousValue.get(path);
        if (previousValue == null) {
            Log.i(TAG, "PREVIOUS_VALUE == null");
            List<Integer> collection = new ArrayList<Integer>(countImages);
            collection.add(resultPosition);
            mPreviousValue.put(path, collection);
        } else {
            if (previousValue.contains(resultPosition)) {
                resultPosition = mRandom.nextInt(countImages);
            }
            previousValue.add(resultPosition);
        }
        return resultPosition;
    }

    /**
     * Set the target image size (width and height will be the same).
     *
     * @param size
     */
    public void setImageSize(int size) {
        setImageSize(size, size);
    }

    private void setImageSize(int width, int height) {
        mImageWidth = width;
        mImageHeight = height;
    }

    /**
     * If set to true, the image will fade-in once it has been loaded by the background thread.
     */
    public void setImageFadeIn(boolean fadeIn) {
        mFadeInBitmap = fadeIn;
    }

    public void setLoadingBitmap(Bitmap loadingBitmap) {
        this.mLoadingBitmap = loadingBitmap;
    }

    public void setLoadingBitmap(int resId) {
        this.mLoadingBitmap = BitmapFactory.decodeResource(mResources, resId);
    }

    /**
     * The actual AsyncTask that will asynchronously download the image.
     */
    private class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private String pathImageFile = null;
        private final MODE_LOADING_IMAGES loading_images;
        private final WeakReference<ImageView> imageViewReference;

        private BitmapDownloaderTask(ImageView imageView, MODE_LOADING_IMAGES loading_images) {
            this.loading_images = loading_images;
            this.imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            if (params == null || params.length == 0) return null;
            Bitmap result;
            pathImageFile = params[0];
            switch (loading_images) {
                case LOADING_IMAGE:
                    result = createBitmap(pathImageFile);
                    break;
                case LOADING_RANDOM_IMAGE_FROM_DIRECTORY:
                    result = createRandomBitmap(pathImageFile, false);
                    break;
                case LOADING_RANDOM_IMAGE_FROM_DIRECTORY_RECURSION:
                    result = createRandomBitmap(pathImageFile, true);
                    break;
                default:
                    throw new IllegalArgumentException("Don't support mode loading images = " + loading_images);
            }
            return result;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            ImageView imageView = imageViewReference.get();
            BitmapDownloaderTask downloaderTask = getBitmapDownloaderTask(imageView);
            if ((this == downloaderTask)) {
                setImageBitmap(imageView, bitmap);
                if (imageView != null) {
                    if (imageView.getVisibility() != View.VISIBLE) {
                        imageView.setVisibility(View.VISIBLE);
                    }
                }
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Image set in ImageView");
                }
            }
        }

    }

    /**
     * Returns true if the current download has been canceled or if there was no download in
     * progress on this image view.
     * Returns false if the download in progress deals with the same url. The download is not
     * stopped in that case.
     */
    public static boolean cancelPotentialDownloaderTask(String pathImage, ImageView imageView) {
        BitmapDownloaderTask task = getBitmapDownloaderTask(imageView);
        if (task != null) {
            String pathBitmap = task.pathImageFile;
            if ((pathBitmap == null) || (!pathBitmap.equals(pathImage))) {
                task.cancel(true);
            } else {
                // The same pathImage is already being downloaded.
                return false;
            }
        }
        return true;
    }


    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active download task (if any) associated with this imageView.
     * null if there is no such task.
     */
    private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    /**
     * A fake Drawable that will be attached to the imageView while the download is in progress.
     * <p/>
     * <p>Contains a reference to the actual download task, so that a download task can be stopped
     * if a new binding is required, and makes sure that only the last started download process can
     * bind its result, independently of the download finish order.</p>
     */
    static class DownloadedDrawable extends BitmapDrawable {
        private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

        public DownloadedDrawable(Resources res, Bitmap loaderBitmap, BitmapDownloaderTask bitmapDownloaderTask) {
            super(res, loaderBitmap);
            bitmapDownloaderTaskReference =
                    new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
        }

        public BitmapDownloaderTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }
    }

    private void setImageBitmap(ImageView imageView, Bitmap bitmap) {
        if (imageView != null) {
            if (mFadeInBitmap) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "alpha", 0.0f, 1.0f);
                animator.setDuration(FADE_IN_TIME);
                imageView.setImageBitmap(bitmap);
                animator.start();
            } else {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public boolean isDownloadDrawable(Drawable drawable) {
        return drawable != null && (drawable instanceof DownloadedDrawable);
    }

    /**
     * Cancels any pending work attached to the provided ImageView.
     *
     * @param imageView
     */
    public static void cancelWork(ImageView imageView) {
        final BitmapDownloaderTask bitmapWorkerTask = getBitmapDownloaderTask(imageView);
        if (bitmapWorkerTask != null) {
            bitmapWorkerTask.cancel(true);
            if (BuildConfig.DEBUG) {
                //  final Object bitmapData = bitmapWorkerTask.mData;
                Log.d(TAG, "cancelWork - cancelled");
            }
        }
    }

    private Bitmap initDefaultLoadingImage(int width, int height) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(COLOR_BLACK_OPACITY_5_PERCENTAGE);
        paint.setStyle(Paint.Style.FILL);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawRect(0, 0, width, height, paint);
        return bitmap;
    }
}
