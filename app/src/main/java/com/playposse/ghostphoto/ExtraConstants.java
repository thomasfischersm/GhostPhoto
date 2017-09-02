package com.playposse.ghostphoto;

import android.content.Context;
import android.content.Intent;

import com.playposse.ghostphoto.activities.other.PermissionRecoveryActivity;
import com.playposse.ghostphoto.activities.review.ComparePhotosActivity;
import com.playposse.ghostphoto.activities.review.ReviewPhotoShootActivity;
import com.playposse.ghostphoto.activities.review.ViewPhotoActivity;

/**
 * A central class for dealing with extra constants of intents.
 */
public class ExtraConstants {

    private static final String PHOTO_SHOOT_INDEX_ID = "com.playposse.ghostphoto.photoShootIndex";
    private static final String PHOTO_INDEX_ID = "com.playposse.ghostphoto.photoIndex";
    private static final String PHOTO_INDEXES_ID = "com.playposse.ghostphoto.photoIndexes";

    public static long getPhotoShootIndex(Intent intent) {
        return intent.getLongExtra(PHOTO_SHOOT_INDEX_ID, 0);
    }

    public static Intent createReviewPhotoShootIntent(Context context, long photoShootIndex) {
        return new Intent(context, ReviewPhotoShootActivity.class)
                .putExtra(PHOTO_SHOOT_INDEX_ID, photoShootIndex);
    }

    public static long getPhotoIndex(Intent intent) {
        return intent.getLongExtra(PHOTO_INDEX_ID, -1);
    }

    public static Intent createViewPhotoIntent(Context context, long photoShootId, long photoId) {
        return new Intent(context, ViewPhotoActivity.class)
                .putExtra(PHOTO_SHOOT_INDEX_ID, photoShootId)
                .putExtra(PHOTO_INDEX_ID, photoId);
    }

    public static long[] getPhotoIndexes(Intent intent) {
        return intent.getLongArrayExtra(PHOTO_INDEXES_ID);
    }

    public static Intent createComparePhotosIntent(Context context, long[] photoIndexes) {
        return new Intent(context, ComparePhotosActivity.class)
                .putExtra(PHOTO_INDEXES_ID, photoIndexes);
    }

    public static void startPermissionActivity(Context context) {
        context.startActivity(new Intent(context, PermissionRecoveryActivity.class));
    }
}
