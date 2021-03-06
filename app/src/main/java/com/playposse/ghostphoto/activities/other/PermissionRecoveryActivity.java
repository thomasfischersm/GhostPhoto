package com.playposse.ghostphoto.activities.other;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.playposse.ghostphoto.R;
import com.playposse.ghostphoto.activities.ParentActivity;
import com.playposse.ghostphoto.activities.camera.PhotoActivity;
import com.playposse.ghostphoto.constants.PhotoFileConversions;
import com.playposse.ghostphoto.util.PermissionUtil;
import com.playposse.ghostphoto.util.ToastUtil;

/**
 * An {@link Activity} that tries to guide the user through handling photo directory creation
 * errors.
 */
public class PermissionRecoveryActivity extends ParentActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 2;
    private static final int REQUEST_STORAGE_PERMISSION = 3;

    private LinearLayout requestPermissionLayout;
    private TextView grantCameraPermissionLabel;
    private TextView grantCameraPermissionLink;
    private TextView grantStoragePermissionLabel;
    private TextView grantStoragePermissionLink;
    private LinearLayout directoryCreationErrorLayout;
    private TextView directoryCreationErrorMessageTextView;
    private Button createDirectoryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.permission_recovery_activity);

        initActionBar();

        requestPermissionLayout = findViewById(R.id.requestPermissionLayout);
        grantCameraPermissionLabel = findViewById(R.id.grantCameraPermissionLabel);
        grantCameraPermissionLink = findViewById(R.id.grantCameraPermissionLink);
        grantStoragePermissionLabel = findViewById(R.id.grantStoragePermissionLabel);
        grantStoragePermissionLink = findViewById(R.id.grantStoragePermissionLink);
        directoryCreationErrorLayout = findViewById(R.id.directoryCreationErrorLayout);
        directoryCreationErrorMessageTextView = findViewById(R.id.directoryCreationErrorMessageTextView);
        createDirectoryButton = findViewById(R.id.createDirectoryButton);

        directoryCreationErrorMessageTextView.setText(getString(
                R.string.directory_creation_error_message,
                PhotoFileConversions.getPhotoDir().getAbsolutePath()));

        updateLayoutVisibility();

        grantCameraPermissionLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRequestCameraPermissionClicked();
            }
        });

        grantStoragePermissionLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRequestStoragePermissionClicked();
            }
        });

        createDirectoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateDirectoryClicked();
            }
        });
    }

    private void updateLayoutVisibility() {
        boolean hasStoragePermission = PermissionUtil.hasStoragePermission(this);
        boolean hasCameraPermission = PermissionUtil.hasCameraPermission(this);
        if (!hasStoragePermission || !hasCameraPermission) {
            requestPermissionLayout.setVisibility(View.VISIBLE);
            directoryCreationErrorLayout.setVisibility(View.INVISIBLE);
            grantCameraPermissionLabel
                    .setVisibility(hasCameraPermission ? View.GONE : View.VISIBLE);
            grantCameraPermissionLink
                    .setVisibility(hasCameraPermission ? View.GONE : View.VISIBLE);
            grantStoragePermissionLabel
                    .setVisibility(hasStoragePermission ? View.GONE : View.VISIBLE);
            grantStoragePermissionLink
                    .setVisibility(hasStoragePermission ? View.GONE : View.VISIBLE);
        } else if (!PermissionUtil.doesPhotoDirectoryExist()) {
            requestPermissionLayout.setVisibility(View.INVISIBLE);
            directoryCreationErrorLayout.setVisibility(View.VISIBLE);
        } else {
            // All problems are resolved. Send the user back to the camera.
            startPhotoActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        updateLayoutVisibility();
    }

    private void startPhotoActivity() {
        startActivity(new Intent(this, PhotoActivity.class));
    }

    private void onRequestCameraPermissionClicked() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
    }

    private void onRequestStoragePermissionClicked() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_STORAGE_PERMISSION);
    }

    private void onCreateDirectoryClicked() {
        if (PermissionUtil.doesPhotoDirectoryExist() || attemptToCreatePhotoDirectory()) {
            startPhotoActivity();
        } else {
            ToastUtil.sendShortToast(this, R.string.failed_to_create_directory_toast);
        }
    }

    private boolean attemptToCreatePhotoDirectory() {
        return PhotoFileConversions.getPhotoDir().mkdir();
    }
}
