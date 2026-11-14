package com.meteordevelopments.duels.util;

import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NumberUtil {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\s*v?(\\d+(?:\\.\\d+)*)");

    private NumberUtil() {
    }

    /**
     * Copy of {@link Integer#parseInt(String)} (String)} but returns an empty {@link OptionalInt} instead of throwing a {@link NumberFormatException}.
     *
     * @param s String to parse.
     * @return {@link OptionalInt} instance with parsed value inside or empty if string is invalid.
     */
    public static OptionalInt parseInt(final String s) {
        if (s == null) {
            return OptionalInt.empty();
        }

        int result = 0;
        boolean negative = false;
        int i = 0, len = s.length();
        int limit = -Integer.MAX_VALUE;
        int multmin;
        int digit;

        if (len > 0) {
            char firstChar = s.charAt(0);

            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+') {
                    return OptionalInt.empty();
                }

                if (len == 1) { // Cannot have lone "+" or "-"
                    return OptionalInt.empty();
                }

                i++;
            }

            multmin = limit / 10;

            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(s.charAt(i++), 10);
                if (digit < 0) {
                    return OptionalInt.empty();
                }

                if (result < multmin) {
                    return OptionalInt.empty();
                }

                result *= 10;

                if (result < limit + digit) {
                    return OptionalInt.empty();
                }

                result -= digit;
            }
        } else {
            return OptionalInt.empty();
        }

        return OptionalInt.of(negative ? result : -result);
    }

    public static int getChange(final int k, final int winnerRating, final int loserRating) {
        final double wr = r(winnerRating);
        final double lr = r(loserRating);
        return (int) Math.floor(k * (1 - (wr / (wr + lr))));
    }

    private static double r(final int rating) {
        return Math.pow(10.0, rating / 400.0);
    }

    /**
     * Compares two versions part by part ("8.10" is higher than "8.9").
     * Only the leading numbers count, so any suffix works: "8.3-SNAPSHOT", "8.3-custom2", "8.3+build.5".
     * A version without leading numbers is never treated as lower.
     */
    public static boolean isLower(String version, String otherVersion) {
        final int[] current = versionParts(version);
        final int[] required = versionParts(otherVersion);

        if (current.length == 0 || required.length == 0) {
            return false;
        }

        for (int i = 0; i < Math.max(current.length, required.length); i++) {
            final int a = i < current.length ? current[i] : 0;
            final int b = i < required.length ? required[i] : 0;

            if (a != b) {
                return a < b;
            }
        }

        return false;
    }

    private static int[] versionParts(final String version) {
        if (version == null) {
            return new int[0];
        }

        final Matcher matcher = VERSION_PATTERN.matcher(version);

        if (!matcher.find()) {
            return new int[0];
        }

        final String[] parts = matcher.group(1).split("\\.");
        final int[] result = new int[parts.length];

        for (int i = 0; i < parts.length; i++) {
            result[i] = parseInt(parts[i]).orElse(0);
        }

        return result;
    }
}
