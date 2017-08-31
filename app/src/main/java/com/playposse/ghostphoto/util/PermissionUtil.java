package com.playposse.ghostphoto.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import com.playposse.ghostphoto.constants.PhotoFileConversions;

/**
 * A utility to check app relevant permissions easily.
 */
public class PermissionUtil {

    private PermissionUtil() {}


    public static boolean hasStoragePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean doesPhotoDirectoryExist() {
        return PhotoFileConversions.getPhotoDir().exists();
    }
}
