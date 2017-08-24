package com.playposse.ghostphoto.constants;

import android.content.res.Resources;

import com.playposse.ghostphoto.R;

/**
 * An enum that describes the time interval with which the camera will take photos.
 */
public enum TimeInterval {
    halfSecond(500, R.string.half_second_interval),
    oneSecond(1_000, R.string.second_interval),
    threeSeconds(3_000, R.string.three_second_interval),
    tenSeconds(10_000, R.string.ten_second_interval),
    custom(-1, R.string.custom_interval);

    private final long timeInMs;
    private final int stringResId;

    TimeInterval(long timeInMs, int stringResId) {
        this.timeInMs = timeInMs;
        this.stringResId = stringResId;
    }

    public long getTimeInMs() {
        return timeInMs;
    }

    public String getString(Resources resources) {
        return resources.getString(stringResId);
    }
}
