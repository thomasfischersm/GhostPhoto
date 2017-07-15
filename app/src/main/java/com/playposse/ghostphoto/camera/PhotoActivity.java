package com.playposse.ghostphoto.camera;

import android.os.Bundle;

import com.playposse.ghostphoto.ParentActivity;
import com.playposse.ghostphoto.R;

public class PhotoActivity extends ParentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo);

        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, PhotoFragment.newInstance())
                    .commit();
        }
    }
}
