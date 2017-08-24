package com.playposse.ghostphoto.activities.review;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;

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
    private CheckBox keepCheckBox;

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

        keepCheckBox = (CheckBox) findViewById(R.id.keepCheckBox);

        photoContainerFragment =
                ViewPhotoContainerFragment.newInstance(photoShootId, initialPhotoId);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragmentContainer, photoContainerFragment)
                .commit();

        keepCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if ((isSelected != null) && (isSelected != isChecked)) {
                    isSelected = isChecked;
                    QueryUtil.selectPhoto(getContentResolver(), photoId, isSelected);
                    refreshSelectButton();

                    AnalyticsUtil.reportEvent(
                            getApplication(),
                            AnalyticsCategory.selectPhoto,
                            Boolean.toString(isSelected));
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_photo_options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_photos_menu_item:
                onDeleteClicked();
                return true;
            case R.id.share_photos_menu_item:
                onShareClicked();
                return true;
            case R.id.edit_menu_item:
                onEditClicked();
                return true;
            case R.id.rotate_menu_item:
                onRotateClicked();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        keepCheckBox.setChecked(isSelected);
    }


    private void onEditClicked() {
        if (photoFile != null) {
            IntegrationUtil.openExternalActivityToEditPhoto(
                    ViewPhotoActivity.this,
                    photoFile);
            AnalyticsUtil.reportEvent(getApplication(), AnalyticsCategory.editPhoto, "");
        }
    }

    private void onShareClicked() {
        if (photoFile != null) {
            IntegrationUtil.sharePhoto(ViewPhotoActivity.this, photoFile);
            AnalyticsUtil.reportEvent(getApplication(), AnalyticsCategory.sharePhoto, "");
        }
    }

    private void onDeleteClicked() {
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

    private void onConfirmedDelete() {
        QueryUtil.deletePhoto(getContentResolver(), photoId);

        AnalyticsUtil.reportEvent(getApplication(), AnalyticsCategory.deletePhoto, "");
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
