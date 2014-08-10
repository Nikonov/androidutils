package com.appmobileos.android.utils.file;

import android.os.Environment;
import android.util.Log;

import com.appmobileos.android.utils.BuildConfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Various operations on I/O or file
 */
public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * Save or copy (from sourceInputStream) InputStream in file.
     *
     * @param sourceInputStream input stream which will be copied in destinationFile
     * @param destinationFile   this is a new(if don't created) file, double input stream.
     *                          Will be created if does not exist/
     */
    public static void saveISInFile(InputStream sourceInputStream, File destinationFile) throws IOException {
        if (sourceInputStream == null || destinationFile == null) {
            throw new NullPointerException("sourceInputStream or destinationFile == null. Now sourceInputStream = " + sourceInputStream + " destinationFile = " + destinationFile);
        }
        checkFile(destinationFile);
        OutputStream destination = null;
        BufferedInputStream bufferedInputStream = new BufferedInputStream(sourceInputStream);
        try {
            destination = new BufferedOutputStream(new FileOutputStream(destinationFile));
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bufferedInputStream.read(buffer)) > 0) {
                destination.write(buffer, 0, len);
            }
        } finally {
            if (destination != null) {
                destination.flush();
                destination.close();
            }
            bufferedInputStream.close();
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

    /**
     * Delete all files in directory
     *
     * @param path directory path
     */
    public static void clearDirectory(String path) {
        if (path == null) return;
        File file = new File(path);
        File[] filesInDir = file.listFiles();
        if (filesInDir != null) {
            for (File fileInDir : filesInDir) {
                fileInDir.delete();
            }
        }
    }

    private static boolean checkFile(File file) throws IOException {
        if (!file.exists()) {
            Log.d(TAG, "Created file = " + file.getAbsolutePath());
            boolean resultCreateDestinationFile = file.createNewFile();
            if (!resultCreateDestinationFile) {
                throw new FileNotFoundException("File " + file.getAbsoluteFile() + " don't create in file system");
            }
        }
        return true;
    }


    private static InputStream openFile(File fileOpen) {
        if (fileOpen == null || !fileOpen.exists()) return null;
        InputStream is = null;
        try {
            is = new FileInputStream(fileOpen);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return is;
    }

    public static InputStream testJsonFileAndroid(String nameFileWithoutExtension) {
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File folder = new File(rootPath + File.separator + nameFileWithoutExtension);
        if (!folder.exists()) folder.mkdir();
        File jsonFile = new File(folder.getAbsolutePath() + File.separator + nameFileWithoutExtension + ".json");
        return openFile(jsonFile);
    }

    public static InputStream testJsonStringAndroid(String jsonData) {
        InputStream stream = null;
        try {
            stream = new ByteArrayInputStream(jsonData.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return stream;
    }

    /**
     * @param file   the file will be added to archive
     * @param zos    current zip archive
     * @param buffer buffer used read byte from input stream
     */
    public static void addToZipFile(File file, ZipOutputStream zos, byte[] buffer) throws IOException {
        if (file == null || zos == null || buffer == null) {
            throw new NullPointerException("file or zos or buffer == null. Now file = " + file + " zos = " + zos + " and check buffer params");
        }
        if (BuildConfig.DEBUG) System.out.println("Writing '" + file.getPath() + "to zip file");
        BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file));
        ZipEntry zipEntry = new ZipEntry(file.getName());
        zos.putNextEntry(zipEntry);
        BufferedOutputStream zipInBuffer = new BufferedOutputStream(zos);
        int length;
        while ((length = fis.read(buffer)) >= 0) {
            zipInBuffer.write(buffer, 0, length);
        }
        zos.closeEntry();
        fis.close();
    }

  /*  public static void copyDBFile(Context context, String databaseName) {
        try {
            File database = new File("/data/data/" + context.getPackageName() + "/databases/" + databaseName);
            File copyDataBase = new File(Storages.getTempSmartGalleryDir(context) + "/" + databaseName);
            copyFile(database, copyDataBase);
            if (!database.exists() || !copyDataBase.exists()) {
                Log.e(TAG, " FILE NOT FOUNT ORIGINAL = " + database.getAbsoluteFile() + " COPY = " + copyDataBase.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
