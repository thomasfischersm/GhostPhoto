package com.playposse.ghostphoto.activities.review;

import android.net.Uri;

/**
 * A listener callback that notifies that a new photo has been selected. This is specifically for
 * {@link ViewPhotoIndividualFragment} to communicate back to the parent fragment and/or activity
 * that the user has swiped to another photo.
 */
public interface PhotoSelectionChangeListener {

    void onPhotoSelected(long photoId, boolean isSelected, Uri photoUri);
}
