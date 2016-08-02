package com.appmobileos.android.utils;

import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.spbtv.tele2.BuildConfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Andrey Nikonov on 31.10.14.
 * Helper class which help write logs and support different levels (debug, release) application.
 */
public class BradburyLogger {
    private static final String TAG = makeLogTag(BradburyLogger.class);
    private static final String LOG_PREFIX = "bradbury_";
    private static final String DEFAULT_TAG = LOG_PREFIX + BradburyLogger.class.getSimpleName();
    private static final String DEFAULT_MESSAGE = "Message == null. Please, check message parameter value";
    private static boolean sDebugAppLevel = false;
    private static boolean sDebugBuild = false;
    private static final String DIRECTORY_LOGS = "logs";
    private static long MAX_LOG_FILE_SIZE_BYTES = 50 * 1024 * 1024;//50 mb
    private static long MAX_LOG_DIRECTORY_SIZE_BYTES = MAX_LOG_FILE_SIZE_BYTES * 10;//500 mb
    private static String PREFIX_LOG_CAT_FILE = "logCat";
    private static String PREFIX_DMESG_FILE = "dmesg";

    public static void initDebugBuild(boolean debug) {
        sDebugBuild = debug;
    }

    /**
     * Make tag by application pattern
     *
     * @return tag made by application pattern. If have any problem return {@link BradburyLogger#DEFAULT_TAG}
     */

    public static String makeLogTag(String str) {
        if (isDebugGradleBuildEnable()) {
            if (str != null) {
                return LOG_PREFIX + str;
            } else {
                Log.e(DEFAULT_TAG, " str == null. Used default tag ");
            }
        } else {
            //application not DEBUG level don't write logs in system
        }
        return DEFAULT_TAG;
    }

    /**
     * Make tag by application pattern
     *
     * @return tag made by application pattern. If have any problem return {@link BradburyLogger#DEFAULT_TAG}
     */
    public static String makeLogTag(Class<?> cls) {
        if (isDebugGradleBuildEnable()) {
            if (cls != null) {
                return LOG_PREFIX + cls.getSimpleName();
            } else {
                Log.e(DEFAULT_TAG, " str == null. Used default tag ");
            }
        } else {
            //application not DEBUG level don't write logs in system
        }
        return DEFAULT_TAG;
    }

    /**
     * Write log to logCat if application used debug level
     */
    public static void logDebug(String tag, String message) {
        if (isDebugGradleBuildEnable()) {
            String[] parameters = processingOptions(tag, message);
            Log.d(parameters[0], parameters[1]);
        } else {
            //application not DEBUG level don't write logs in system
        }
    }

    /**
     * Write log to logCat if application used debug level
     */
    public static void logInfo(String tag, String message) {
        if (isDebugGradleBuildEnable()) {
            String[] parameters = processingOptions(tag, message);
            Log.i(parameters[0], parameters[1]);
        } else {
            //application not DEBUG level don't write logs in system
        }
    }

    /**
     * Write log to logCat if application used debug warning
     */
    public static void logWarning(String tag, String message) {
        if (isDebugGradleBuildEnable()) {
            String[] parameters = processingOptions(tag, message);
            Log.w(parameters[0], parameters[1]);
        } else {
            //application not DEBUG level don't write logs in system
        }
    }

    /**
     * Write log to logCat if application used error level
     */
    public static void logError(String tag, String message) {
        if (isDebugGradleBuildEnable()) {
            String[] parameters = processingOptions(tag, message);
            Log.e(parameters[0], parameters[1]);
        } else {
            //application not DEBUG level don't write logs in system
        }
    }

    /**
     * Write log to logCat if application used error level
     */
    public static void logError(String tag, Throwable w) {
        if (isDebugGradleBuildEnable()) {
            String[] parameters = processingOptions(tag, (w == null ? "null" : w.getMessage()));
            Log.e(parameters[0], parameters[1]);
        } else {
            //application not DEBUG level don't write logs in system
        }
    }

