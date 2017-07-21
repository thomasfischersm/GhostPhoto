package com.playposse.ghostphoto;

import android.content.Context;
import android.content.Intent;

import com.playposse.ghostphoto.activities.review.ComparePhotosActivity;
import com.playposse.ghostphoto.activities.review.ReviewPhotoShootActivity;

/**
 * A central class for dealing with extra constants of intents.
 */
public class ExtraConstants {

    private static final String PHOTO_SHOOT_INDEX_ID = "com.playposse.ghostphoto.photoShootIndex";
    private static final String PHOTO_INDEXES_ID = "com.playposse.ghostphoto.photoIndexes";

    public static long getPhotoShootIndex(Intent intent) {
        return intent.getLongExtra(PHOTO_SHOOT_INDEX_ID, 0);
    }

    public static Intent createReviewPhotoShootIntent(Context context, long photoShootIndex) {
        return new Intent(context, ReviewPhotoShootActivity.class)
                .putExtra(PHOTO_SHOOT_INDEX_ID, photoShootIndex);
    }

    public static long[] getPhotoIndexes(Intent intent) {
        return intent.getLongArrayExtra(PHOTO_INDEXES_ID);
    }

    public static Intent createComparePhotosIntent(Context context, long[] photoIndexex) {
        return new Intent(context, ComparePhotosActivity.class)
                .putExtra(PHOTO_INDEXES_ID, photoIndexex);
    }
}
