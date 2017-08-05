package com.playposse.ghostphoto.util;

/**
 * Helpful methods for dealing with strings.
 */
public class StringUtil {

    public static boolean isEmpty(String str) {
        return (str == null) || (str.trim().length() == 0);
    }

    public static int countOccurrencesOf(String str, char c) {
        if (isEmpty(str)) {
            return 0;
        }

        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }
}
