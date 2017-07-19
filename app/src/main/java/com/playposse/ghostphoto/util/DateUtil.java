package com.playposse.ghostphoto.util;

import java.util.Date;

/**
 * A utility for dealing with dates.
 */
public final class DateUtil {

    private DateUtil() {}

    public static long getMinutesDiff(Date date0, Date date1) {
        long diff = Math.abs(date0.getTime() - date1.getTime());
        return diff / ( 60 * 1_000);
    }

    public static boolean isLessThan60MinutesAgo(Date date) {
        long diff = new Date().getTime() - date.getTime();
        return diff < 60 * 60 * 1_000;
    }

    public static boolean isToday(Date date) {
        Date now = new Date();

        return (date.getDate() == now.getDate())
                && (date.getMonth() == now.getMonth())
                && (date.getYear() == now.getYear());
    }
}
