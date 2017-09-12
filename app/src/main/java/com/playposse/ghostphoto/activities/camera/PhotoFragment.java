package com.playposse.ghostphoto.activities.camera;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.playposse.ghostphoto.GhostPhotoPreferences;
import com.playposse.ghostphoto.R;
import com.playposse.ghostphoto.activities.review.ListShootsActivity;
import com.playposse.ghostphoto.constants.ActionState;
import com.playposse.ghostphoto.constants.CameraType;
import com.playposse.ghostphoto.constants.FlashMode;
import com.playposse.ghostphoto.constants.TimeInterval;
import com.playposse.ghostphoto.data.GhostPhotoContract;
import com.playposse.ghostphoto.data.GhostPhotoContract.AddPhotoAction;
import com.playposse.ghostphoto.data.GhostPhotoContract.EndShootAction;
import com.playposse.ghostphoto.data.GhostPhotoContract.GetLatestPhotoAction;
import com.playposse.ghostphoto.data.GhostPhotoContract.PhotoTable;
import com.playposse.ghostphoto.util.AnalyticsUtil;
import com.playposse.ghostphoto.util.RevealAnimationUtil;
import com.playposse.ghostphoto.util.SmartCursor;
import com.playposse.ghostphoto.util.view.NumberPickerDialogBuilder;

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
    private static final String CAMERA_TYPE = "cameraType";

    private static final int LOADER_ID = 2;

    private static final int MINIMUM_CUSTOM_TIME_INTERVAL = 1;
    private static final int MAXIMUM_CUSTOM_TIME_INTERVAL = 60;

    private final Timer timer = new Timer();

    private ImageView flashImageView;
    private ImageView switchCameraImageView;
    private ImageView optionsMenuImageView;
    private Button selectedIntervalButton;
    private FrameLayout intervalSelectionLayout;
    private Button halfSecondTextView;
    private Button secondTextView;
    private Button threeSecondTextView;
    private Button tenSecondTextView;
    private Button customTextView;
    private ImageButton actionButton;
    private ImageView thumbNailImageView;
    private FrameLayout flashSelectionLayout;
    private LinearLayout flashOffLayout;
    private LinearLayout flashAutoLayout;
    private LinearLayout flashOnLayout;

    private TimeInterval currentTimeInterval = TimeInterval.oneSecond;
    private int customInterval = 30;
    private ActionState actionState = ActionState.stopped;
    private PhotoTimerTask currentTimerTask = null;
    private BiMap<TimeInterval, Button> timeIntervalToViewMap = HashBiMap.create();

    /**
     * The {@link Context} is needed after the {@link Fragment} is detached from the
     * {@link Activity} because some photo operations could finish late.
     */
    private Context applicationContext;

    public static PhotoFragment newInstance() {
        return new PhotoFragment();
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        flashImageView = rootView.findViewById(R.id.flashImageView);
        switchCameraImageView = rootView.findViewById(R.id.switchCameraImageView);
        optionsMenuImageView = rootView.findViewById(R.id.optionsMenuImageView);
        selectedIntervalButton = rootView.findViewById(R.id.selectedIntervalButton);
        intervalSelectionLayout = rootView.findViewById(R.id.intervalSelectionLayout);
        halfSecondTextView = rootView.findViewById(R.id.halfSecondTextView);
        secondTextView = rootView.findViewById(R.id.secondTextView);
        threeSecondTextView = rootView.findViewById(R.id.threeSecondTextView);
        tenSecondTextView = rootView.findViewById(R.id.tenSecondTextView);
        customTextView = rootView.findViewById(R.id.customTextView);
        actionButton = rootView.findViewById(R.id.actionButton);
        thumbNailImageView = rootView.findViewById(R.id.thumbNailImageView);
        flashSelectionLayout = rootView.findViewById(R.id.flashSelectionLayout);
        flashOffLayout = rootView.findViewById(R.id.flashOffLayout);
        flashAutoLayout = rootView.findViewById(R.id.flashAutoLayout);
        flashOnLayout = rootView.findViewById(R.id.flashOnLayout);

        customInterval = GhostPhotoPreferences.getCustomPhotoIntervalSeconds(getActivity());

        initIntevalButton(halfSecondTextView, TimeInterval.halfSecond);
        initIntevalButton(secondTextView, TimeInterval.oneSecond);
        initIntevalButton(threeSecondTextView, TimeInterval.threeSeconds);
        initIntevalButton(tenSecondTextView, TimeInterval.tenSeconds);
        initIntevalButton(customTextView, TimeInterval.custom);

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
                startActivity(new Intent(getActivity(), ListShootsActivity.class));
            }
        });

        flashImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startFlashLayoutRevealAnimation();
            }
        });

        optionsMenuImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onShowOptionsMenu();
            }
        });

        switchCameraImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSwitchCamera();
            }
        });

        selectedIntervalButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startIntervalLayoutRevealAnimation();
            }
        });

        intervalSelectionLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the selection panel if the user clicks away.
                startIntervalLayoutHideAnimation();
            }
        });

        continueTakingPhotosAfterScreenRotation(savedInstanceState);
        refreshFlashState();
        refreshTimeIntervalViews();
    }

    @Override
    public void onResume() {
        super.onResume();

        getLoaderManager().initLoader(
                LOADER_ID,
                null,
                new LoaderManager.LoaderCallbacks<Cursor>() {
                    @Override
                    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                        return new CursorLoader(
                                getActivity(),
                                GetLatestPhotoAction.CONTENT_URI,
                                null,
                                null,
                                null,
                                null);
                    }

                    @Override
                    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                        if (cursor.moveToFirst()) {
                            SmartCursor smartCursor =
                                    new SmartCursor(cursor, PhotoTable.COLUMN_NAMES);
                            final String photoUri =
                                    smartCursor.getString(PhotoTable.FILE_URI_COLUMN);
                            thumbNailImageView.post(new Runnable() {
                                @Override
                                public void run() {
                                    showThumbNail(photoUri);
                                    Log.i(LOG_TAG, "onLoadFinished: Showing new thumbnail: "
                                            + photoUri);
                                }
                            });
                            showThumbNail(photoUri);
                            Log.i(LOG_TAG, "onLoadFinished: Showing new thumbnail: " + photoUri);
                        } else {
                            Log.i(LOG_TAG, "onLoadFinished: Didn't get a last photo thumbnail.");
                            showThumbNail(null);
                        }
                    }

                    @Override
                    public void onLoaderReset(Loader<Cursor> loader) {
                        showThumbNail(null);
                    }
                });

        showActionButtonHint();
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
        outState.putString(FLASH_MODE_KEY, currentFlashMode.name());
        outState.putString(CAMERA_TYPE, getCurrentCameraType().name());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        applicationContext = context.getApplicationContext();
    }

    private void initIntevalButton(Button button, TimeInterval timeInterval) {
        timeIntervalToViewMap.put(timeInterval, button);
        button.setOnClickListener(new TimeIntervalOnClickListener());
    }

    private void refreshActionButton() {
        if (actionState == ActionState.running) {
            actionButton.setImageResource(R.drawable.ic_stop_white_24dp);
        } else {
            actionButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        }
    }

    private synchronized void startTakingPhotos() {
        if (currentTimerTask != null) {
            // Be extra defensive!
            currentTimerTask.cancel();
        }

        long timeInMs = currentTimeInterval.getTimeInMs();
        if (timeInMs <=0) {
            timeInMs = customInterval * 1_000;
        }

        currentTimerTask = new PhotoTimerTask();
        timer.scheduleAtFixedRate(currentTimerTask, 0, timeInMs);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        AnalyticsUtil.sendEvent(this, AnalyticsUtil.START_TAKING_PHOTOS_ACTION);

        actionState = ActionState.running;
        refreshActionButton();

        // Record action to the db.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                getActivity()
                        .getContentResolver()
                        .insert(GhostPhotoContract.StartShootAction.CONTENT_URI, null);
                return null;
            }
        }.execute();
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

        // Record action to the db.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    // Wait for the last photo to be taken (in case it is late).
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Log.e(LOG_TAG, "doInBackground: Failed to sleep", ex);
                }

                getActivity()
                        .getContentResolver()
                        .update(EndShootAction.CONTENT_URI, null, null, null);
                return null;
            }
        }.execute();

        showThumbnailHint();
    }

    @Override
    protected void onAfterPhotoTaken(final File photoFile) {
        addPhotoToGallery(photoFile);
//        showThumbNail(photoFile);

        // Record action to the db.
        if (photoFile != null) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    Context context = getUsefulContext();
                    if ((context == null) || (context.getContentResolver() == null)) {
                        return null;
                    }

                    Uri fileUri = Uri.fromFile(photoFile);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(
                            PhotoTable.FILE_URI_COLUMN,
                            fileUri.toString());
                    context
                            .getContentResolver()
                            .insert(AddPhotoAction.CONTENT_URI, contentValues);
                    return null;
                }
            }.execute();
        }
    }

    private Context getUsefulContext() {
        if (applicationContext != null) {
            return applicationContext;
        } else if (getActivity() != null) {
            return getActivity();
        } if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // getActivity() may return null during screen rotation changes. If the user has
            // a late enough SDK, try using getContext().
            return getContext();
        } else {
            return null;
        }
    }

    /**
     * Called when the pre-capture sequence is complete, and the actual frame is about to be taken.
     * This is the most precise moment to play a trigger sound.
     */
    @Override
    protected void onAboutToTakePhoto() {
        playSound();
    }

    private void addPhotoToGallery(File photoFile) {
        if (photoFile != null) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(photoFile);
            mediaScanIntent.setData(contentUri);

            Context context = getUsefulContext();
            if (context != null) {
                context.sendBroadcast(mediaScanIntent);
            }
        }
    }

    private void showThumbNail(String photoContentUri) {
        if (!isAdded()) {
            // Let's be defensive. Since retrieving the thumbnail info, the fragment could have
            // detached. So, let's avoid creating an exception here.
            return;
        }

        if (photoContentUri != null) {
            Uri contentUri = Uri.parse(photoContentUri);
            Glide.with(getActivity())
                    .load(contentUri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(thumbNailImageView);
        } else {
            thumbNailImageView.setImageBitmap(null);
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
        selectedIntervalButton.setSelected(true);
        selectedIntervalButton.setText(currentTimeInterval.getString(getResources()));

        for (Map.Entry<TimeInterval, Button> entry : timeIntervalToViewMap.entrySet()) {
            makeBoldOrNormal(entry.getValue(), entry.getKey() == currentTimeInterval);
        }
    }

    private void makeBoldOrNormal(Button button, boolean isBold) {
        Context context = getActivity();
        if (isBold) {
            button.setSelected(true);
            button.setTypeface(null, Typeface.BOLD);
            button.setTextColor(ContextCompat.getColor(context, R.color.selectedText));
            button.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.selectedText));
        } else {
            button.setSelected(false);
            button.setTypeface(null, Typeface.NORMAL);
            button.setTextColor(ContextCompat.getColor(context, R.color.unselectedText));
            button.setTextSize(
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

            String savedCameraType = savedInstanceState.getString(CAMERA_TYPE);
            if (savedCameraType != null) {
                setCurrentCameraType(CameraType.valueOf(savedCameraType));
            }
        }
    }

    private void startFlashLayoutRevealAnimation() {
        RevealAnimationUtil.startRevealAnimation(flashImageView, flashSelectionLayout);
    }

    private void startFlashLayoutHideAnimation() {
        RevealAnimationUtil.startHideAnimation(flashImageView, flashSelectionLayout);
    }

    private void startIntervalLayoutRevealAnimation() {
        RevealAnimationUtil.startRevealAnimation(selectedIntervalButton, intervalSelectionLayout);
    }

    private void startIntervalLayoutHideAnimation() {
        RevealAnimationUtil.startHideAnimation(selectedIntervalButton, intervalSelectionLayout);
    }

    private void showActionButtonHint() {
        if (!GhostPhotoPreferences.hasActionButtonHintBeenSeen(getActivity())) {
            GhostPhotoPreferences.setHasActionButtonHintBeenSeen(getActivity(), true);
            TapTargetView.showFor(getActivity(),
                    TapTarget.forView(
                            actionButton,
                            getString(R.string.action_button_hint_title),
                            getString(R.string.action_button_hint_message))
                            // All options below are optional
                            .outerCircleColor(R.color.colorPrimary)      // Specify a color for the outer circle
                            .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                            .targetCircleColor(R.color.emphasis)   // Specify a color for the target circle
                            .titleTextSize(20)                  // Specify the size (in sp) of the title text
                            .titleTextColor(R.color.primaryText)      // Specify the color of the title text
                            .descriptionTextSize(14)            // Specify the size (in sp) of the description text
                            .descriptionTextColor(R.color.primaryText)  // Specify the color of the description text
//                        .textColor(R.color.secondaryTextColorDark)            // Specify a color for both the title and description text
                            .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                            //.dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                            .drawShadow(true)                   // Whether to draw a drop shadow or not
                            .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                            .tintTarget(true)                   // Whether to tint the target view's color
                            .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                            //.icon(Drawable)                     // Specify a custom drawable to draw as the target
                            .targetRadius(60));                 // Specify the target radius (in dp)
        }
    }

    private void showThumbnailHint() {
        if (!GhostPhotoPreferences.hasThumbnailHintBeenSeen(getActivity())) {
            GhostPhotoPreferences.setHasThumbnailHintBeenSeen(getActivity(), true);
            TapTargetView.showFor(getActivity(),
                    TapTarget.forView(
                            thumbNailImageView,
                            getString(R.string.thumbnail_hint_title),
                            getString(R.string.thumbnail_hint_message))
                            // All options below are optional
                            .outerCircleColor(R.color.colorPrimary)      // Specify a color for the outer circle
                            .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                            .targetCircleColor(R.color.emphasis)   // Specify a color for the target circle
                            .titleTextSize(20)                  // Specify the size (in sp) of the title text
                            .titleTextColor(R.color.primaryText)      // Specify the color of the title text
                            .descriptionTextSize(14)            // Specify the size (in sp) of the description text
                            .descriptionTextColor(R.color.primaryText)  // Specify the color of the description text
//                        .textColor(R.color.secondaryTextColorDark)            // Specify a color for both the title and description text
                            .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                            //.dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                            .drawShadow(true)                   // Whether to draw a drop shadow or not
                            .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                            .tintTarget(true)                   // Whether to tint the target view's color
                            .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                            //.icon(Drawable)                     // Specify a custom drawable to draw as the target
                            .targetRadius(60));                  // Specify the target radius (in dp)
        }
    }

    private void onShowOptionsMenu() {
        PopupMenu menu = new PopupMenu(getActivity(), optionsMenuImageView);
        menu.inflate(R.menu.options_menu);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return getActivity().onOptionsItemSelected(item);
            }
        });

        // Make the background transparent.
        Context wrapper = new ContextThemeWrapper(getActivity(), R.style.ManualPopupMenuStyle);

        MenuPopupHelper menuHelper =
                new MenuPopupHelper(wrapper, (MenuBuilder) menu.getMenu(), optionsMenuImageView);
        menuHelper.setForceShowIcon(true);
        menuHelper.show();
    }

    private void onSwitchCamera() {
        switchCameraType();
        switchCameraImageView.animate()
                .rotationBy(180)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * An {@link OnClickListener} that selects a different time interval.
     */
    private class TimeIntervalOnClickListener implements OnClickListener {

        @Override
        public void onClick(View view) {
            currentTimeInterval = timeIntervalToViewMap.inverse().get((TextView) view);

            if (currentTimeInterval == TimeInterval.custom) {
                showCustomTimeIntervalDialog();
            }

            refreshTimeIntervalViews();

            startIntervalLayoutHideAnimation();

            AnalyticsUtil.sendEvent(
                    getActivity().getApplication(),
                    AnalyticsUtil.SET_INTERVAL_ACTION + currentTimeInterval.getTimeInMs());
        }

        private void showCustomTimeIntervalDialog() {
            NumberPickerDialogBuilder.build(
                    getActivity(),
                    getString(R.string.custom_interval_dialog_title),
                    customInterval,
                    MINIMUM_CUSTOM_TIME_INTERVAL,
                    MAXIMUM_CUSTOM_TIME_INTERVAL,
                    new NumberPickerDialogBuilder.NumberPickerDialogCallback() {
                        @Override
                        public void onPickedNumber(int number) {
                            customInterval = number;
                            GhostPhotoPreferences
                                    .setCustomPhotoIntervalSeconds(getActivity(), number);
                        }
                    });
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
                    AnalyticsUtil.sendEvent(
                            getActivity().getApplication(),
                            AnalyticsUtil.TAKE_PHOTO_ACTION);
                }
            });
        }
    }
}
