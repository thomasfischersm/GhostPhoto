package com.playposse.ghostphoto.services;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.playposse.ghostphoto.data.GhostPhotoContract.ScanPhotoFilesAction;

/**
 * A {@link Service} that scans for photo files that have been deleted by other apps.
 */
public class ScanForDeletedPhotosService extends IntentService {

    private static final String SERVICE_NAME = "ScanForDeletedPhotosService";

    public ScanForDeletedPhotosService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        getContentResolver().update(ScanPhotoFilesAction.CONTENT_URI, null, null, null);
    }
}
