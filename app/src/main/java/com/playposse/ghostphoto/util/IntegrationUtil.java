package com.playposse.ghostphoto.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;

/**
 * A utility to help integrating with external apps to show/edit photos.
 */
public final class IntegrationUtil {

    private static final String LOG_TAG = IntegrationUtil.class.getSimpleName();

    private IntegrationUtil() {
    }

    public static void openExternalActivityToShowPhoto(Context context, File photoFile) {
        try {
            Uri uri = FileProvider.getUriForFile(
                    context,
                    "com.playposse.ghostphoto",
                    photoFile);
            Log.i(LOG_TAG, "Starting intent to view " + uri);

            Intent intent = new Intent()
                    .setAction(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "image/jpeg")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            PackageManager packageManager = context.getPackageManager();
            if (intent.resolveActivity(packageManager) != null) {
                context.startActivity(intent);
            }
        } catch (Throwable ex) {
            Log.e(LOG_TAG, "Failed to view photo in photoviewer");
            throw ex;
        }
    }
}
