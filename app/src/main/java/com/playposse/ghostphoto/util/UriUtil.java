package com.playposse.ghostphoto.util;

import android.net.Uri;

/**
 * A utility for dealing with {@link Uri}s.
 */
public class UriUtil {

    private UriUtil() {}

    public static Uri tryParse(String uri) {
        if (uri != null) {
            return Uri.parse(uri);
        } else {
            return null;
        }
    }
}
