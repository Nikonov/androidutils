package com.appmobileos.android.utils.file;

import java.io.File;
import java.io.FileFilter;

/**
 * File filters used to search for specific file types in folder
 */
public class FileFilters {
    /**
     * Filter for apk file
     */
    public static class ApkFileFilter implements FileFilter {
        private final String[] okFileExtensions = new String[]{"apk"};

        public boolean accept(File file) {
            for (String extension : okFileExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Filter for image file
     * Support file with extensions: jpg,png,gif,jpeg
     */
    public static class ImageFileFilter implements FileFilter {
        private final String[] okFileExtensions = new String[]{"jpg", "png", "gif", "jpeg"};

        public boolean accept(File file) {
            for (String extension : okFileExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }
    }
    /**
     * Filter for folder file
     */
    public static class FolderFilter implements FileFilter {
        public boolean accept(File file) {
            return file.isDirectory();
        }
    }
}
