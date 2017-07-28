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

    public static void deletePhoto(ContentResolver contentResolver, long photoId) {
        new DeletePhotoAsyncTask(contentResolver, photoId).execute();
    }

    /**
     * An {@link AsyncTask} that selects or deselects a photo.
     */
    private static class SelectPhotoAsyncTask extends AsyncTask<Void, Void, Integer> {

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
        protected Integer doInBackground(Void... params) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(PhotoTable.IS_SELECTED_COLUMN, shouldBeSelected);

            String whereClause = PhotoTable.ID_COLUMN + " = " + photoId;

            return contentResolver.update(PhotoTable.CONTENT_URI, contentValues, whereClause, null);
        }
    }

    /**
     * An {@link AsyncTask} to delete a photo.
     */
    private static class DeletePhotoAsyncTask extends AsyncTask<Void, Void, Integer> {

        private final ContentResolver contentResolver;
        private final long photoId;

        private DeletePhotoAsyncTask(ContentResolver contentResolver, long photoId) {
            this.contentResolver = contentResolver;
            this.photoId = photoId;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            String where = PhotoTable.ID_COLUMN + "=" + photoId;
            return contentResolver.delete(PhotoTable.CONTENT_URI, where, null);
        }
    }
}
