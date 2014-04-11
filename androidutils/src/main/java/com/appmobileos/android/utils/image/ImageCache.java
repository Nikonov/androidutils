package com.appmobileos.android.utils.image;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.appmobileos.android.utils.AndroidVersion;
import com.appmobileos.android.utils.BuildConfig;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Created by anikonov on 4/10/14.
 */
public class ImageCache {
    private static final String TAG = "ImageCache";
    private static final String RETAIN_FRAGMENT_TAG = "f_tag_retain";
    // Default memory cache size in kilobytes
    private static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 5; // 5MB

    // Default disk cache size in bytes
    private static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB

    // Compression settings when writing images to disk cache
    private static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    private static final int DEFAULT_COMPRESS_QUALITY = 70;
    private static final int DISK_CACHE_INDEX = 0;
    // Constants to easily toggle various caches
    private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
    private static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
    private static final boolean DEFAULT_INIT_DISK_CACHE_ON_CREATE = false;

    private ImageCacheParameters mCacheParams;

    private LruCache<String, BitmapDrawable> mMemoryCache;

    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;

    private DiskLruCache mDiskLruCache;



    /**
     * Create a new ImageCache object using the specified parameters.
     * This should not be called directly by other classes, instead use
     * {@link com.appmobileos.android.utils.image.ImageCache#getInstance(android.support.v4.app.FragmentManager, com.appmobileos.android.utils.image.ImageCache.ImageCacheParameters)}
     * to fetch an ImageCache instance.
     *
     * @param parameters The cache parameters to initialize the cache
     */
    private ImageCache(ImageCacheParameters parameters) {
        initCache(parameters);
    }

