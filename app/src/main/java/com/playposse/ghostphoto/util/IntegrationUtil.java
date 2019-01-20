package com.playposse.ghostphoto.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.playposse.ghostphoto.data.GhostPhotoContract.PhotoTable;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static com.playposse.ghostphoto.GhostPhotoApplication.FILE_PROVIDER_AUTHORITY;

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
                    "com.playposse.ghostphoto.fileprovider",
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

    public static void sharePhoto(Context context, File photoFile) {
        // Prepare share intent.
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        Uri publicUri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, photoFile);

        // Start the share activity.
        intent.putExtra(Intent.EXTRA_STREAM, publicUri);
        context.startActivity(Intent.createChooser(intent, null));
    }

    public static void shareSelectedPhotos(Context context, long photoShootIndex)
            throws URISyntaxException {

        ContentResolver contentResolver = context.getContentResolver();

        // Query content provider for selected photos.
        String whereClause = PhotoTable.SHOOT_ID_COLUMN + " = " + photoShootIndex
                + " and " + PhotoTable.IS_SELECTED_COLUMN;
        Cursor cursor = contentResolver.query(
                PhotoTable.CONTENT_URI,
                new String[]{PhotoTable.FILE_URI_COLUMN},
                whereClause,
                null,
                PhotoTable.ID_COLUMN + " asc");

        if (cursor.getCount() == 0) {
            // There are no selected photos. So, share all photos.
            cursor.close();

            cursor = contentResolver.query(
                    PhotoTable.CONTENT_URI,
                    new String[]{PhotoTable.FILE_URI_COLUMN},
                    PhotoTable.SHOOT_ID_COLUMN + " = " + photoShootIndex,
                    null,
                    PhotoTable.ID_COLUMN + " asc");
        }

        if (cursor == null) {
            Log.e(LOG_TAG, "onShareClicked: Cursor was unexpectedly null!");
            return;
        }

        try {
            // Prepare share intent.
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("image/jpeg");

            // Add selected photos to intent.
            ArrayList<Uri> files = new ArrayList<>();
            while (cursor.moveToNext()) {
                String photoUri = cursor.getString(0);
                File photoFile = new File(new java.net.URI(photoUri));
                Uri publicUri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, photoFile);
                files.add(publicUri);
            }

            // Start the share activity.
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
            context.startActivity(intent);
        } finally {
            cursor.close();
        }
    }

    public static void openExternalActivityToEditPhoto(Context context, File photoFile) {
        try {
            Uri uri = FileProvider.getUriForFile(
                    context,
                    "com.playposse.ghostphoto.fileprovider",
                    photoFile);
            Log.i(LOG_TAG, "Starting intent to view " + uri);

            Intent intent = new Intent()
                    .setAction(Intent.ACTION_EDIT)
                    .setDataAndType(uri, "image/jpeg")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            PackageManager packageManager = context.getPackageManager();
            if (intent.resolveActivity(packageManager) != null) {
                context.startActivity(Intent.createChooser(intent, null));
            }
        } catch (Throwable ex) {
            Log.e(LOG_TAG, "Failed to view photo in photoviewer");
            Crashlytics.logException(ex);
            throw ex;
        }
    }
}
