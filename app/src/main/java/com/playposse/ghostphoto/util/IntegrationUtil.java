package com.playposse.ghostphoto.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.playposse.ghostphoto.data.GhostPhotoContract;

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

    public static void shareSelectedPhotos(Context context, long photoShootIndex)
            throws URISyntaxException {

        ContentResolver contentResolver = context.getContentResolver();

        // Query content provider for selected photos.
        String whereClause = GhostPhotoContract.PhotoTable.SHOOT_ID_COLUMN + " = " + photoShootIndex
                + " and " + GhostPhotoContract.PhotoTable.IS_SELECTED_COLUMN;
        Cursor cursor = contentResolver.query(
                GhostPhotoContract.PhotoTable.CONTENT_URI,
                new String[]{GhostPhotoContract.PhotoTable.FILE_URI_COLUMN},
                whereClause,
                null,
                GhostPhotoContract.PhotoTable.ID_COLUMN + " asc");

        if (cursor == null) {
            Log.e(LOG_TAG, "onShareClicked: Cursor was unexpectedly null!");
            return;
        }

        try {
            // Prepare share intent.
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
//        intent.putExtra(Intent.EXTRA_SUBJECT, "Here are some files.");
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
}
