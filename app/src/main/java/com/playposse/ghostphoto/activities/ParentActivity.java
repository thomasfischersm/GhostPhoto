package com.playposse.ghostphoto.activities;

import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.playposse.ghostphoto.GhostPhotoApplication;

/**
 * A common base class for all activities to enable Google Analytics tracking.
 */
public class ParentActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();

        GhostPhotoApplication application = (GhostPhotoApplication) getApplication();
        Tracker tracker = application.getDefaultTracker();
        tracker.setScreenName(getClass().getSimpleName());
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
