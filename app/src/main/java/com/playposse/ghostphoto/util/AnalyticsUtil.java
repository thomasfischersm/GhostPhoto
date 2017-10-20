package com.playposse.ghostphoto.util;

import android.app.Application;
import android.app.Fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.playposse.ghostphoto.GhostPhotoApplication;

/**
 * A helper class to deal with Google Analytics.
 */
public abstract class AnalyticsUtil {

    public static final String START_TAKING_PHOTOS_ACTION = "startTakingPhotos";
    public static final String STOP_TAKING_PHOTOS_ACTION = "stopTakingPhotos";
    public static final String SET_INTERVAL_ACTION = "setInterval";
    public static final String TAKE_PHOTO_ACTION = "takePhoto";

    private static final String DEFAULT_CATEGORY = "Action";

    public enum AnalyticsCategory {
        sharePhoto,
        sharePhotoShoot,
        deleteAll,
        deleteUnselected,
        deletePhoto,
        selectPhoto,
        rotatePhoto,
        editPhoto,
        sendFeedback,
        fileIntegrityCheck,
        deleteDirectory,
    }

    private AnalyticsUtil() {}

    public static void sendEvent(Fragment fragment, String action) {
        sendEvent(fragment.getActivity().getApplication(), action);
    }

    public static void sendEvent(Application application, String action) {
        GhostPhotoApplication ghostPhotoApplication = (GhostPhotoApplication) application;
        Tracker tracker = ghostPhotoApplication.getDefaultTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(DEFAULT_CATEGORY)
                .setAction(action)
                .build());
    }

    public static void reportScreenName(Application defaultApp, String screenName) {
        GhostPhotoApplication app = (GhostPhotoApplication) defaultApp;
        Tracker tracker = app.getDefaultTracker();
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());

        tracker.enableAdvertisingIdCollection(true);
    }

    public static void reportEvent(
            Application defaultApp,
            AnalyticsCategory category,
            String action) {

        if (StringUtil.isEmpty(action)) {
            action = category.name();
        }

        GhostPhotoApplication app = (GhostPhotoApplication) defaultApp;
        Tracker tracker = app.getDefaultTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category.name())
                .setAction(action)
                .build());
    }
}
