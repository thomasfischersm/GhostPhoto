package com.playposse.ghostphoto.activities.other;

import android.os.Bundle;

import com.playposse.ghostphoto.R;
import com.playposse.ghostphoto.activities.ParentActivity;

/**
 * A {@link android.app.Activity} that provides background information about the app.
 */
public class AboutActivity extends ParentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        initActionBar();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
