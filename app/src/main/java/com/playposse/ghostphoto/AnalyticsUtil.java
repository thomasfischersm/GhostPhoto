package com.playposse.ghostphoto;

import android.app.Application;
import android.app.Fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * A helper class to deal with Google Analytics.
 */
public abstract class AnalyticsUtil {

    public static final String START_TAKING_PHOTOS_ACTION = "startTakingPhotos";
    public static final String STOP_TAKING_PHOTOS_ACTION = "stopTakingPhotos";
    public static final String SET_INTERVAL_ACTION = "setInterval";
    public static final String TAKE_PHOTO_ACTION = "takePhoto";

    private static final String DEFAULT_CATEGORY = "Action";

    private AnalyticsUtil() {}

    public  static void sendEvent(Fragment fragment, String action) {
        sendEvent(fragment.getActivity().getApplication(), action);
    }

    public static void sendEvent(Application application, String action) {
        GhostPhotoApplication lsystemApplication = (GhostPhotoApplication) application;
        Tracker tracker = lsystemApplication.getDefaultTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(DEFAULT_CATEGORY)
                .setAction(action)
                .build());
    }
}