    /**
     * Initialize the cache, providing all parameters
     *
     * @param parameters The cache parameters to initialize the cache
     */
    private void initCache(ImageCacheParameters parameters) {
        mCacheParams = parameters;
        if (mCacheParams.memoryCacheEnable) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "Memory cache created (size =" + mCacheParams.memCacheSize + ")");
            }

            mMemoryCache = new LruCache<String, BitmapDrawable>(mCacheParams.memCacheSize) {

                // TODO skipped working with RecyclingBitmapDrawable

                @Override
                protected int sizeOf(String key, BitmapDrawable value) {
                    final int bitmapSize = getBitmapSize(value) / 1024;
                    return bitmapSize == 0 ? 1 : bitmapSize;
                }
            };

        }
        if (parameters.initDiskCacheOnCreate) {
            initDiskCache();
        }
    }

    /**
     * Initializer the disk cache.
     * That this includes disk access so this should not be executed on the main/UI thread
     * and must have permission write to external storage.
     *
     * @see android.Manifest.permission#WRITE_EXTERNAL_STORAGE
     */
    public void initDiskCache() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
                File diskCacheDir = mCacheParams.diskCacheDir;
                if (mCacheParams.diskCacheEnable && diskCacheDir != null) {
                    if (!diskCacheDir.exists()) {
                        diskCacheDir.mkdirs();

                    }
                    if (getUsableSpace(diskCacheDir) > mCacheParams.diskCacheSize) {
                        try {
                            mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, mCacheParams.diskCacheSize);
                            if (BuildConfig.DEBUG) {
                                Log.i(TAG, "Disc cache initialized");
                            }
                        } catch (IOException e) {
                            mCacheParams.diskCacheDir = null;
                            e.printStackTrace();
                            Log.e(TAG, "initDiskCache - " + e);
                        }
                    }
                }

            }
            mDiskCacheStarting = false;
            mDiskCacheLock.notifyAll();
        }
    }

    /**
     * Check how much usable space is available at a given path.
     *
     * @param path The path to check
     * @return The space available in bytes
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static long getUsableSpace(File path) {
        if (AndroidVersion.hasGingerbread()) {
            return path.getUsableSpace();
        }
        final StatFs statFs = new StatFs(path.getPath());
        return (long) statFs.getBlockSize() * (long) statFs.getAvailableBlocks();
    }

    /**
     * Adds a bitmap to both memory and disk cache.
     *
     * @param key   Unique identifier for the bitmap to store
     * @param value The  bitmap drawable to store
     */
    public void addBitmapToCache(String key, BitmapDrawable value) {
        if (key == null || value == null) return;
        //Add to memory cache
        if (mMemoryCache != null) {
            // TODO skipped working with RecyclingBitmapDrawable
            mMemoryCache.put(key, value);
        }

        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                final String keyDisk = hashKeyForDisk(key);
                OutputStream outputStream = null;
                try {
                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(keyDisk);
                    if (snapshot != null) {
                        final DiskLruCache.Editor editor = mDiskLruCache.edit(keyDisk);
                        if (editor != null) {
                            outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
                            value.getBitmap().compress(mCacheParams.compressFormat, mCacheParams.compressQuality, outputStream);
                            editor.commit();
                            outputStream.close();
                        } else {
                            snapshot.getInputStream(DISK_CACHE_INDEX).close();
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "addBitmapToCache - " + e);
                    e.printStackTrace();
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }

    /**
     * Get from disk cache.
     *
     * @param key Unique identifier for which item to get
     * @return The bitmap if found in cache, null otherwise
     */
    public Bitmap getBitmapFromDiskCache(String key) {
        final String keyDisk = hashKeyForDisk(key);
        Bitmap bitmap = null;
        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (mDiskLruCache != null) {
                InputStream inputStream = null;
                try {
                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(keyDisk);
                    if (snapshot != null) {
                        if (BuildConfig.DEBUG) {
                            Log.i(TAG, "Disk cache hit");
                        }
                        inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                        if (inputStream != null) {
                            FileDescriptor fd = ((FileInputStream) inputStream).getFD();
                            bitmap = ImageUtility.decodeSampledBitmapFromDescriptor(fd, Integer.MAX_VALUE, Integer.MIN_VALUE, this);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "getBitmapFromDiskCache - " + e);
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
            return bitmap;
        }
    }

    /**
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a
     * disk filename.
     */
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = byteToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String byteToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * Get from memory cache
     *
     * @param key Unique identifier for which item to get
     * @return The bitmap drawable if found in cache, null otherwise
     */
    public BitmapDrawable getBitmapFromMemCache(String key) {
        BitmapDrawable bitmapDrawable = null;
        if (mMemoryCache != null) {
            bitmapDrawable = mMemoryCache.get(key);
        }
        if (BuildConfig.DEBUG && bitmapDrawable != null) {
            Log.d(TAG, "Memory cache hit");
        }
        return bitmapDrawable;
    }

    /**
     * Clears both the memory and disk cache associated with this ImageCache object. Note that
     * this includes disk access so this should not be executed on the main/UI thread.
     */
    public void clearCache() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "Memory cache cleared");
            }
        }
        synchronized (mDiskCacheLock) {
            mDiskCacheStarting = true;
            if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
                try {
                    mDiskLruCache.delete();
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Disk cache cleared");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "clearCache - " + e);
                }
                mDiskLruCache = null;
                initDiskCache();
            }
        }
    }

    /**
     * Flushes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.
     */
    public void flush() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    mDiskLruCache.flush();
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Disk cache flushed");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "flush - " + e);
                }
            }
        }
    }

    /**
     * Closes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.
     */
    public void close() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    if (!mDiskLruCache.isClosed()) {
                        mDiskLruCache.close();
                        mDiskLruCache = null;
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Disk cache closed");
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "close - " + e);
                }
            }
        }
    }

    /**
     * Return an {@link com.appmobileos.android.utils.image.ImageCache} instance. A {@link com.appmobileos.android.utils.image.ImageCache.RetainFragment}
     * is used to retain the ImageCache object across configuration changes such as a change in device orientation.
     *
     * @param fragmentManager      The FragmentManager manager to use
     * @param imageCacheParameters The cache parameters to use if the ImageCache needs instantiation.
     * @return An existing retained ImageCache object or a new one if one did not exist
     */
    public static ImageCache getInstance(FragmentManager fragmentManager, ImageCacheParameters imageCacheParameters) {
        final RetainFragment fragment = findOrCreateRetainFragment(fragmentManager);
        ImageCache imageCache = (ImageCache) fragment.getObject();
        if (imageCache == null) {
            imageCache = new ImageCache(imageCacheParameters);
            fragment.setObject(imageCache);
        }
        return imageCache;
    }

    /**
     * Locate an existing instance of this fragment or if not found,
     * create and add it using FragmentManager
     *
     * @param fragmentManager The FragmentManager manager to use
     * @return The existing instance of the Fragment or the new instance if just created.
     */
    private static RetainFragment findOrCreateRetainFragment(FragmentManager fragmentManager) {
        RetainFragment retainFragment = (RetainFragment) fragmentManager.findFragmentByTag(RETAIN_FRAGMENT_TAG);
        if (retainFragment == null) {
            retainFragment = new RetainFragment();
            fragmentManager.beginTransaction().add(retainFragment, RETAIN_FRAGMENT_TAG).commitAllowingStateLoss();
        }
        return retainFragment;
    }

    public static class ImageCacheParameters {
        public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;
        public int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
        public File diskCacheDir;
        public Bitmap.CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
        public int compressQuality = DEFAULT_COMPRESS_QUALITY;
        public boolean memoryCacheEnable = DEFAULT_MEM_CACHE_ENABLED;
        public boolean diskCacheEnable = DEFAULT_DISK_CACHE_ENABLED;
        public boolean initDiskCacheOnCreate = DEFAULT_INIT_DISK_CACHE_ON_CREATE;

        /**
         * Create a set of image cache parameters
         *
         * @param context                application's context
         * @param diskCacheDirectoryName A unique subdirectory name that will be appended to the application cache directory.
         *                               Usually "cache" or "images" is sufficient.
         */
        public ImageCacheParameters(Context context, String diskCacheDirectoryName) {
            diskCacheDir = getDiskCacheDir(context, diskCacheDirectoryName);
        }

        /**
         * Sets the memory cache size based on a percentage of the max available VM memory.
         * Eg. settings percent to 0.2 would set the memory cache to one fifty of the available memory.
         * Memory cache size is stored in kilobytes instead of bytes as this will eventually be passed
         * to construct a LruCache which takes an int in this constructor
         *
         * @param percent Percent of available application memory to use to size memory cache
         * @throws {@link java.lang.IllegalArgumentException} if percent is < 0.01 or >8.
         */
        public void setMemCacheSizePercent(float percent) {
            if (percent < 0.01f || percent > 0.8f) {
                throw new IllegalArgumentException("setMemCacheSizePercent - percent must be "
                        + "between 0.01 and 0.8 (inclusive). Current value  percent =  " + percent);
            }
            memCacheSize = Math.round(percent * Runtime.getRuntime().maxMemory() / 1024);
        }
    }

    /**
     * Get a usable cache directory (external if available,internal otherwise)
     *
     * @param context    application's context
     * @param uniqueName A unique directory name to append to cached dir
     * @return The cache directory
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath = null;
        boolean externalStorageEnable = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !isExternalStorageRemovable();
        if (externalStorageEnable) {
            cachePath = getExternalCacheDir(context).getPath();
        } else {
            File cacheDir = context.getCacheDir();
            if (cacheDir != null) cachePath = cacheDir.getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * Get the external app cache directory.
     *
     * @return True if external storage is removable ( like an SD card), false otherwise.
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static boolean isExternalStorageRemovable() {
        if (AndroidVersion.hasGingerbread()) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    /**
     * Get the external application cache directory.
     *
     * @param context application's context
     * @return The external cache dir
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static File getExternalCacheDir(Context context) {
        if (AndroidVersion.hasFroyo()) {
            return context.getExternalCacheDir();
        }
        // Before Froyo we need to construct the external cache dir ourselves
        final String cachedDir = "/Android/data/" + context.getPackageName() + "/cache";
        return new File(Environment.getExternalStorageDirectory().getPath() + cachedDir);
    }

    /**
     * Get the size in byte of a bitmap in a {@link android.graphics.drawable.BitmapDrawable}.
     * Note that from Android 4.4 (KitKat) onward this returns the allocated memory size of
     * the bitmap which can be larger than use actual bitmap data byte count (in the case war re-used).
     *
     * @param value BitmapDrawable for which will be count size
     * @return size bitmap in byte
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static int getBitmapSize(BitmapDrawable value) {
        Bitmap bitmap = value.getBitmap();
        if (AndroidVersion.hasKitKat()) {
            return bitmap.getAllocationByteCount();
        }
        if (AndroidVersion.hasHoneycombMR1()) {
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    /**
     * A simple non-UI fragment that stores a single Object and is retained over configuration changes.
     * It will be used to retain the ImageCache object.
     */
    public static class RetainFragment extends Fragment {
        private Object mObject;

        /**
         * Empty constructor as per the Fragment documentation
         */
        public RetainFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //Make sure this Fragment is retained over o configuration change
            setRetainInstance(true);
        }

        /**
         * Store a single object in the Fragment
         *
         * @param object The object to store
         */
        public void setObject(Object object) {
            mObject = object;
        }

        /**
         * Get the stored object
         *
         * @return The stored object
         */
        public Object getObject() {
            return mObject;
        }
    }

}
