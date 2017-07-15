package com.playposse.ghostphoto.activities.other;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.playposse.ghostphoto.activities.ParentActivity;
import com.playposse.ghostphoto.R;

/**
 * A {@link android.app.Activity} that provides background information about the app.
 */
public class AboutActivity extends ParentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
