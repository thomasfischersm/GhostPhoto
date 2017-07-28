package com.playposse.ghostphoto.activities.review;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.playposse.ghostphoto.ExtraConstants;
import com.playposse.ghostphoto.R;
import com.playposse.ghostphoto.activities.ParentActivity;
import com.playposse.ghostphoto.data.GhostPhotoContract.PhotoTable;
import com.playposse.ghostphoto.data.QueryUtil;
import com.playposse.ghostphoto.util.SmartCursor;

/**
 * An {@link Activity} to view a single photo.
 *
 * <p>Android has an {@link Intent} to view a photo. However, the resulting system activity does
 * not allow to launch Snapseed or similar through an "Edit In" action. Being able to have a
 * workflow to edit the photo is essential.
 */
public class ViewPhotoActivity
        extends ParentActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 6;

    private ImageView photoImageView;
    private ImageButton selectButton;
    private ImageButton editInButton;
    private ImageButton shareButton;
    private ImageButton deleteButton;

    private long photoId;
    private Boolean isSelected = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_photo);

        initActionBar();

        photoId = ExtraConstants.getPhotoIndex(getIntent());

        photoImageView = (ImageView) findViewById(R.id.photoImageView);
        selectButton = (ImageButton) findViewById(R.id.selectButton);
        editInButton = (ImageButton) findViewById(R.id.editInButton);
        shareButton = (ImageButton) findViewById(R.id.shareButton);
        deleteButton = (ImageButton) findViewById(R.id.deleteButton);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelected != null) {
                    QueryUtil.selectPhoto(getContentResolver(), photoId, !isSelected);
                }
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String whereClause = PhotoTable.ID_COLUMN + " = " + photoId;
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

        if (cursor.moveToFirst()) {
            String photoUri = smartCursor.getString(PhotoTable.FILE_URI_COLUMN);
            isSelected = smartCursor.getBoolean(PhotoTable.IS_SELECTED_COLUMN);

            Uri contentUri = Uri.parse(photoUri);
            Glide.with(this)
                    .load(contentUri)
                    .into(photoImageView);

            if (isSelected) {
                selectButton.setImageResource(R.drawable.ic_select_off);
            } else {
                selectButton.setImageResource(R.drawable.ic_select);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        photoImageView.setImageBitmap(null);
    }
}
