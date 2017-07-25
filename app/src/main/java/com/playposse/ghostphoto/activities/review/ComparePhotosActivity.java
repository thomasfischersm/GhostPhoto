package com.playposse.ghostphoto.activities.review;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.playposse.ghostphoto.ExtraConstants;
import com.playposse.ghostphoto.R;
import com.playposse.ghostphoto.activities.ParentActivity;
import com.playposse.ghostphoto.data.GhostPhotoContract.PhotoTable;
import com.playposse.ghostphoto.util.SmartCursor;

/**
 * An {@link Activity} that compares two photos.
 */
public class ComparePhotosActivity
        extends ParentActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ComparePhotosActivity.class.getSimpleName();

    private static final int LOADER_ID = 5;

    private ImageView firstImageView;
    private ImageView secondImageView;

    private long firstPhotoId;
    private long secondPhotoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.compare_photos_title);

        setContentView(R.layout.activity_compare_photos);

        initActionBar();

        firstImageView = (ImageView) findViewById(R.id.firstImageView);
        secondImageView = (ImageView) findViewById(R.id.secondImageView);

        long[] photoIndexes = ExtraConstants.getPhotoIndexes(getIntent());
        if ((photoIndexes == null) || (photoIndexes.length != 2)) {
            Log.e(LOG_TAG, "onCreate: Received unexpected photo indexes from the intent: "
                    + photoIndexes);
            return;
        }
        firstPhotoId = photoIndexes[0];
        secondPhotoId = photoIndexes[1];

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String whereClause = PhotoTable.ID_COLUMN + " = " + firstPhotoId
                + " or " + PhotoTable.ID_COLUMN + " = " + secondPhotoId;
        return new CursorLoader(
                this,
                PhotoTable.CONTENT_URI,
                PhotoTable.COLUMN_NAMES,
                whereClause,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        SmartCursor smartCursor = new SmartCursor(cursor, PhotoTable.COLUMN_NAMES);
        loadPhoto(cursor, smartCursor, 0, firstImageView);
        loadPhoto(cursor, smartCursor, 1, secondImageView);
    }

    private void loadPhoto(
            Cursor cursor,
            SmartCursor smartCursor,
            int position,
            ImageView imageView) {

        if (cursor.moveToPosition(position)) {
            final String photoUri =
                    smartCursor.getString(PhotoTable.FILE_URI_COLUMN);
            Uri contentUri = Uri.parse(photoUri);
            Glide.with(this)
                    .load(contentUri)
                    .into(imageView);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        firstImageView.setImageBitmap(null);
    }
}
