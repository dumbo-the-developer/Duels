package me.realized.duels.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateUtil {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

    private DateUtil() {}

    public static String formatDate(final Date date) {
        return DATE_FORMAT.format(date);
    }

    public static String formatDatetime(final long millis) {
        return TIMESTAMP_FORMAT.format(millis);
    }
}
