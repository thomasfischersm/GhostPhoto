package com.playposse.ghostphoto.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.playposse.ghostphoto.data.GhostPhotoContract.AddPhotoAction;
import com.playposse.ghostphoto.data.GhostPhotoContract.DeleteAllAction;
import com.playposse.ghostphoto.data.GhostPhotoContract.DeleteUnselectedAction;
import com.playposse.ghostphoto.data.GhostPhotoContract.EndShootAction;
import com.playposse.ghostphoto.data.GhostPhotoContract.GetLatestPhotoAction;
import com.playposse.ghostphoto.data.GhostPhotoContract.PhotoShootTable;
import com.playposse.ghostphoto.data.GhostPhotoContract.PhotoTable;
import com.playposse.ghostphoto.data.GhostPhotoContract.ScanPhotoFilesAction;
import com.playposse.ghostphoto.data.GhostPhotoContract.StartShootAction;
import com.playposse.ghostphoto.util.DatabaseDumper;

/**
 * A {@link ContentProvider} to store informations about the photos that were taken.
 */
public class GhostPhotoContentProvider extends ContentProvider {

    private static final String LOG_TAG = GhostPhotoContentProvider.class.getSimpleName();

    private static final int PHOTO_SHOOT_TABLE_KEY = 1;
    private static final int PHOTO_TABLE_KEY = 2;
    private static final int START_SHOOT_ACTION_KEY = 3;
    private static final int END_SHOOT_ACTION_KEY = 4;
    private static final int ADD_PHOTO_KEY = 5;
    private static final int GET_LATEST_PHOTO_ACTION_KEY = 6;
    private static final int DELETE_ALL_ACTION_KEY = 7;
    private static final int DELETE_UNSELECTED_ACTION_KEY = 8;
    private static final int SCAN_PHOTO_FILE_ACTION_KEY = 9;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(GhostPhotoContract.AUTHORITY, PhotoShootTable.PATH, PHOTO_SHOOT_TABLE_KEY);
        uriMatcher.addURI(GhostPhotoContract.AUTHORITY, PhotoTable.PATH, PHOTO_TABLE_KEY);
        uriMatcher.addURI(GhostPhotoContract.AUTHORITY, StartShootAction.PATH, START_SHOOT_ACTION_KEY);
        uriMatcher.addURI(GhostPhotoContract.AUTHORITY, EndShootAction.PATH, END_SHOOT_ACTION_KEY);
        uriMatcher.addURI(GhostPhotoContract.AUTHORITY, AddPhotoAction.PATH, ADD_PHOTO_KEY);
        uriMatcher.addURI(GhostPhotoContract.AUTHORITY, GetLatestPhotoAction.PATH, GET_LATEST_PHOTO_ACTION_KEY);
        uriMatcher.addURI(GhostPhotoContract.AUTHORITY, DeleteAllAction.PATH, DELETE_ALL_ACTION_KEY);
        uriMatcher.addURI(GhostPhotoContract.AUTHORITY, DeleteUnselectedAction.PATH, DELETE_UNSELECTED_ACTION_KEY);
        uriMatcher.addURI(GhostPhotoContract.AUTHORITY, ScanPhotoFilesAction.PATH, SCAN_PHOTO_FILE_ACTION_KEY);
    }

    private GhostPhotoDatabaseHelper databaseHelper;

    @Override
    public boolean onCreate() {
        databaseHelper = new GhostPhotoDatabaseHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(
            @NonNull Uri uri,
            @Nullable String[] projection,
            @Nullable String selection,
            @Nullable String[] selectionArgs,
            @Nullable String sortOrder) {

        ContentResolver contentResolver = getContext().getContentResolver();
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        String tableName;
        Uri notificationUri = null;
        switch (uriMatcher.match(uri)) {
            case PHOTO_SHOOT_TABLE_KEY:
                Cursor cursor = database.rawQuery(PhotoShootTable.SQL_SELECT, null);
                cursor.setNotificationUri(contentResolver, PhotoShootTable.CONTENT_URI);
                return cursor;
            case PHOTO_TABLE_KEY:
                tableName = PhotoTable.TABLE_NAME;
                notificationUri = PhotoTable.CONTENT_URI;
                break;
            case GET_LATEST_PHOTO_ACTION_KEY:
                return getLastPhoto(contentResolver, database);
            default:
                return null;
        }

        Cursor cursor = database.query(
                tableName,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        if (notificationUri != null) {
            cursor.setNotificationUri(contentResolver, notificationUri);
        }
        return cursor;

    }

    @NonNull
    private Cursor getLastPhoto(ContentResolver contentResolver, SQLiteDatabase database) {
        Cursor cursor = database.rawQuery(PhotoTable.SQL_SELECT_LAST_PHOTO, null);
        cursor.setNotificationUri(contentResolver, PhotoTable.CONTENT_URI);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (getContext() == null) {
            Log.e(LOG_TAG, "insert: getContext was null!");
            return null;
        }

        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        ContentResolver contentResolver = getContext().getContentResolver();

        switch (uriMatcher.match(uri)) {
            case START_SHOOT_ACTION_KEY:
                return startShoot(database, contentResolver);
            case ADD_PHOTO_KEY:
                return addPhoto(values, database, contentResolver);
            default:
                return null;
        }
    }

    private Uri startShoot(SQLiteDatabase database, ContentResolver contentResolver) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PhotoShootTable.ID_COLUMN, (String) null);
        long id = database.insert(PhotoShootTable.TABLE_NAME, null, contentValues);
        contentResolver.notifyChange(PhotoShootTable.CONTENT_URI, null);
        Uri resultUri = ContentUris.withAppendedId(PhotoShootTable.CONTENT_URI, id);
        DatabaseDumper.dumpTables(databaseHelper);
        return resultUri;
    }

    /**
     * Adds a photo to the database. Only the FILE_URI_COLUMN has to be passed in the ContentValues.
     */
    private Uri addPhoto(
            ContentValues values,
            SQLiteDatabase database,
            ContentResolver contentResolver) {

        long shootId = getActivePhotoShootId(database, contentResolver);
        values.put(PhotoTable.SHOOT_ID_COLUMN, shootId);

        long id = database.insert(PhotoTable.TABLE_NAME, null, values);
        contentResolver.notifyChange(PhotoTable.CONTENT_URI, null);
        return ContentUris.withAppendedId(PhotoTable.CONTENT_URI, id);
    }

    private long getActivePhotoShootId(SQLiteDatabase database, ContentResolver contentResolver) {
        Cursor cursor = database.query(
                PhotoShootTable.TABLE_NAME,
                new String[]{PhotoShootTable._ID, PhotoShootTable.STATE_COLUMN},
                "state = " + PhotoShootTable.ACTIVE_STATE,
                null,
                null,
                null,
                PhotoShootTable.ID_COLUMN);

        try {
            if (cursor.moveToNext()) {
                int idColumnIndex = cursor.getColumnIndex(PhotoShootTable.ID_COLUMN);
                long id = cursor.getLong(idColumnIndex);
                Log.d(LOG_TAG, "getActivePhotoShootId: Found active shoot: " + id);
                return id;
            } else {
                Log.e(LOG_TAG, "getActivePhotoShootId: Failed to find active photo shoot!");
                // Try to recover by creating a new photo shoot.
                Uri uri = startShoot(database, contentResolver);
                long id = ContentUris.parseId(uri);
                return id;
            }
        } finally {
            cursor.close();
        }
    }

    @Override
    public int delete(
            @NonNull Uri uri,
            @Nullable String selection,
            @Nullable String[] selectionArgs) {

        if (getContext() == null) {
            Log.e(LOG_TAG, "delete: getContext was null!");
            return 0;
        }

        ContentResolver contentResolver = getContext().getContentResolver();
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case DELETE_ALL_ACTION_KEY:
                return deleteAll(database, contentResolver, selectionArgs);
            case DELETE_UNSELECTED_ACTION_KEY:
                return deleteUnselected(database, contentResolver, selectionArgs);
            default:
                return 0;

        }
    }

    /**
     * Deletes all photos in a photo shoot.
     *
     * <p>The photo shoot id should be passed as the first argument of the selectionArgs.
     */
    private int deleteAll(
            SQLiteDatabase database,
            ContentResolver contentResolver,
            String[] selectionArgs) {

        if (selectionArgs.length != 1) {
            Log.e(LOG_TAG, "deleteAll: The photo shoot id was not specified correctly.");
            return 0;
        }

        long shootId = Long.parseLong(selectionArgs[0]);

        // Delete physical photos.
        Cursor cursor = database.query(
                PhotoTable.TABLE_NAME,
                new String[]{PhotoTable.ID_COLUMN},
                "shoot_id = " + shootId,
                null,
                null,
                null,
                null);

        try {
            int idColumnIndex = cursor.getColumnIndex(PhotoTable.ID_COLUMN);
            while (cursor.moveToNext()) {
                String uri = cursor.getString(idColumnIndex);
                contentResolver.delete(Uri.parse(uri), null, null);
            }
        } finally {
            cursor.close();
        }

        // Delete all the photos.
        int photoDeleteCount = database.delete(
                PhotoTable.TABLE_NAME,
                "shoot_id=" + shootId,
                null);

        // Delete the photo shoot.
        int shootDeleteCount = database.delete(
                PhotoShootTable.TABLE_NAME,
                "_id=" + shootId,
                null);
        if (shootDeleteCount != 1) {
            Log.e(LOG_TAG, "deleteAll: Something went wrong deleting a photo shoot: "
                    + shootDeleteCount);
        }

        // Notify of changes.
        contentResolver.notifyChange(PhotoShootTable.CONTENT_URI, null);
        contentResolver.notifyChange(PhotoTable.CONTENT_URI, null);

        return photoDeleteCount;
    }

    private int deleteUnselected(
            SQLiteDatabase database,
            ContentResolver contentResolver,
            String[] selectionArgs) {

        if (selectionArgs.length != 1) {
            Log.e(LOG_TAG, "deleteSelected: The photo shoot id was not specified correctly.");
            return 0;
        }

        long shootId = Long.parseLong(selectionArgs[0]);

        // Delete physical photos.
        Cursor cursor = database.query(
                PhotoTable.TABLE_NAME,
                new String[]{PhotoTable.ID_COLUMN},
                "(shoot_id=" + shootId + ") and not(is_selected)",
                null,
                null,
                null,
                null);

        try {
            int idColumnIndex = cursor.getColumnIndex(PhotoTable.ID_COLUMN);
            while (cursor.moveToNext()) {
                String uri = cursor.getString(idColumnIndex);
                contentResolver.delete(Uri.parse(uri), null, null);
            }
        } finally {
            cursor.close();
        }

        // Delete selected photos.
        int photoDeleteCount = database.delete(
                PhotoTable.TABLE_NAME,
                "(shoot_id=" + shootId + ") and not(is_selected)",
                null);

        // Notify of changes.
        contentResolver.notifyChange(PhotoShootTable.CONTENT_URI, null);
        contentResolver.notifyChange(PhotoTable.CONTENT_URI, null);

        return photoDeleteCount;
    }

    @Override
    public int update(
            @NonNull Uri uri,
            @Nullable ContentValues values,
            @Nullable String selection,
            @Nullable String[] selectionArgs) {

        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        if (getContext() == null) {
            Log.e(LOG_TAG, "update: Context was unexpectedly null");
            return -1;
        }
        ContentResolver contentResolver = getContext().getContentResolver();

        switch (uriMatcher.match(uri)) {
            case PHOTO_TABLE_KEY:
                int photoCount =
                        database.update(PhotoTable.TABLE_NAME, values, selection, selectionArgs);
                contentResolver.notifyChange(PhotoTable.CONTENT_URI, null);
                return photoCount;
            case END_SHOOT_ACTION_KEY:
                return endShoot(database);
            case SCAN_PHOTO_FILE_ACTION_KEY:
                return ScanPhotoFilesHelper.scan(database, contentResolver);
            default:
                return 0;
        }
    }

    private int endShoot(SQLiteDatabase database) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(
                PhotoShootTable.STATE_COLUMN,
                Integer.toString(PhotoShootTable.COMPLETED_STATE));
        int rowCount = database.update(
                PhotoShootTable.TABLE_NAME,
                contentValues, "state=" + PhotoShootTable.ACTIVE_STATE,
                null);
        DatabaseDumper.dumpTables(databaseHelper);
        return rowCount;
    }
}

