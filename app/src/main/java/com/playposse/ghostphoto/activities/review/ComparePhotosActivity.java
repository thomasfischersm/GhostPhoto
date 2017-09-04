package com.playposse.ghostphoto.activities.review;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.playposse.ghostphoto.ExtraConstants;
import com.playposse.ghostphoto.R;
import com.playposse.ghostphoto.activities.ParentActivity;
import com.playposse.ghostphoto.data.GhostPhotoContract.PhotoTable;
import com.playposse.ghostphoto.data.QueryUtil;
import com.playposse.ghostphoto.util.AnalyticsUtil;
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
    private CheckBox keepFirstCheckBox;
    private ImageView secondImageView;
    private CheckBox keepSecondCheckBox;

    private long firstPhotoId;
    private Boolean isFirstPhotoSelected;
    private long secondPhotoId;
    private Boolean isSecondPhotoSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.compare_photos_title);

        setContentView(R.layout.activity_compare_photos);

        initActionBar();

        firstImageView = findViewById(R.id.firstImageView);
        keepFirstCheckBox = findViewById(R.id.keepFirstCheckBox);
        secondImageView = findViewById(R.id.secondImageView);
        keepSecondCheckBox = findViewById(R.id.keepSecondCheckBox);

        long[] photoIndexes = ExtraConstants.getPhotoIndexes(getIntent());
        if ((photoIndexes == null) || (photoIndexes.length != 2)) {
            Log.e(LOG_TAG, "onCreate: Received unexpected photo indexes from the intent: "
                    + photoIndexes);
            return;
        }
        firstPhotoId = photoIndexes[0];
        secondPhotoId = photoIndexes[1];

        getLoaderManager().initLoader(LOADER_ID, null, this);

        keepFirstCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(LOG_TAG, "onCheckedChanged: first check listener triggered");
                isFirstPhotoSelected =
                        ComparePhotosActivity.this.onCheckedChanged(
                                keepFirstCheckBox,
                                isChecked,
                                isFirstPhotoSelected,
                                firstPhotoId);
            }
        });

        keepSecondCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(LOG_TAG, "onCheckedChanged: second check listener triggered");
                isSecondPhotoSelected =
                        ComparePhotosActivity.this.onCheckedChanged(
                                keepSecondCheckBox,
                                isChecked,
                                isSecondPhotoSelected,
                                secondPhotoId);
            }
        });
    }

    private Boolean onCheckedChanged(
            final CheckBox checkBox,
            boolean shouldBeCheck,
            Boolean isCurrentlyChecked,
            final long photoId) {

        Log.d(LOG_TAG, "onCheckedChanged: checkbox view " + checkBox.getId()
                + ", shouldBeCheck" + shouldBeCheck
                + ", isCurrentlyChecked" + isCurrentlyChecked);

        if ((isCurrentlyChecked != null) && (isCurrentlyChecked != shouldBeCheck)) {
            Log.d(LOG_TAG, "onCheckedChanged: Updating check state");
            isCurrentlyChecked = shouldBeCheck;
            final Boolean isFinalChecked = isCurrentlyChecked;
            checkBox.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(LOG_TAG, "onCheckedChanged: Executing check change " + photoId
                            + " set to " + isFinalChecked);
                    QueryUtil.selectPhoto(getContentResolver(), photoId, isFinalChecked);
                    checkBox.setChecked(isFinalChecked);
                }
            });

            AnalyticsUtil.reportEvent(
                    getApplication(),
                    AnalyticsUtil.AnalyticsCategory.selectPhoto,
                    Boolean.toString(isCurrentlyChecked));
        }
        return isCurrentlyChecked;
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
        Log.d(LOG_TAG, "onLoadFinished: OnLoadFinished triggered");
        SmartCursor smartCursor = new SmartCursor(cursor, PhotoTable.COLUMN_NAMES);
        isFirstPhotoSelected =
                loadPhoto(cursor, smartCursor, 0, firstImageView, keepFirstCheckBox);
        isSecondPhotoSelected =
                loadPhoto(cursor, smartCursor, 1, secondImageView, keepSecondCheckBox);
    }

    private Boolean loadPhoto(
            Cursor cursor,
            SmartCursor smartCursor,
            int position,
            ImageView imageView,
            final CheckBox checkBox) {

        if (cursor.moveToPosition(position)) {
            final String photoUri =
                    smartCursor.getString(PhotoTable.FILE_URI_COLUMN);
            Uri contentUri = Uri.parse(photoUri);
            Glide.with(this)
                    .load(contentUri)
                    .into(imageView);

            final boolean isSelected = smartCursor.getBoolean(PhotoTable.IS_SELECTED_COLUMN);
            checkBox.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(LOG_TAG, "loadPhoto: Executing check change");
                    checkBox.setChecked(isSelected);
                }
            });
            Log.d(LOG_TAG, "loadPhoto: view " + imageView.getId() + ", position " + position
                    + ", checkbox view " + checkBox.getId() + ", " + isSelected
                    + " photoId " + smartCursor.getLong(PhotoTable.ID_COLUMN));
            return isSelected;
        }

        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        firstImageView.setImageBitmap(null);
    }
}
