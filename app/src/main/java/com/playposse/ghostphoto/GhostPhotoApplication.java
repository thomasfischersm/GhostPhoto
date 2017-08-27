package com.playposse.ghostphoto;

import android.app.Application;
import android.content.Intent;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.playposse.ghostphoto.services.CheckFileIntegrityService;

/**
 * An implementation of {@link Application} to provide access to Google Analytics.
 */
public class GhostPhotoApplication extends Application {

    public static final String FILE_PROVIDER_AUTHORITY = "com.playposse.ghostphoto.fileprovider";

    private Tracker tracker;

    @Override
    public void onCreate() {
        super.onCreate();

        startService(new Intent(getApplicationContext(), CheckFileIntegrityService.class));

//        getApplicationContext().deleteDatabase("ghostPhotoDb");
//        GhostPhotoPreferences.reset(getApplicationContext());
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            tracker = analytics.newTracker(R.xml.global_tracker);
        }
        return tracker;
    }
}
