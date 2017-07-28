package com.playposse.ghostphoto.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.AsyncTask;

import com.playposse.ghostphoto.data.GhostPhotoContract.PhotoTable;

/**
 * A utility for common operations on the {@link ContentProvider}.
 */
public final class QueryUtil {

    private QueryUtil() {}

    public static void selectPhoto(
            ContentResolver contentResolver,
            long photoId,
            boolean shouldBeSelected) {

        new SelectPhotoAsyncTask(contentResolver, photoId, shouldBeSelected).execute();
    }


    /**
     * An {@link AsyncTask} that selects or deselects a photo.
     */
    private static class SelectPhotoAsyncTask extends AsyncTask<Void, Void, Void> {

        private final ContentResolver contentResolver;
        private final long photoId;
        private final boolean shouldBeSelected;

        private SelectPhotoAsyncTask(
                ContentResolver contentResolver,
                long photoId,
                boolean shouldBeSelected) {

            this.contentResolver = contentResolver;
            this.photoId = photoId;
            this.shouldBeSelected = shouldBeSelected;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(PhotoTable.IS_SELECTED_COLUMN, shouldBeSelected);

            String whereClause = PhotoTable.ID_COLUMN + " = " + photoId;

            contentResolver.update(PhotoTable.CONTENT_URI, contentValues, whereClause, null);
            return null;
        }
    }
}
