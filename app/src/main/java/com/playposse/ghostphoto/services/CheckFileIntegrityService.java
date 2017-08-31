package com.playposse.ghostphoto.services;

import android.app.IntentService;
import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.playposse.ghostphoto.GhostPhotoPreferences;
import com.playposse.ghostphoto.constants.PhotoFileConversions;
import com.playposse.ghostphoto.data.GhostPhotoContract.PhotoShootTable;
import com.playposse.ghostphoto.data.GhostPhotoContract.PhotoTable;
import com.playposse.ghostphoto.util.AnalyticsUtil;
import com.playposse.ghostphoto.util.AnalyticsUtil.AnalyticsCategory;
import com.playposse.ghostphoto.util.PermissionUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A {@link Service} that checks the integrity of the files:
 *
 * <ul>
 *     <li>It checks for photos in the apps' directory that are not registered in the database.</li>
 *     <li>It sends all the photos in the database to the device's media scanner again.</li>
 * </ul>
 */
public class CheckFileIntegrityService extends IntentService {

    private static final String LOG_TAG = CheckFileIntegrityService.class.getSimpleName();

    private static final String SERVICE_NAME = "CheckFileIntegrityService";

    public CheckFileIntegrityService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        long start = System.currentTimeMillis();

        try {
            if (!PermissionUtil.hasStoragePermission(getApplicationContext())
                    || !PermissionUtil.doesPhotoDirectoryExist()) {
                Log.d(LOG_TAG, "onHandleIntent: Storage permission or the photo directory are " +
                        "missing. Skipping file integry check.");
                return;
            }

            checkForOrphanFiles();
            sendAllPhotosToTheMediaScanner();
        } finally {
            long end = System.currentTimeMillis();

            AnalyticsUtil.reportEvent(getApplication(), AnalyticsCategory.fileIntegrityCheck, "");
            Log.i(LOG_TAG, "onHandleIntent: The file integrity check took " + (end - start) + "ms");
        }
    }

    private void checkForOrphanFiles() {
        // Query the db and directory for photos.
        List<String> dbFileNames = getPhotoFilesInDatabase();
        List<String> dirFileNames = getPhotoFilesInDirectory();

        // Find files that are not in the db.
        dirFileNames.removeAll(dbFileNames);

        // Create a new photo shoot for the orphaned photos.
        if (dirFileNames.size() > 0) {
            long shootId = createOrphanShoot(dirFileNames);

            GhostPhotoPreferences.setHasOrphanPhotoShootBeenCreated(getApplicationContext(), true);
            Log.d(LOG_TAG, "checkForOrphanFiles: Created orphan photo shoot: " + shootId);
        }
    }

    private void sendAllPhotosToTheMediaScanner() {
        List<String> dbFileNames = getPhotoFilesInDatabase();
        String[] paths = new String[dbFileNames.size()];

        for (int i = 0; i < dbFileNames.size(); i++) {
            String fileName = dbFileNames.get(i);
            paths[i] = PhotoFileConversions.toFile(fileName).getAbsolutePath();
        }

        MediaScannerConnection.scanFile(getApplicationContext(), paths, null, null);
    }

    private List<String> getPhotoFilesInDatabase() {
        Cursor cursor = getContentResolver().query(
                PhotoTable.CONTENT_URI,
                new String[]{PhotoTable.FILE_URI_COLUMN},
                null,
                null,
                null);

        if (cursor == null) {
            Log.e(LOG_TAG, "getPhotoFilesInDatabase: Got a null cursor!");
            return new ArrayList<>(0);
        }

        try {
            List<String> fileNames = new ArrayList<>(cursor.getCount());
            while (cursor.moveToNext()) {
                String uri = cursor.getString(0);
                String fileName = PhotoFileConversions.toFileName(uri);
                fileNames.add(fileName);
            }
            return fileNames;
        } finally {
            cursor.close();
        }
    }

    private List<String> getPhotoFilesInDirectory() {
        File photoDir = PhotoFileConversions.getPhotoDir();
        String[] fileNamesArray = photoDir.list();
        return new ArrayList<>(Arrays.asList(fileNamesArray));
    }

    private long createOrphanShoot(List<String> dirFileNames) {
        long shootId = createOrphanPhotoShoot();

        createOrphanPhotos(dirFileNames, shootId);

        return shootId;
    }

    private long createOrphanPhotoShoot() {
        ContentValues values = new ContentValues();
        values.put(PhotoShootTable.STATE_COLUMN, PhotoShootTable.COMPLETED_STATE);
        Uri photoShootUri = getContentResolver().insert(PhotoShootTable.CONTENT_URI, values);
        return ContentUris.parseId(photoShootUri);
    }

    private void createOrphanPhotos(List<String> dirFileNames, long shootId) {
        for (String fileName : dirFileNames) {
            String photoUri = PhotoFileConversions.toUri(fileName).toString();

            ContentValues contentValues = new ContentValues();
            contentValues.put(PhotoTable.FILE_URI_COLUMN, photoUri);
            contentValues.put(PhotoTable.SHOOT_ID_COLUMN, shootId);
            getContentResolver().insert(PhotoTable.CONTENT_URI, contentValues);
        }
    }
}
