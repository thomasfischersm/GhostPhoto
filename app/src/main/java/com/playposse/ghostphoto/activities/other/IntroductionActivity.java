package com.playposse.ghostphoto.activities.other;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

import com.playposse.ghostphoto.GhostPhotoPreferences;
import com.playposse.ghostphoto.R;
import com.playposse.ghostphoto.activities.ParentActivity;
import com.playposse.ghostphoto.activities.camera.PhotoActivity;
import com.playposse.ghostphoto.util.AnalyticsUtil;

/**
 * An {@link Activity} that shows introductory slides to the user after the first user log on.
 */
public class IntroductionActivity extends ParentActivity {

    private static final String LOG_TAG = IntroductionActivity.class.getSimpleName();

    private ViewPager introductionSlidePager;
    private Button getStartedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (GhostPhotoPreferences.hasSeenIntroDeck(this)) {
            finish();
            Intent intent = new Intent(getApplicationContext(), PhotoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            return;
        }

        setContentView(R.layout.activity_introduction);

        introductionSlidePager = findViewById(R.id.introductionSlidePager);
        getStartedButton = findViewById(R.id.getStartedButton);

        IntroductionSlidePagerAdapter pagerAdapter =
                new IntroductionSlidePagerAdapter(getSupportFragmentManager());
        introductionSlidePager.setAdapter(pagerAdapter);

        introductionSlidePager.addOnPageChangeListener(new AnalyticsPageChangeListener());

        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GhostPhotoPreferences.setHasSeenIntroDeck(getApplicationContext(), true);
                startActivity(new Intent(getApplicationContext(), PhotoActivity.class));
            }
        });
    }

    private class IntroductionSlidePagerAdapter extends FragmentPagerAdapter {

        private IntroductionSlidePagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new IntroductionSlide0Fragment();
                case 1:
                    return new IntroductionSlide1Fragment();
                default:
                    throw new IllegalStateException(
                            "Unexpected introduction deck was requested: " + position);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    /**
     * A {@link android.support.v4.view.ViewPager.OnPageChangeListener} that reports to Analytics
     * when a new fragment is selected.
     */
    private class AnalyticsPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // Nothing to do.
        }

        @Override
        public void onPageSelected(int position) {
            String screenName = IntroductionActivity.class.getSimpleName() + position;
            AnalyticsUtil.reportScreenName(getApplication(), screenName);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            // Nothing to do.
        }
    }
}
