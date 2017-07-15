package com.playposse.ghostphoto.camera;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.playposse.ghostphoto.AboutActivity;
import com.playposse.ghostphoto.AnalyticsUtil;
import com.playposse.ghostphoto.GhostPhotoPreferences;
import com.playposse.ghostphoto.R;

import java.io.File;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A {@link android.app.Fragment} that adds the business logic to the {@link BasicPhotoFragment}.
 * {@link BasicPhotoFragment} deals with the mechanics of dealing with the Android photo API.
 */
public class PhotoFragment extends BasicPhotoFragment {

    private static final String LOG_TAG = PhotoFragment.class.getSimpleName();

    private static final String ACTION_STATE_KEY = "actionState";
    private static final String TIME_INTERVAL_KEY = "timeInterval";
    private static final String FLASH_MODE_KEY = "flashMode";

    private enum TimeInterval {
        halfSecond(500),
        oneSecond(1_000),
        threeSeconds(3_000),
        tenSeconds(10_000),;

        private final long timeInMs;

        TimeInterval(long timeInMs) {
            this.timeInMs = timeInMs;
        }

        private long getTimeInMs() {
            return timeInMs;
        }
    }

    private enum ActionState {
        running,
        stopped,
    }

    public enum FlashMode {
        auto,
        on,
        off
    }

    private final Timer timer = new Timer();

    private ImageView flashImageView;
    private TextView halfSecondTextView;
    private TextView secondTextView;
    private TextView threeSecondTextView;
    private TextView tenSecondTextView;
    private ImageView infoButton;
    private FloatingActionButton actionButton;
    private ImageView thumbNailImageView;
    private FrameLayout flashSelectionLayout;
    private LinearLayout flashOffLayout;
    private LinearLayout flashAutoLayout;
    private LinearLayout flashOnLayout;

    private TimeInterval currentTimeInterval = TimeInterval.oneSecond;
    private ActionState actionState = ActionState.stopped;
    private PhotoTimerTask currentTimerTask = null;
    private BiMap<TimeInterval, TextView> timeIntervalToViewMap = HashBiMap.create();

