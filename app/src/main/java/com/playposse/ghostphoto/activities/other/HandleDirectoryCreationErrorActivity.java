package com.playposse.ghostphoto.activities.other;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.playposse.ghostphoto.R;
import com.playposse.ghostphoto.activities.ParentActivity;
import com.playposse.ghostphoto.activities.camera.PhotoActivity;
import com.playposse.ghostphoto.util.ToastUtil;

import java.io.File;

import static com.playposse.ghostphoto.activities.camera.BasicPhotoFragment.DIR_NAME;

/**
 * An {@link Activity} that tries to guide the user through handling photo directory creation
 * errors.
 */
public class HandleDirectoryCreationErrorActivity extends ParentActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 2;

    private LinearLayout requestPermissionLayout;
    private Button requestPermissionButton;
    private LinearLayout directoryCreationErrorLayout;
    private TextView directoryCreationErrorMessageTextView;
    private Button createDirectoryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_handle_directory_creation_error);

        initActionBar();

        requestPermissionLayout = (LinearLayout) findViewById(R.id.requestPermissionLayout);
        requestPermissionButton = (Button) findViewById(R.id.requestPermissionButton);
        directoryCreationErrorLayout = (LinearLayout) findViewById(R.id.directoryCreationErrorLayout);
        directoryCreationErrorMessageTextView = (TextView) findViewById(R.id.directoryCreationErrorMessageTextView);
        createDirectoryButton = (Button) findViewById(R.id.createDirectoryButton);

        directoryCreationErrorMessageTextView.setText(getString(
                R.string.directory_creation_error_message,
                getPhotoDirectory().getAbsolutePath()));

        updateLayoutVisibility();

        requestPermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRequestPermissionClicked();
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
        if (!hasFilePermission()) {
            requestPermissionLayout.setVisibility(View.VISIBLE);
            directoryCreationErrorLayout.setVisibility(View.INVISIBLE);
        } else if (!doesPhotoDirectoryExist()) {
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

    private void onRequestPermissionClicked() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_STORAGE_PERMISSION);
    }

    private void onCreateDirectoryClicked() {
        if (doesPhotoDirectoryExist() || attemptToCreatePhotoDirectory()) {
            startPhotoActivity();
        } else {
            ToastUtil.sendShortToast(this, R.string.failed_to_create_directory_toast);
        }
    }

    private boolean hasFilePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean doesPhotoDirectoryExist() {
        return getPhotoDirectory().exists();
    }

    @NonNull
    private File getPhotoDirectory() {
        File rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(rootDir, DIR_NAME);
    }

    private boolean attemptToCreatePhotoDirectory() {
        return getPhotoDirectory().mkdir();
    }
}