    /**
     * return array String first item = tag, second item = message
     */
    private static String[] processingOptions(String tag, String message) {
        String[] correctParameters = new String[2];
        if (isDebugGradleBuildEnable()) {
            String correctTag = tag;
            String correctMessage = message;
            if (correctTag == null) {
                correctTag = DEFAULT_TAG;
            }
            if (message == null) {
                correctMessage = DEFAULT_MESSAGE;
            }
            correctParameters[0] = correctTag;
            correctParameters[1] = correctMessage;
            return correctParameters;
        } else {
            //application not DEBUG level don't write logs in system
        }
        //default values
        correctParameters[0] = DEFAULT_TAG;
        correctParameters[1] = DEFAULT_MESSAGE;
        return correctParameters;
    }

    /**
     * Level application logic. Typical this is used when debug large message and need enable only now.
     * The {@link #isDebugGradleBuildEnable()} have high priority.
     * If {@link #isDebugGradleBuildEnable()} disable, this is method always return false
     *
     * @return true if debug level application
     */
    public static boolean isDebugApplicationEnable() {
        return isDebugGradleBuildEnable() && sDebugAppLevel;
    }

    /**
     * Level gradle build logic.
     * Logic need configure in build.gradle file
     *
     * @return true if debug level gradle build logic enable
     */
    public static boolean isDebugGradleBuildEnable() {
        return sDebugBuild;
    }

    /**
     * @see {@link #isDebugApplicationEnable()}
     */
    public static void setDebugAppLevel(boolean debugLevel) {
        sDebugAppLevel = debugLevel;
    }

    /**
     * Write data in log
     */
    public static void logHeap(String tag) {
        Runtime info = Runtime.getRuntime();
        long freeSize = info.freeMemory();
        long totalSize = info.totalMemory();
        long maxSize = info.maxMemory();
        long usedSize = totalSize - freeSize;
        long usedInMgs = usedSize / (1024 * 1024);
        Double allocated = new Double(Debug.getNativeHeapAllocatedSize()) / new Double((1048576));
        Double available = new Double(Debug.getNativeHeapSize()) / 1048576.0;
        Double free = new Double(Debug.getNativeHeapFreeSize()) / 1048576.0;
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);

