package com.playposse.ghostphoto.activities.review;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.playposse.ghostphoto.ExtraConstants;
import com.playposse.ghostphoto.R;
import com.playposse.ghostphoto.activities.ParentActivity;
import com.playposse.ghostphoto.data.GhostPhotoContract.PhotoTable;
import com.playposse.ghostphoto.data.QueryUtil;
import com.playposse.ghostphoto.util.AnalyticsUtil;
import com.playposse.ghostphoto.util.AnalyticsUtil.AnalyticsCategory;
import com.playposse.ghostphoto.util.BitmapRotationUtil;
import com.playposse.ghostphoto.util.IntegrationUtil;
import com.playposse.ghostphoto.util.ToastUtil;
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
public class ViewPhotoActivity extends ParentActivity implements PhotoSelectionChangeListener {

    private static final String LOG_TAG = ViewPhotoActivity.class.getSimpleName();

    private ViewPhotoContainerFragment photoContainerFragment;
    private ImageButton selectButton;
    private ImageButton rotateButton;
    private ImageButton editInButton;
    private ImageButton shareButton;
    private ImageButton deleteButton;

    private long photoShootId;
    private long initialPhotoId;
    private long photoId;
    private Boolean isSelected = null;
    private File photoFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_photo);

        initActionBar();

        photoShootId = ExtraConstants.getPhotoShootIndex(getIntent());
        initialPhotoId = ExtraConstants.getPhotoIndex(getIntent());

        selectButton = (ImageButton) findViewById(R.id.selectButton);
        rotateButton = (ImageButton) findViewById(R.id.rotateButton);
        editInButton = (ImageButton) findViewById(R.id.editInButton);
        shareButton = (ImageButton) findViewById(R.id.shareButton);
        deleteButton = (ImageButton) findViewById(R.id.deleteButton);

        photoContainerFragment =
                ViewPhotoContainerFragment.newInstance(photoShootId, initialPhotoId);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragmentContainer, photoContainerFragment)
                .commit();

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelected != null) {
                    isSelected = !isSelected;
                    QueryUtil.selectPhoto(getContentResolver(), photoId, isSelected);
                    refreshSelectButton();

                    AnalyticsUtil.reportEvent(
                            getApplication(),
                            AnalyticsCategory.selectPhoto,
                            Boolean.toString(isSelected));
                }
            }
        });

        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRotateClicked();
            }
        });

        editInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (photoFile != null) {
                    IntegrationUtil.openExternalActivityToEditPhoto(
                            getApplicationContext(),
                            photoFile);
                    AnalyticsUtil.reportEvent(getApplication(), AnalyticsCategory.editPhoto, "");
                }
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (photoFile != null) {
                    IntegrationUtil.sharePhoto(getApplicationContext(), photoFile);
                    AnalyticsUtil.reportEvent(getApplication(), AnalyticsCategory.sharePhoto, "");
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

    private void onConfirmedDelete() {
        QueryUtil.deletePhoto(getContentResolver(), photoId);

        AnalyticsUtil.reportEvent(getApplication(), AnalyticsCategory.deletePhoto, "");
    }

    @Override
    public void onPhotoShown(long photoId, boolean isSelected, Uri photoUri) {
        this.photoId = photoId;
        this.isSelected = isSelected;

        refreshSelectButton();

        try {
            this.photoFile = new File(new java.net.URI(photoUri.toString()));
        } catch (URISyntaxException ex) {
            Log.e(LOG_TAG, "onPhotoShown: Failed to create photo File object", ex);
        }
    }

    private void refreshSelectButton() {
        if (isSelected) {
            selectButton.setImageResource(R.drawable.ic_select_off);
        } else {
            selectButton.setImageResource(R.drawable.ic_select);
        }
    }

    private void onRotateClicked() {
        if (photoFile != null) {
            ToastUtil.sendShortToast(ViewPhotoActivity.this, R.string.rotation_toast);

            BitmapRotationUtil.rotate(
                    photoFile, new BitmapRotationUtil.RotationCallback() {
                        @Override
                        public void onRotated(File newFile) {
                            onRotateComplete(newFile);
                        }
                    });

            AnalyticsUtil.reportEvent(getApplication(), AnalyticsCategory.rotatePhoto, "");
        }
    }

    private void onRotateComplete(File newFile) {
        Uri photoUri = Uri.fromFile(newFile);

        ContentValues contentValues = new ContentValues();
        contentValues.put(PhotoTable.FILE_URI_COLUMN, photoUri.toString());

        String where = PhotoTable.ID_COLUMN + " = " + photoId;

        getContentResolver().update(PhotoTable.CONTENT_URI, contentValues, where, null);
    }
}
