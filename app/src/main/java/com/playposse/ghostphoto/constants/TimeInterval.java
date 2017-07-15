package com.playposse.ghostphoto.constants;

/**
 * An enum that describes the time interval with which the camera will take photos.
 */
public enum TimeInterval {
    halfSecond(500),
    oneSecond(1_000),
    threeSeconds(3_000),
    tenSeconds(10_000),;

    private final long timeInMs;

    TimeInterval(long timeInMs) {
        this.timeInMs = timeInMs;
    }

    public long getTimeInMs() {
        return timeInMs;
    }
}
