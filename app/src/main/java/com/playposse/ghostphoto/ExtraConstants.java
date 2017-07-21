package com.playposse.ghostphoto;

import android.content.Context;
import android.content.Intent;

import com.playposse.ghostphoto.activities.review.ReviewPhotoShootActivity;

/**
 * A central class for dealing with extra constants of intents.
 */
public class ExtraConstants {

    private static final String PHOTO_SHOOT_INDEX_ID = "com.playposse.ghostphoto.photoShootIndex";

    public static long getPhotoShootIndex(Intent intent) {
        return intent.getLongExtra(PHOTO_SHOOT_INDEX_ID, 0);
    }

    public static Intent createReviewPhotoShootIntent(Context context, long photoIndex) {
        return new Intent(context, ReviewPhotoShootActivity.class)
                .putExtra(PHOTO_SHOOT_INDEX_ID, photoIndex);
    }
}
