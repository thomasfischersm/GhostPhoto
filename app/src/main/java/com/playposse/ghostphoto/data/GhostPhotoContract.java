package com.playposse.ghostphoto.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * A contract class for the {@link GhostPhotoContentProvider}.
 */
public class GhostPhotoContract {

    public static final String AUTHORITY = "com.playposse.ghostphoto.provider";

    private static final String CONTENT_SCHEME = "content";

    private static Uri createContentUri(String path) {
        return new Uri.Builder()
                .scheme(CONTENT_SCHEME)
                .encodedAuthority(AUTHORITY)
                .appendPath(path)
                .build();
    }

    /**
     * Stores meta information about a photo shoot. A photo shoot is defined as the photos taken
     * between when the start and stop button are pressed.
     */
    public static final class PhotoShootTable implements BaseColumns {

        public static final String PATH = "photoShoot";
        public static final Uri CONTENT_URI = createContentUri(PATH);
        public static final String TABLE_NAME = "photo_shoot";

        public static final String ID_COLUMN = _ID;
        public static final String START_TIME_COLUMN = "start_time";
        public static final String PHOTO_COUNT_COLUMN = "photo_count";
        public static final String STATE_COLUMN = "state";
        public static final String FIRST_PHOTO_URI_COLUMN = "first_photo_uri";

        public static final int ACTIVE_STATE = 1;
        public static final int COMPLETED_STATE = 2;

        public static final String[] COLUMN_NAMES = new String[]{
                ID_COLUMN,
                START_TIME_COLUMN,
                PHOTO_COUNT_COLUMN,
                STATE_COLUMN};

        public static final String[] SELECT_COLUMN_NAMES = new String[]{
                ID_COLUMN,
                START_TIME_COLUMN,
                PHOTO_COUNT_COLUMN,
                STATE_COLUMN,
                FIRST_PHOTO_URI_COLUMN};

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE photo_shoot "
                        + "(_id INTEGER PRIMARY KEY, "
                        + "start_time DATETIME DEFAULT CURRENT_TIMESTAMP, "
                        + "photo_count INTEGER DEFAULT 0,"
                        + "state DEFAULT 1)";

        public static final String SQL_SELECT =
                "select a.*, b.file_uri "
                        + "from photo_shoot a "
                        + "inner join photo b on (a._id = b.shoot_id) "
                        + "where b._id = (select max(_id) from photos where shoot_id=a._id) "
                        + "order by _id desc";
    }

    /**
     * Stores meta information about a photo.
     */
    public static final class PhotoTable implements BaseColumns {

        public static final String PATH = "photo";
        public static final Uri CONTENT_URI = createContentUri(PATH);
        public static final String TABLE_NAME = "photo";

        public static final String ID_COLUMN = _ID;
        public static final String SHOOT_ID_COLUMN = "shoot_id";
        public static final String FILE_URI_COLUMN = "file_uri";
        public static final String IS_SELECTED_COLUMN = "is_selected";
        public static final String CREATED_COLUMN = "created";

        public static final String[] COLUMN_NAMES = new String[]{
                ID_COLUMN,
                SHOOT_ID_COLUMN,
                FILE_URI_COLUMN,
                IS_SELECTED_COLUMN,
                CREATED_COLUMN};

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE photo "
                        + "(_id INTEGER PRIMARY KEY, "
                        + "shoot_id INTEGER, "
                        + "file_uri TEXT, "
                        + "is_selected BOOLEAN DEFAULT FALSE, "
                        + "created DATETIME DEFAULT CURRENT_TIMESTAMP, "
                        + "FOREIGN KEY(shoot_id) REFERENCES photo_shoot(id));";

        static final String SQL_SELECT_LAST_PHOTO =
                "SELECT * FROM photo photo order by _id desc limit 1;";
    }
}
