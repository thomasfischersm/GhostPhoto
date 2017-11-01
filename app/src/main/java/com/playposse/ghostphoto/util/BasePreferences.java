package com.playposse.ghostphoto.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * A helper class that makes it easier to deal with preferences.
 */
public class BasePreferences {

    private static final String LOG_TAG = BasePreferences.class.getSimpleName();

    private static final String NULL_STRING = "-1";
    private static final int NULL_VALUE = -1;

    private final String preferencesName;

    public BasePreferences(String preferencesName) {
        this.preferencesName = preferencesName;
    }

    public SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
    }

    public String getString(Context context, String key) {
        String str = getSharedPreferences(context).getString(key, NULL_STRING);
        return (!NULL_STRING.equals(str)) ? str : null;
    }

    public void setString(Context context, String key, String value) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        if (value != null) {
            sharedPreferences.edit().putString(key, value).commit();
        } else {
            sharedPreferences.edit().remove(key).commit();
        }
    }

    public Integer getInt(Context context, String key) {
        Integer value = getSharedPreferences(context).getInt(key, -1);
        return (value != -1) ? value : null;
    }

    public int getInt(Context context, String key, int defaultValue) {
        Integer value = getInt(context, key);
        return (value != null) ? value : defaultValue;
    }

    public void setInt(Context context, String key, Integer value) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        if (value != null) {
            sharedPreferences.edit().putInt(key, value).commit();
        } else {
            sharedPreferences.edit().remove(key).commit();
        }
    }

    public boolean getBoolean(Context context, String key, boolean defaultValue) {
        try {
            return getSharedPreferences(context).getBoolean(key, defaultValue);
        } catch (ClassCastException ex) {
            setBoolean(context, key, defaultValue);
            return false;
        }
    }

    public void setBoolean(Context context, String key, boolean value) {
        Log.i(LOG_TAG, "Setting preference boolean for key " + key + " to " + value);
        getSharedPreferences(context)
                .edit()
                .putBoolean(key, value)
                .commit();
    }

    public Set<Long> getLongSet(Context context, String key) {
        Set<String> set = getSharedPreferences(context).getStringSet(key, null);

        if ((set == null) || (set.size() == 0)) {
            return new HashSet<>();
        }

        Set<Long> result = new HashSet<>(set.size());
        for (String value : set) {
            result.add(Long.valueOf(value));
        }
        return result;
    }

    public void setLongSet(Context context, String key, Set<Long> set) {
        Set<String> stringSet = new HashSet<>(set.size());
        for (Long value : set) {
            stringSet.add(value.toString());
        }

        getSharedPreferences(context).edit().putStringSet(key, stringSet).commit();
    }

    public void addValueToLongSet(Context context, String key, Long value) {
        Set<Long> set = getLongSet(context, key);
        set.add(value);
        setLongSet(context, key, set);
    }

    public Set<String> getStringSet(Context context, String key) {
        Set<String> stringSet = getSharedPreferences(context).getStringSet(key, null);
        if (stringSet != null) {
            return stringSet;
        } else {
            return new HashSet<>();
        }
    }

    public void setStringSet(Context context, String key, Set<String> set) {
        getSharedPreferences(context).edit().putStringSet(key, set).commit();
    }

    public void addValueToStringSet(Context context, String key, String value) {
        Set<String> set = getStringSet(context, key);
        set.add(value);
        setStringSet(context, key, set);
    }

    public Long getLong(Context context, String key) {
        Long value = getSharedPreferences(context).getLong(key, NULL_VALUE);
        return (value != NULL_VALUE) ? value : null;
    }

    public void setLong(Context context, String key, Long value) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        if (value != null) {
            sharedPreferences.edit().putLong(key, value).commit();
        } else {
            sharedPreferences.edit().remove(key).commit();
        }
    }

    public Double getDouble(Context context, String key) {
        String str = getString(context, key);
        return (str != null) ? Double.parseDouble(str) : null;
    }

    public void setDouble(Context context, String key, Double value) {
        if (value != null) {
            setString(context, key, value.toString());
        } else {
            setString(context, key, null);
        }
    }

    /**
     * Throws away all the local preference data.
     */
    public void reset(Context context) {
        getSharedPreferences(context).edit()
                .clear()
                .commit();
    }
}
