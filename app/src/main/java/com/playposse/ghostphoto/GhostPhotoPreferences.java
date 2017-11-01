package com.playposse.ghostphoto;

import android.content.Context;

import com.playposse.ghostphoto.constants.FlashMode;
import com.playposse.ghostphoto.util.BasePreferences;

/**
 * Helper class that makes application preferences accessible.
 */
public final class GhostPhotoPreferences {

    private static final String LOG_CAT = GhostPhotoPreferences.class.getSimpleName();

    private static final String PREFS_NAME = "GhostPhotoPreferences";

    private static final String FLASH_MODE_KEY = "flashMode";
    private static final String HAS_ACTION_BUTTON_HINT_BEEN_SEEN = "hasActionButtonHintBeenSeen";
    private static final String HAS_THUMBNAIL_HINT_BEEN_SEEN = "hasThumbnailHintBeenSeen";
    private static final String HAS_SEEN_INTRO_DECK_KEY = "hasSeenIntroDeck";
    private static final String CUSTOM_PHOTO_INTERVAL_SECONDS_KEY = "customPhotoIntervalSeconds";
    private static final String HAS_ORPHAN_PHOTO_SHOOT_BEEN_CREATED = "hasOrphanPhotoShootBeenCreated";

    private static final FlashMode FLASH_MODE_DEFAULT = FlashMode.auto;
    private static final boolean HINT_DEFAULT = false;
    private static final int CUSTOM_INTERVAL_DEFAULT = 30;

    private static BasePreferences basePreferences = new BasePreferences(PREFS_NAME);

    public static FlashMode getFlashMode(Context context) {
        String flashModeStr = basePreferences.getString(context, FLASH_MODE_KEY);
        if (flashModeStr != null) {
            return FlashMode.valueOf(flashModeStr);
        } else {
            return FLASH_MODE_DEFAULT;
        }
    }

    public static void setFlashMode(Context context, FlashMode flashMode) {
        basePreferences.setString(context, FLASH_MODE_KEY, flashMode.name());
    }

    public static boolean hasActionButtonHintBeenSeen(Context context) {
        return basePreferences.getBoolean(context, HAS_ACTION_BUTTON_HINT_BEEN_SEEN, HINT_DEFAULT);
    }

    public static void setHasActionButtonHintBeenSeen(Context context, boolean flag) {
        basePreferences.setBoolean(context, HAS_ACTION_BUTTON_HINT_BEEN_SEEN, flag);
    }

    public static boolean hasThumbnailHintBeenSeen(Context context) {
        return basePreferences.getBoolean(context, HAS_THUMBNAIL_HINT_BEEN_SEEN, HINT_DEFAULT);
    }

    public static void setHasThumbnailHintBeenSeen(Context context, boolean flag) {
        basePreferences.setBoolean(context, HAS_THUMBNAIL_HINT_BEEN_SEEN, flag);
    }

    public static void setHasSeenIntroDeck(Context context, boolean hasSeenIntroDeck) {
        basePreferences.setBoolean(context, HAS_SEEN_INTRO_DECK_KEY, hasSeenIntroDeck);
    }

    public static boolean hasSeenIntroDeck(Context context) {
        return basePreferences.getBoolean(
                context,
                HAS_SEEN_INTRO_DECK_KEY,
                HINT_DEFAULT);
    }

    public static void setCustomPhotoIntervalSeconds(Context context, int seconds) {
        basePreferences.setInt(context, CUSTOM_PHOTO_INTERVAL_SECONDS_KEY, seconds);
    }

    public static int getCustomPhotoIntervalSeconds(Context context) {
        return basePreferences.getInt(
                context,
                CUSTOM_PHOTO_INTERVAL_SECONDS_KEY, CUSTOM_INTERVAL_DEFAULT);
    }

    public static void setHasOrphanPhotoShootBeenCreated(Context context, boolean flag) {
        basePreferences.setBoolean(context, HAS_ORPHAN_PHOTO_SHOOT_BEEN_CREATED, flag);
    }

    public static boolean hasOrphanPhotoShootBeenCreated(Context context) {
        return basePreferences.getBoolean(context, HAS_ORPHAN_PHOTO_SHOOT_BEEN_CREATED, false);
    }

    public static boolean getBoolean(Context context, String preferenceKey, boolean value) {
        return basePreferences.getBoolean(context, preferenceKey, value);
    }

    public static void setBoolean(Context context, String preferenceKey, boolean value) {
        basePreferences.setBoolean(context, preferenceKey, value);
    }
}