    public static PhotoFragment newInstance() {
        return new PhotoFragment();
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        flashImageView = (ImageView) rootView.findViewById(R.id.flashImageView);
        halfSecondTextView = (TextView) rootView.findViewById(R.id.halfSecondTextView);
        secondTextView = (TextView) rootView.findViewById(R.id.secondTextView);
        threeSecondTextView = (TextView) rootView.findViewById(R.id.threeSecondTextView);
        tenSecondTextView = (TextView) rootView.findViewById(R.id.tenSecondTextView);
        infoButton = (ImageView) rootView.findViewById(R.id.infoButton);
        actionButton = (FloatingActionButton) rootView.findViewById(R.id.actionButton);
        thumbNailImageView = (ImageView) rootView.findViewById(R.id.thumbNailImageView);
        flashSelectionLayout = (FrameLayout) rootView.findViewById(R.id.flashSelectionLayout);
        flashOffLayout = (LinearLayout) rootView.findViewById(R.id.flashOffLayout);
        flashAutoLayout = (LinearLayout) rootView.findViewById(R.id.flashAutoLayout);
        flashOnLayout = (LinearLayout) rootView.findViewById(R.id.flashOnLayout);

        initTextView(halfSecondTextView, TimeInterval.halfSecond);
        initTextView(secondTextView, TimeInterval.oneSecond);
        initTextView(threeSecondTextView, TimeInterval.threeSeconds);
        initTextView(tenSecondTextView, TimeInterval.tenSeconds);

        flashOffLayout.setOnClickListener(new FlashModeOnClickListener(FlashMode.off));
        flashAutoLayout.setOnClickListener(new FlashModeOnClickListener(FlashMode.auto));
        flashOnLayout.setOnClickListener(new FlashModeOnClickListener(FlashMode.on));
        flashSelectionLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss flash selection when the user clicks away.
                startFlashLayoutHideAnimation();
            }
        });

        refreshActionButton();
        actionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionState == ActionState.running) {
                    stopTakingPhotos();
                } else {
                    startTakingPhotos();
                }
            }
        });

        thumbNailImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getLastFile() != null) {
                    try {
                        Uri uri = FileProvider.getUriForFile(
                                getActivity(),
                                "com.playposse.ghostphoto",
                                getLastFile());
                        Log.i(LOG_TAG, "Starting intent to view " + uri);

                        Intent intent = new Intent()
                                .setAction(Intent.ACTION_VIEW)
                                .setDataAndType(uri, "image/jpeg")
                                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        PackageManager packageManager = getActivity().getPackageManager();
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent);
                        }
                    } catch (Throwable ex) {
                        Log.e(LOG_TAG, "Failed to view photo in photoviewer");
                        throw ex;
                    }
                }
            }
        });

        infoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AboutActivity.class));
            }
        });

        flashImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startFlashLayoutRevealAnimation();
            }
        });

        continueTakingPhotosAfterScreenRotation(savedInstanceState);
        refreshFlashState();
        refreshTimeIntervalViews();
    }

    @Override
    public void onPause() {
        if (currentTimerTask != null) {
            currentTimerTask.cancel();
            currentTimerTask = null;
        }

        super.onPause();

        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(ACTION_STATE_KEY, actionState.name());
        outState.putString(TIME_INTERVAL_KEY, currentTimeInterval.name());
    }

    private void initTextView(TextView textView, TimeInterval timeInterval) {
        timeIntervalToViewMap.put(timeInterval, textView);
        textView.setOnClickListener(new TimeIntervalOnClickListener());
    }

    private void refreshActionButton() {
        if (actionState == ActionState.running) {
            actionButton.setImageResource(R.drawable.ic_stop_black_24dp);
        } else {
            actionButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
    }

    private synchronized void startTakingPhotos() {
        if (currentTimerTask != null) {
            // Be extra defensive!
            currentTimerTask.cancel();
        }

        currentTimerTask = new PhotoTimerTask();
        timer.scheduleAtFixedRate(currentTimerTask, 0, currentTimeInterval.getTimeInMs());
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        AnalyticsUtil.sendEvent(this, AnalyticsUtil.START_TAKING_PHOTOS_ACTION);

        actionState = ActionState.running;
        refreshActionButton();
    }

    private synchronized void stopTakingPhotos() {
        if (currentTimerTask != null) {
            currentTimerTask.cancel();
            currentTimerTask = null;
        }

        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        AnalyticsUtil.sendEvent(this, AnalyticsUtil.STOP_TAKING_PHOTOS_ACTION);

        actionState = ActionState.stopped;
        refreshActionButton();
    }

    @Override
    protected void onAfterPhotoTaken(File photoFile) {
        addPhotoToGallery(photoFile);
        showThumbNail(photoFile);
    }

    private void addPhotoToGallery(File photoFile) {
        if (photoFile != null) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(photoFile);
            mediaScanIntent.setData(contentUri);

            if (getActivity() != null) {
                getActivity().sendBroadcast(mediaScanIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // getActivity() may return null during screen rotation changes. If the user has
                // a late enough SDK, try using getContext().
                getContext().sendBroadcast(mediaScanIntent);
            }
        }
    }

    private void showThumbNail(File photoFile) {
        if (photoFile != null) {
            Uri contentUri = Uri.fromFile(photoFile);
            thumbNailImageView.setImageURI(contentUri);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    actionButton.getWidth(),
                    actionButton.getHeight());
//            layoutParams.addRule(RelativeLayout.ALIGN_TOP, R.id.actionButton);
//            layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.actionButton);
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            } else {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            }
            thumbNailImageView.setLayoutParams(layoutParams);
            thumbNailImageView.requestLayout();
            thumbNailImageView.invalidate();
        }
    }

    private void playSound() {
        final MediaPlayer mediaPlayer;
        if (currentTimeInterval == TimeInterval.halfSecond) {
            mediaPlayer = MediaPlayer.create(getActivity(), R.raw.water_drop_sound);
        } else {
            mediaPlayer = MediaPlayer.create(getActivity(), R.raw.water_drop_sound2);
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        Log.i(LOG_TAG, "Length of sound file: " + mediaPlayer.getDuration());
        mediaPlayer.start();
    }

    private void refreshTimeIntervalViews() {
        for (Map.Entry<TimeInterval, TextView> entry : timeIntervalToViewMap.entrySet()) {
            makeBoldOrNormal(entry.getValue(), entry.getKey() == currentTimeInterval);
        }
    }

    private void makeBoldOrNormal(TextView textView, boolean isBold) {
        Context context = getActivity();
        if (isBold) {
            textView.setTypeface(null, Typeface.BOLD);
            textView.setTextColor(ContextCompat.getColor(context, R.color.selectedText));
            textView.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.selectedText));
        } else {
            textView.setTypeface(null, Typeface.NORMAL);
            textView.setTextColor(ContextCompat.getColor(context, R.color.unselectedText));
            textView.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.unselectedText));
        }
    }

    private void refreshFlashState() {
        if (getActivity() != null) {
            currentFlashMode = GhostPhotoPreferences.getFlashMode(getActivity());
        }
        switch (currentFlashMode) {
            case auto:
                flashImageView.setImageResource(R.drawable.ic_flash_auto_black_24dp);
                break;
            case on:
                flashImageView.setImageResource(R.drawable.ic_flash_on_black_24dp);
                break;
            case off:
                flashImageView.setImageResource(R.drawable.ic_flash_off_black_24dp);
                break;
        }
    }

    private void continueTakingPhotosAfterScreenRotation(Bundle savedInstanceState) {
        // Re-start the photo timer if the user simply changed the screen orientation.
        if (savedInstanceState != null) {
            String savedTimeIntervalStr = savedInstanceState.getString(TIME_INTERVAL_KEY);
            if (savedTimeIntervalStr != null) {
                currentTimeInterval = TimeInterval.valueOf(savedTimeIntervalStr);
                refreshTimeIntervalViews();
            }

            String savedActionStateStr = savedInstanceState.getString(ACTION_STATE_KEY);
            if (savedActionStateStr != null) {
                ActionState savedActionState = ActionState.valueOf(savedActionStateStr);
                if (savedActionState == ActionState.running) {
                    startTakingPhotos();
                }
            }

            String savedFlashModeStr = savedInstanceState.getString(FLASH_MODE_KEY);
            if (savedFlashModeStr != null) {
                currentFlashMode = FlashMode.valueOf(savedFlashModeStr);
            }
        }
    }

    private void startFlashLayoutRevealAnimation() {
        int centerX = (flashImageView.getLeft() + flashImageView.getRight()) / 2;
        int centerY = (flashImageView.getTop() + flashImageView.getBottom()) / 2;
        int startRadius = 0;
        int endRadius = flashSelectionLayout.getWidth();

        Animator animator = ViewAnimationUtils.createCircularReveal(
                flashSelectionLayout,
                centerX,
                centerY,
                startRadius,
                endRadius);

        flashSelectionLayout.setVisibility(View.VISIBLE);
        flashImageView.setVisibility(View.GONE);
        animator.start();
    }

    private void startFlashLayoutHideAnimation() {
        int centerX = (flashImageView.getLeft() + flashImageView.getRight()) / 2;
        int centerY = (flashImageView.getTop() + flashImageView.getBottom()) / 2;
        int startRadius = flashSelectionLayout.getWidth();
        int endRadius = 0;

        Animator animator = ViewAnimationUtils.createCircularReveal(
                flashSelectionLayout,
                centerX,
                centerY,
                startRadius,
                endRadius);

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // Nothing to do.
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                flashSelectionLayout.setVisibility(View.GONE);
                flashImageView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // Nothing to do.
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // Nothing to do.
            }
        });

        animator.start();
    }

    /**
     * An {@link OnClickListener} that selects a different time interval.
     */
    private class TimeIntervalOnClickListener implements OnClickListener {

        @Override
        public void onClick(View view) {
            currentTimeInterval = timeIntervalToViewMap.inverse().get((TextView) view);

            refreshTimeIntervalViews();
            AnalyticsUtil.sendEvent(
                    getActivity().getApplication(),
                    AnalyticsUtil.SET_INTERVAL_ACTION + currentTimeInterval.timeInMs);
        }
    }

    /**
     * An {@link OnClickListener} that selects a new flash mode.
     */
    private class FlashModeOnClickListener implements OnClickListener {

        private final FlashMode flashMode;

        private FlashModeOnClickListener(FlashMode flashMode) {
            this.flashMode = flashMode;
        }

        @Override
        public void onClick(View v) {
            startFlashLayoutHideAnimation();
            GhostPhotoPreferences.setFlashMode(getActivity(), flashMode);
            refreshFlashState();
            Log.i(LOG_TAG, "onClick: Selected new flash mode: " + currentFlashMode.name());
        }
    }

    /**
     * A {@link TimerTask} that takes a photo at each time interval.
     */
    private class PhotoTimerTask extends TimerTask {

        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    takePicture();
                    playSound();
                    AnalyticsUtil.sendEvent(
                            getActivity().getApplication(),
                            AnalyticsUtil.TAKE_PHOTO_ACTION);
                }
            });
        }
    }
}
