package com.playposse.ghostphoto.activities.other;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.playposse.ghostphoto.R;

/**
 * A {@link Fragment} that is shown after the user logs on for the first time. It explains the app
 * to the user.
 */
public class IntroductionSlide0Fragment extends Fragment {

    private ImageView typicalSelfieImageView;
    private ImageView freeSelfieImageView;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_introduction_slide0, container, false);

        typicalSelfieImageView = (ImageView) rootView.findViewById(R.id.typicalSelfieImageView);
        freeSelfieImageView = (ImageView) rootView.findViewById(R.id.freeSelfieImageView);

        Glide.with(getActivity())
                .load(R.drawable.annie_spratt_196010)
                .into(typicalSelfieImageView);

        Glide.with(getActivity())
                .load(R.drawable.havilah_galaxy_249906)
                .into(freeSelfieImageView);

        return rootView;
    }
}
