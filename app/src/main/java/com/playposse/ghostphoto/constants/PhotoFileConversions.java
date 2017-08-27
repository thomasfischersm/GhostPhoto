package com.playposse.ghostphoto.constants;

import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;

import java.io.File;


/**
 * A utility class that deals with the paths and URIs to the photos.
 */
public class PhotoFileConversions {

    private static final String DIR_NAME = "GhostPhoto";

    @Nullable
    private static File photoDir = null;

    public static File getPhotoDir() {
        if (photoDir == null) {
            File rootDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            photoDir = new File(rootDir, DIR_NAME);
        }
        return photoDir;
    }

    public static File toFile(String fileName) {
        return new File(getPhotoDir(), fileName);
    }

    public static Uri toUri(String fileName) {
        return toUri(toFile(fileName));
    }

    public static Uri toUri(File file) {
        return Uri.fromFile(file);
    }

    public static String toFileName(String uri) {
        return uri.substring(uri.lastIndexOf(File.separatorChar) + 1);
    }
}
