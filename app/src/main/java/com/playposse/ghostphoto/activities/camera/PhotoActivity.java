package com.playposse.ghostphoto.activities.camera;

import android.content.Intent;
import android.os.Bundle;

import com.playposse.ghostphoto.GhostPhotoPreferences;
import com.playposse.ghostphoto.activities.ParentActivity;
import com.playposse.ghostphoto.R;
import com.playposse.ghostphoto.activities.other.IntroductionActivity;

public class PhotoActivity extends ParentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!GhostPhotoPreferences.hasSeenIntroDeck(this)) {
            finish();
            Intent intent = new Intent(getApplicationContext(), IntroductionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            return;
        }

        setContentView(R.layout.activity_photo);

        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, PhotoFragment.newInstance())
                    .commit();
        }
    }
}
