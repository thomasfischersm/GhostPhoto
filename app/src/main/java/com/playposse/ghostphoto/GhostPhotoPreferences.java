package com.playposse.ghostphoto;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.playposse.ghostphoto.constants.FlashMode;

import java.util.HashSet;
import java.util.Set;

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

    private static final String NULL_STRING = "-1";
    private static final int NULL_VALUE = -1;

    public static FlashMode getFlashMode(Context context) {
        String flashModeStr = getString(context, FLASH_MODE_KEY);
        if (flashModeStr != null) {
            return FlashMode.valueOf(flashModeStr);
        } else {
            return FLASH_MODE_DEFAULT;
        }
    }

    public static void setFlashMode(Context context, FlashMode flashMode) {
        setString(context, FLASH_MODE_KEY, flashMode.name());
    }

    public static boolean hasActionButtonHintBeenSeen(Context context) {
        return getBoolean(context, HAS_ACTION_BUTTON_HINT_BEEN_SEEN, HINT_DEFAULT);
    }

    public static void setHasActionButtonHintBeenSeen(Context context, boolean flag) {
        setBoolean(context, HAS_ACTION_BUTTON_HINT_BEEN_SEEN, flag);
    }

    public static boolean hasThumbnailHintBeenSeen(Context context) {
        return getBoolean(context, HAS_THUMBNAIL_HINT_BEEN_SEEN, HINT_DEFAULT);
    }

    public static void setHasThumbnailHintBeenSeen(Context context, boolean flag) {
        setBoolean(context, HAS_THUMBNAIL_HINT_BEEN_SEEN, flag);
    }

    public static void setHasSeenIntroDeck(Context context, boolean hasSeenIntroDeck) {
        setBoolean(context, HAS_SEEN_INTRO_DECK_KEY, hasSeenIntroDeck);
    }

    public static boolean hasSeenIntroDeck(Context context) {
        return getBoolean(
                context,
                HAS_SEEN_INTRO_DECK_KEY,
                HINT_DEFAULT);
    }

    public static void setCustomPhotoIntervalSeconds(Context context, int seconds) {
        setInt(context, CUSTOM_PHOTO_INTERVAL_SECONDS_KEY, seconds);
    }

    public static int getCustomPhotoIntervalSeconds(Context context) {
        return getInt(context, CUSTOM_PHOTO_INTERVAL_SECONDS_KEY, CUSTOM_INTERVAL_DEFAULT);
    }

    public static void setHasOrphanPhotoShootBeenCreated(Context context, boolean flag) {
        setBoolean(context, HAS_ORPHAN_PHOTO_SHOOT_BEEN_CREATED, flag);
    }

    public static boolean hasOrphanPhotoShootBeenCreated(Context context) {
        return getBoolean(context, HAS_ORPHAN_PHOTO_SHOOT_BEEN_CREATED, false);
    }

    private static String getString(Context context, String key) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String str = sharedPreferences.getString(key, NULL_STRING);
        return (!NULL_STRING.equals(str)) ? str : null;
    }

    private static void setString(Context context, String key, String value) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (value != null) {
            sharedPreferences.edit().putString(key, value).commit();
        } else {
            sharedPreferences.edit().remove(key).commit();
        }
    }

    private static Integer getInt(Context context, String key) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Integer value = sharedPreferences.getInt(key, -1);
        return (value != -1) ? value : null;
    }

    private static int getInt(Context context, String key, int defaultValue) {
        Integer value = getInt(context, key);
        return (value != null) ? value : defaultValue;
    }

    private static void setInt(Context context, String key, Integer value) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (value != null) {
            sharedPreferences.edit().putInt(key, value).commit();
        } else {
            sharedPreferences.edit().remove(key).commit();
        }
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        try {
            SharedPreferences sharedPreferences =
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean(key, defaultValue);
        } catch (ClassCastException ex) {
            setBoolean(context, key, defaultValue);
            return false;
        }
    }

    public static void setBoolean(Context context, String key, boolean value) {
        Log.i(LOG_CAT, "Setting preference boolean for key " + key + " to " + value);
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferences
                .edit()
                .putBoolean(key, value)
                .commit();
    }

    private static Set<Long> getLongSet(Context context, String key) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> set = sharedPreferences.getStringSet(key, null);

        if ((set == null) || (set.size() == 0)) {
            return new HashSet<>();
        }

        Set<Long> result = new HashSet<>(set.size());
        for (String value : set) {
            result.add(Long.valueOf(value));
        }
        return result;
    }

    private static void setLongSet(Context context, String key, Set<Long> set) {
        Set<String> stringSet = new HashSet<>(set.size());
        for (Long value : set) {
            stringSet.add(value.toString());
        }

        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putStringSet(key, stringSet).commit();
    }

    private static void addValueToLongSet(Context context, String key, Long value) {
        Set<Long> set = getLongSet(context, key);
        set.add(value);
        setLongSet(context, key, set);
    }

    private static Set<String> getStringSet(Context context, String key) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> stringSet = sharedPreferences.getStringSet(key, null);
        if (stringSet != null) {
            return stringSet;
        } else {
            return new HashSet<>();
        }
    }

    private static void setStringSet(Context context, String key, Set<String> set) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putStringSet(key, set).commit();
    }

    private static void addValueToStringSet(Context context, String key, String value) {
        Set<String> set = getStringSet(context, key);
        set.add(value);
        setStringSet(context, key, set);
    }

    private static Long getLong(Context context, String key) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Long value = sharedPreferences.getLong(key, NULL_VALUE);
        return (value != NULL_VALUE) ? value : null;
    }

    private static void setLong(Context context, String key, Long value) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (value != null) {
            sharedPreferences.edit().putLong(key, value).commit();
        } else {
            sharedPreferences.edit().remove(key).commit();
        }
    }

    private static Double getDouble(Context context, String key) {
        String str = getString(context, key);
        return (str != null) ? Double.parseDouble(str) : null;
    }

    private static void setDouble(Context context, String key, Double value) {
        if (value != null) {
            setString(context, key, value.toString());
        } else {
            setString(context, key, null);
        }
    }

    /**
     * Throws away all the local preference data.
     */
    public static void reset(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .clear()
                .commit();
    }
}

