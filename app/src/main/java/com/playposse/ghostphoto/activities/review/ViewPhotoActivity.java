package com.playposse.ghostphoto.activities.review;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.playposse.ghostphoto.ExtraConstants;
import com.playposse.ghostphoto.R;
import com.playposse.ghostphoto.activities.ParentActivity;
import com.playposse.ghostphoto.data.QueryUtil;
import com.playposse.ghostphoto.util.IntegrationUtil;
import com.playposse.ghostphoto.util.view.DialogUtil;

import java.io.File;

/**
 * An {@link Activity} to view a single photo.
 * <p>
 * <p>Android has an {@link Intent} to view a photo. However, the resulting system activity does
 * not allow to launch Snapseed or similar through an "Edit In" action. Being able to have a
 * workflow to edit the photo is essential.
 */
public class ViewPhotoActivity extends ParentActivity {

    private static final String LOG_TAG = ViewPhotoActivity.class.getSimpleName();

    private ViewPhotoContainerFragment photoContainerFragment;
    private ImageButton selectButton;
    private ImageButton editInButton;
    private ImageButton shareButton;
    private ImageButton deleteButton;

    private long photoShootId;
    private long photoId;
    private Boolean isSelected = null;
    private File photoFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_photo);

        initActionBar();

        photoShootId = ExtraConstants.getPhotoShootIndex(getIntent());
        photoId = ExtraConstants.getPhotoIndex(getIntent());

        selectButton = (ImageButton) findViewById(R.id.selectButton);
        editInButton = (ImageButton) findViewById(R.id.editInButton);
        shareButton = (ImageButton) findViewById(R.id.shareButton);
        deleteButton = (ImageButton) findViewById(R.id.deleteButton);

        photoContainerFragment = ViewPhotoContainerFragment.newInstance(photoShootId, photoId);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragmentContainer, photoContainerFragment)
                .commit();

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

    private void onConfirmedDelete() {
        QueryUtil.deletePhoto(getContentResolver(), photoId);
        finish();
    }
}
