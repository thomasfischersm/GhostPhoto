package com.playposse.ghostphoto.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.playposse.ghostphoto.data.GhostPhotoContract.PhotoShootTable;
import com.playposse.ghostphoto.data.GhostPhotoContract.PhotoTable;
import com.playposse.ghostphoto.util.SmartCursor;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * A helper that scans all photo files.
 *
 * <p>TODO: Convert this to no longer use the database directly.
 */
class ScanPhotoFilesHelper {

    private static final String LOG_TAG = ScanPhotoFilesHelper.class.getSimpleName();

    private static final String SQL_SELECT_EMPTY_PHOTO_SHOOT =
            "select "
                    + "a._id "
                    + "from photo_shoot a "
                    + "where (select count(*) from photo where shoot_id=a._id) = 0";

    static int scan(SQLiteDatabase database, ContentResolver contentResolver) {
        Map<Long, Uri> photoUris = listPhotoUris(database);
        int deletedPhotoCount = deleteNonexistentPhotos(photoUris, database);
        int deletedPhotoShootCount = deleteEmptyPhotoShoots(database);

        contentResolver.notifyChange(PhotoShootTable.CONTENT_URI, null);
        contentResolver.notifyChange(PhotoTable.CONTENT_URI, null);

        Log.d(LOG_TAG, "scan: Finished scanning photo files. Photos deleted: " + deletedPhotoCount
                + " Photo shoots deleted: " + deletedPhotoShootCount);

        return deletedPhotoCount;
    }

    private static Map<Long, Uri> listPhotoUris(SQLiteDatabase database) {
        String[] columnNames = new String[]{PhotoTable.ID_COLUMN, PhotoTable.FILE_URI_COLUMN};

        Cursor cursor =
                database.query(PhotoTable.TABLE_NAME, columnNames, null, null, null, null, null);
        SmartCursor smartCursor = new SmartCursor(cursor, columnNames);

        try {
            Map<Long, Uri> photoUris = new HashMap<>(cursor.getCount());
            while (cursor.moveToNext()) {
                long id = smartCursor.getLong(PhotoTable.ID_COLUMN);
                Uri uri = smartCursor.getUri(PhotoTable.FILE_URI_COLUMN);
                photoUris.put(id, uri);
            }

            return photoUris;
        } finally {
            cursor.close();
        }
    }

    private static int deleteNonexistentPhotos(Map<Long, Uri> photoUris, SQLiteDatabase database) {
        try {
            int deleteCount = 0;

            for (Map.Entry<Long, Uri> entry : photoUris.entrySet()) {
                long id = entry.getKey();
                String photoUriStr = entry.getValue().toString();
                java.net.URI photoUri = new java.net.URI(photoUriStr);

                File file = new File(photoUri);
                if (!file.exists()) {
                    // Remove the file from the db.
                    String whereClause = PhotoTable.ID_COLUMN + " = " + id;
                    deleteCount += database.delete(PhotoTable.TABLE_NAME, whereClause, null);
                }
            }

            return deleteCount;
        } catch (URISyntaxException e) {
            return -1;
        }
    }

    private static int deleteEmptyPhotoShoots(SQLiteDatabase database) {
        Cursor cursor = database.rawQuery(SQL_SELECT_EMPTY_PHOTO_SHOOT, null);

        try {
            int deleteCount = 0;

            while (cursor.moveToNext()) {
                // Delete the empty photo shoot.
                long id = cursor.getLong(0);
                String whereClause = PhotoShootTable.ID_COLUMN + " = " + id;
                deleteCount += database.delete(PhotoShootTable.TABLE_NAME, whereClause, null);
            }

            return deleteCount;
        } finally {
            cursor.close();
        }
    }
}
