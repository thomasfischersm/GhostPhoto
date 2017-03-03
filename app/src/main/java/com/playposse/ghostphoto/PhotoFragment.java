package com.playposse.ghostphoto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A {@link android.app.Fragment} that adds the business logic to the {@link BasicPhotoFragment}.
 * {@link BasicPhotoFragment} deals with the mechanics of dealing with the Android photo API.
 */
public class PhotoFragment extends BasicPhotoFragment {

    private static final String LOG_CAT = PhotoFragment.class.getSimpleName();

    private static enum TimeInterval {
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

    private static enum ActionState {
        running,
        stopped,
    }

    private final Timer timer = new Timer();

    private TextView halfSecondTextView;
    private TextView secondTextView;
    private TextView threeSecondTextView;
    private TextView tenSecondTextView;
    private ImageView infoButton;
    private FloatingActionButton actionButton;
    private ImageView thumbNailImageView;

    private TimeInterval currentTimeInterval = TimeInterval.oneSecond;
    private ActionState actionState = ActionState.stopped;
    private PhotoTimerTask currentTimerTask = null;

    public static PhotoFragment newInstance() {
        return new PhotoFragment();
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        halfSecondTextView = (TextView) rootView.findViewById(R.id.halfSecondTextView);
        secondTextView = (TextView) rootView.findViewById(R.id.secondTextView);
        threeSecondTextView = (TextView) rootView.findViewById(R.id.threeSecondTextView);
        tenSecondTextView = (TextView) rootView.findViewById(R.id.tenSecondTextView);
        infoButton = (ImageView) rootView.findViewById(R.id.infoButton);
        actionButton = (FloatingActionButton) rootView.findViewById(R.id.actionButton);
        thumbNailImageView = (ImageView) rootView.findViewById(R.id.thumbNailImageView);

        initTextView(halfSecondTextView, TimeInterval.halfSecond);
        initTextView(secondTextView, TimeInterval.oneSecond);
        initTextView(threeSecondTextView, TimeInterval.threeSeconds);
        initTextView(tenSecondTextView, TimeInterval.tenSeconds);

        refreshActionButton();
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionState == ActionState.running) {
                    stopTakingPhotos();
                    actionState = ActionState.stopped;
                } else {
                    startTakingPhotos();
                    actionState = ActionState.running;
                }
                refreshActionButton();
            }
        });

        thumbNailImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getLastFile() != null) {
                    try {
                        Uri uri = FileProvider.getUriForFile(
                                getActivity(),
                                "com.playposse.ghostphoto",
                                getLastFile());
                        Log.i(LOG_CAT, "Starting intent to view " + uri);

                        Intent intent = new Intent()
                                .setAction(Intent.ACTION_VIEW)
                                .setDataAndType(uri, "image/jpeg")
                                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        PackageManager packageManager = getActivity().getPackageManager();
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent);
                        }
                    } catch (Throwable ex) {
                        Log.e(LOG_CAT, "Failed to view photo in photoviewer");
                        throw ex;
                    }
                }
            }
        });

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AboutActivity.class));
            }
        });
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

    private void initTextView(TextView textView, TimeInterval timeInterval) {
        textView.setOnClickListener(new TimeIntervalOnClickListener(textView, timeInterval));
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
    }

    private synchronized void stopTakingPhotos() {
        if (currentTimerTask != null) {
            currentTimerTask.cancel();
            currentTimerTask = null;
        }

        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
            getActivity().sendBroadcast(mediaScanIntent);
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
        Log.i(LOG_CAT, "Length of sound file: " + mediaPlayer.getDuration());
        mediaPlayer.start();
    }

    /**
     * An {@link android.view.View.OnClickListener} that selects a different time interval.
     */
    private class TimeIntervalOnClickListener implements View.OnClickListener {

        private final TimeInterval timeInterval;
        private final TextView textView;

        private TimeIntervalOnClickListener(TextView textView, TimeInterval timeInterval) {
            this.timeInterval = timeInterval;
            this.textView = textView;

            if (currentTimeInterval == timeInterval) {
                refreshView();
            }
        }

        @Override
        public void onClick(View v) {
            currentTimeInterval = timeInterval;

            refreshView();
        }

        private void refreshView() {
            makeBoldOrNormal(halfSecondTextView);
            makeBoldOrNormal(secondTextView);
            makeBoldOrNormal(threeSecondTextView);
            makeBoldOrNormal(tenSecondTextView);
        }

        private void makeBoldOrNormal(TextView otherTextView) {
            Context context = getActivity();
            if (textView == otherTextView) {
                otherTextView.setTypeface(null, Typeface.BOLD);
                otherTextView.setTextColor(ContextCompat.getColor(context, R.color.selectedText));
                otherTextView.setTextSize(
                        TypedValue.COMPLEX_UNIT_PX,
                        getResources().getDimension(R.dimen.selectedText));
            } else {
                otherTextView.setTypeface(null, Typeface.NORMAL);
                otherTextView.setTextColor(ContextCompat.getColor(context, R.color.unselectedText));
                otherTextView.setTextSize(
                        TypedValue.COMPLEX_UNIT_PX,
                        getResources().getDimension(R.dimen.unselectedText));
            }
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
                }
            });
        }
    }
}
