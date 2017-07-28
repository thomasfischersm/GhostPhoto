package com.playposse.ghostphoto.activities.review;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.playposse.ghostphoto.ExtraConstants;
import com.playposse.ghostphoto.R;
import com.playposse.ghostphoto.activities.ParentActivity;
import com.playposse.ghostphoto.data.GhostPhotoContract.PhotoTable;
import com.playposse.ghostphoto.data.QueryUtil;
import com.playposse.ghostphoto.util.IntegrationUtil;
import com.playposse.ghostphoto.util.SmartCursor;
import com.playposse.ghostphoto.util.view.DialogUtil;

import java.io.File;
import java.net.URISyntaxException;

/**
 * An {@link Activity} to view a single photo.
 * <p>
 * <p>Android has an {@link Intent} to view a photo. However, the resulting system activity does
 * not allow to launch Snapseed or similar through an "Edit In" action. Being able to have a
 * workflow to edit the photo is essential.
 */
public class ViewPhotoActivity
        extends ParentActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ViewPhotoActivity.class.getSimpleName();

    private static final int LOADER_ID = 6;

    private ImageView photoImageView;
    private ImageButton selectButton;
    private ImageButton editInButton;
    private ImageButton shareButton;
    private ImageButton deleteButton;

    private long photoId;
    private Boolean isSelected = null;
    private File photoFile = null;

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
                    finish();
                }
            }
        });

        editInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (photoFile != null) {
                    IntegrationUtil.openExternalActivityToEditPhoto(
                            getApplicationContext(),
                            photoFile);
                }
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (photoFile != null) {
                    IntegrationUtil.sharePhoto(getApplicationContext(), photoFile);
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.confirm(
                        ViewPhotoActivity.this,
                        R.string.confirm_delete_photo_dialog,
                        R.string.confirm_delete_photo_dialog_body,
                        R.string.delete_button_label,
                        R.string.cancel_button_label,
                        new Runnable() {
                            @Override
                            public void run() {
                                onConfirmedDelete();
                            }
                        });
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

            try {
                photoFile = new File(new java.net.URI(photoUri));
            } catch (URISyntaxException ex) {
                Log.d(LOG_TAG, "onLoadFinished: Failed to create photo file for editing.", ex);
            }
        } else {
            Log.e(LOG_TAG, "onLoadFinished: Failed to load photo cursor");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        photoImageView.setImageBitmap(null);
    }

    private void onConfirmedDelete() {
        QueryUtil.deletePhoto(getContentResolver(), photoId);
        finish();
    }
}
