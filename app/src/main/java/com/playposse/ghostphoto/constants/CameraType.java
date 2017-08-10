package com.playposse.ghostphoto.constants;

import android.hardware.camera2.CameraCharacteristics;

/**
 * An enum to describe the front or back-facing camera.
 */
public enum CameraType {
    front(CameraCharacteristics.LENS_FACING_FRONT),
    back(CameraCharacteristics.LENS_FACING_BACK);

    private int cameraCharacteristics;

    CameraType(int cameraCharacteristics) {
        this.cameraCharacteristics = cameraCharacteristics;
    }

    public int getCameraCharacteristics() {
        return cameraCharacteristics;
    }
}
