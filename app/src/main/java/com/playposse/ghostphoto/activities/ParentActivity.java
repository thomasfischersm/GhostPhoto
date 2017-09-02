package com.playposse.ghostphoto.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.playposse.ghostphoto.R;
import com.playposse.ghostphoto.activities.camera.PhotoActivity;
import com.playposse.ghostphoto.activities.other.AboutActivity;
import com.playposse.ghostphoto.data.GhostPhotoContract;
import com.playposse.ghostphoto.util.AnalyticsUtil;
import com.playposse.ghostphoto.util.AnalyticsUtil.AnalyticsCategory;
import com.playposse.ghostphoto.util.EmailUtil;
import com.playposse.ghostphoto.util.view.DialogUtil;

import io.fabric.sdk.android.Fabric;

/**
 * A common base class for all activities to enable Google Analytics tracking.
 */
public class ParentActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        AnalyticsUtil.reportScreenName(getApplication(), getClass().getSimpleName());
    }

    protected void initActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_directory_content_menu_item:
                DialogUtil.confirm(
                        this,
                        R.string.confirm_delete_directory_title,
                        R.string.confirm_delete_directory_message,
                        R.string.confirm_button_label,
                        R.string.cancel_button_label,
                        new Runnable() {
                            @Override
                            public void run() {
                                onConfirmedDeleteDirectoryContent();
                            }
                        }
                );
                return true;
            case R.id.send_feedback_menu_item:
                EmailUtil.sendFeedbackAction(this);
                AnalyticsUtil.reportEvent(getApplication(), AnalyticsCategory.sendFeedback, "");
                return true;
            case R.id.about_menu_item:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onConfirmedDeleteDirectoryContent() {
        new DeleteDirectoryAsyncTask().execute();
    }

    /**
     * An {@link AsyncTask} to delete the entire directory content.
     */
    private class DeleteDirectoryAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            getContentResolver().delete(GhostPhotoContract.DeleteDirectoryContentAction.CONTENT_URI, null, null);
            startActivity(new Intent(ParentActivity.this, PhotoActivity.class));
            AnalyticsUtil.reportEvent(getApplication(), AnalyticsCategory.deleteDirectory, "");
            return null;
        }
    }
}
