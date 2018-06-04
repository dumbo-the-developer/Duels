/*
 * This file is part of Duels, licensed under the MIT License.
 *
 * Copyright (c) Realized
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

    public static String format(long seconds) {
        if (seconds <= 0) {
            return "updating...";
        }

        long years = seconds / 31556952;
        seconds -= years * 31556952;
        long months = seconds / 2592000;
        seconds -= months * 2592000;
        long weeks = seconds / 604800;
        seconds -= weeks * 604800;
        long days = seconds / 86400;
        seconds -= days * 86400;
        long hours = seconds / 3600;
        seconds -= hours * 3600;
        long minutes = seconds / 60;
        seconds -= minutes * 60;

        StringBuilder sb = new StringBuilder();

        if (years > 0) {
            sb.append(years).append("yr");
        }

        if (months > 0) {
            sb.append(months).append("mo");
        }

        if (weeks > 0) {
            sb.append(weeks).append("w");
        }

        if (days > 0) {
            sb.append(days).append("d");
        }

        if (hours > 0) {
            sb.append(hours).append("h");
        }

        if (minutes > 0) {
            sb.append(minutes).append("m");
        }

        if (seconds > 0) {
            sb.append(seconds).append("s");
        }

        return sb.toString();
    }
}