        logDebug(tag == null ? TAG : tag, "debug. =================================");
        logDebug(tag == null ? TAG : tag, "debug.heap native: allocated " + df.format(allocated) + "MB of " + df.format(available) + "MB (" + df.format(free) + "MB free)");
        logDebug(tag == null ? TAG : tag, "debug.memory: allocated (HEAP SIZE): " + df.format(totalSize / 1048576) + "MB of (MAX SIZE) "
                + df.format(maxSize / 1048576) + "MB (" + df.format(freeSize / 1048576) + "MB FREE)" + " APP NOW USE = " + usedInMgs);

    }

    private static String currentTime() {
        SimpleDateFormat fmtOut = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        long currentTime = System.currentTimeMillis();
        return fmtOut.format(new Date(currentTime));
    }


    public static String parseVodTime(int timeMs) {
        StringBuilder mFormatBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    public static void infoProduct(String tag) {
        StringBuilder sb = new StringBuilder();
        sb.append("VERSION.RELEASE {").append(Build.VERSION.RELEASE).append("}")
                .append("\nVERSION.INCREMENTAL {").append(Build.VERSION.INCREMENTAL).append("}")
                .append("\nVERSION.SDK {").append(Build.VERSION.SDK).append("}")
                .append("\nBOARD {").append(Build.BOARD).append("}")
                .append("\nBRAND {").append(Build.BRAND).append("}")
                .append("\nDEVICE {").append(Build.DEVICE).append("}")
                .append("\nHARDWARE {").append(Build.HARDWARE).append("}")
                .append("\nMANUFACTURER {").append(Build.MANUFACTURER).append("}")
                .append("\nMODEL {").append(Build.MODEL).append("}")
                .append("\nSERIAL {").append(Build.SERIAL).append("}")
                .append("\nFINGERPRINT {").append(Build.FINGERPRINT).append("}")
                .append("\nHOST {").append(Build.HOST).append("}")
                .append("\nID {").append(Build.ID).append("}");
        logDebug(tag, " About tv box " + sb.toString());
    }

    public static File createLogCatLogFile() {
        return createLogFile(PREFIX_LOG_CAT_FILE);
    }

    private static File createLogFile(String prefix) {
        File logDir = getLogDir();
        if (logDir == null) return null;
        cleanLogDirIfNeed();
        //calculated 80% of free space
        long freeSpaceBytes = (long) (logDir.getFreeSpace() * 0.8);
        if (freeSpaceBytes > MAX_LOG_DIRECTORY_SIZE_BYTES) {
            SimpleDateFormat formatName = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
            formatName.setTimeZone(TimeZone.getDefault());
            File logFile = new File(logDir, prefix + "_" + formatName.format(new Date(System.currentTimeMillis())).concat(".log"));
            //delete old file if need
            if (logFile.exists()) {
                boolean deleteFile = logFile.delete();
                if (!deleteFile) {
                    logError(DEFAULT_TAG, " Can't delete old file: " + logFile.getAbsolutePath());
                    return null;
                }
            }
            //create new file
            try {
                boolean resultCreate = logFile.createNewFile();
                if (!resultCreate) {
                    logError(DEFAULT_TAG, " Can't create new file: " + logFile.getAbsolutePath());
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return logFile;
        } else {
            logWarning(DEFAULT_TAG, " Can't write log, because little free space.");
        }
        return null;
    }

    public static File createDmesgLogFile() {
        return createLogFile(PREFIX_DMESG_FILE);
    }

    public static void debugBackStack(FragmentManager fm) {
        if (isDebugGradleBuildEnable()) {
            int count = fm.getBackStackEntryCount();
            logDebug(DEFAULT_TAG, "===START debugBackStack ===");
            logDebug(DEFAULT_TAG, "Fragment back stack has " + count + " entries");
            for (int i = 0; i < count; i++) {
                String name = fm.getBackStackEntryAt(i).getName();
                logDebug(DEFAULT_TAG, "entry " + i + ": " + name);
            }
            logDebug(DEFAULT_TAG, "===END debugBackStack ===");
        }
    }
/*
    *//**
     * Stop write logcat
     *//*
    public void stopWriteLogcat() {
        mLogcatThread = null;
    }

    */
    /**
     * Start write logcat
     *//*
    public void startWriteLogcat() {
        File logFile = createLogCatLogFile(null);
        if (logFile == null) {
            logError(DEFAULT_TAG, " Can't start write file. I have any problems. ");
            return;
        }
        startWriteLogcatToFile(createLogCatLogFile());
    }*/

    private volatile Thread mLogcatThread;

    private void startWriteLogcatToFile(final File file) {
        mLogcatThread = new Thread(new Runnable() {
            @Override
            public void run() {
                logDebug(DEFAULT_TAG, " start writing logs to file: " + file.getAbsolutePath());
                Process process = null;
                BufferedWriter writer = null;
                BufferedReader bufferedReader = null;
                try {
                    process = Runtime.getRuntime().exec("logcat -v time");
                    writer = new BufferedWriter(new FileWriter(file));
                    bufferedReader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));
                    String line;
                    Thread thisThread = Thread.currentThread();
                    while ((line = bufferedReader.readLine()) != null &&
                            thisThread == mLogcatThread &&
                            file.length() < MAX_LOG_FILE_SIZE_BYTES) {
                        writer.write(line);
                        writer.newLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (bufferedReader != null) bufferedReader.close();
                        if (writer != null) writer.close();
                        if (process != null) process.destroy();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mLogcatThread.start();
    }


    public static File getLogDir() {
        if (!isExternalStorageWritable()) {
            logError(DEFAULT_TAG, " Problem with external storage. Do it mount? ");
            return null;
        }
        File dirLogs = new File(Environment.getExternalStorageDirectory(), DIRECTORY_LOGS);
        if (!dirLogs.exists()) {
            boolean result = dirLogs.mkdir();
            if (!result) {
                logWarning(DEFAULT_TAG, " I can't create directory: " + DIRECTORY_LOGS);
                return null;
            }
        }
        return dirLogs;
    }

    private static void cleanLogDirIfNeed() {
        long lengthAllLogsFiles = 0;
        File dirLog = getLogDir();
        if (dirLog == null) return;
        File[] listLogFiles = dirLog.listFiles();
        for (File logFile : listLogFiles) {
            if (logFile.isFile()) {
                lengthAllLogsFiles += logFile.length();
            }
        }
        if (lengthAllLogsFiles >= MAX_LOG_DIRECTORY_SIZE_BYTES) {
            Arrays.sort(listLogFiles, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                }
            });
            final int countNeedDeleteFiles = 3;
            int counterDeleted = 0;
            for (File oldLogFile : listLogFiles) {
                if (counterDeleted <= countNeedDeleteFiles) {
                    boolean resultDeleted = oldLogFile.delete();
                    if (!resultDeleted) {
                        logWarning(DEFAULT_TAG, " Can;t delete file: " + oldLogFile.getAbsolutePath());
                    }
                } else {
                    break;
                }
                counterDeleted++;
            }
        }
    }

    public static File makeDumpLogs() {
        File logCatLog = createLogCatLogFile();
        if (logCatLog == null) {
            logError(DEFAULT_TAG, " Can't make dump file. I have any problems. ");
            return null;
        }
        final String pathLogCatFile = logCatLog.getPath();
        logDebug(DEFAULT_TAG, " pathLogCatFile: " + pathLogCatFile + "\n");
        try {
            Runtime.getRuntime().exec("logcat -d -f " + pathLogCatFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return logCatLog;
    }

    /* Checks if external storage is available for read and write */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static void welcome(String tag, @NonNull Context context) {
        Context app = context.getApplicationContext();
        final String packageApp = app.getPackageName();
        String versionName = null;
        try {
            versionName = app.getPackageManager().getPackageInfo(packageApp, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int stringId = app.getApplicationInfo().labelRes;
        String nameApp = app.getString(stringId);
        StringBuilder builder = new StringBuilder();
        String separator = "######";
        builder.append(separator).append(" GOOD DAY WORLD ").append(separator).append("\n")
                .append(separator).append(" APPLICATION: ").append(nameApp.toUpperCase()).append(" ")
                .append(versionName).append(" ( ").append(BuildConfig.VERSION_CODE).append(" ) ").append(" STARTED ").append(separator).append("\n")
                .append(separator).append(" DEVICE INFO: ").append(Build.MODEL)
                .append(" (").append("api:").append(Build.VERSION.SDK_INT).append(")").append(" ").append(separator).append("\n")
                .append(separator).append(" I wish you good work ".toUpperCase()).append(separator);
        logDebug(tag, builder.toString());
    }

    public static void welcome(@NonNull Context context) {
        welcome(DEFAULT_TAG, context);
    }

    private static boolean checkFile(File file) throws IOException {
        if (!file.exists()) {
            boolean resultCreateDestinationFile = file.createNewFile();
            if (!resultCreateDestinationFile) {
                throw new FileNotFoundException("File " + file.getAbsoluteFile() + " don't create in file system");
            }
        }
        return true;
    }

    public static void copyDBFile(Context context, String databaseName) {
        if (!isDebugGradleBuildEnable()) return;
        try {
            File database = new File("/data/data/" + context.getPackageName() + "/databases/" + databaseName);
            File copyDataBase = new File(Environment.getExternalStorageDirectory() + "/" + databaseName);
            copyFile(database, copyDataBase);
            if (!database.exists() || !copyDataBase.exists()) {
                logDebug(DEFAULT_TAG, " FILE NOT FOUNT ORIGINAL = " + database.getAbsoluteFile() + " COPY = " + copyDataBase.getAbsolutePath());
            }
            logDebug(DEFAULT_TAG, "Database coped path file: " + copyDataBase.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param sourceFile      the file fill be copied to destinationFile
     * @param destinationFile the is a new file, double sourceFile
     */

    public static void copyFile(File sourceFile, File destinationFile) throws IOException {
        checkFile(destinationFile);
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destinationFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }
}
