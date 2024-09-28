package com.meteordevelopments.duels.util;

import com.meteordevelopments.duels.util.reflect.ReflectionUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;

public final class StringUtil {

    private static final Pattern ALPHANUMERIC = Pattern.compile("^[a-zA-Z0-9_]+$");
    private static final TreeMap<Integer, String> ROMAN_NUMERALS = new TreeMap<>();
    private static final boolean COMMONS_LANG3;

    static {
        ROMAN_NUMERALS.put(1000, "M");
        ROMAN_NUMERALS.put(900, "CM");
        ROMAN_NUMERALS.put(500, "D");
        ROMAN_NUMERALS.put(400, "CD");
        ROMAN_NUMERALS.put(100, "C");
        ROMAN_NUMERALS.put(90, "XC");
        ROMAN_NUMERALS.put(50, "L");
        ROMAN_NUMERALS.put(40, "XL");
        ROMAN_NUMERALS.put(10, "X");
        ROMAN_NUMERALS.put(9, "IX");
        ROMAN_NUMERALS.put(5, "V");
        ROMAN_NUMERALS.put(4, "IV");
        ROMAN_NUMERALS.put(1, "I");
        COMMONS_LANG3 = ReflectionUtil.getClassUnsafe("org.apache.commons.lang3.StringUtils") != null;
    }

    private StringUtil() {
    }

    public static String toRoman(final int number) {
        if (number <= 0) {
            return String.valueOf(number);
        }

        int key = ROMAN_NUMERALS.floorKey(number);

        if (number == key) {
            return ROMAN_NUMERALS.get(number);
        }

        return ROMAN_NUMERALS.get(key) + toRoman(number - key);
    }

    public static String fromList(final List<?> list) {
        StringBuilder builder = new StringBuilder();

        if (list != null && !list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                builder.append(list.get(i).toString()).append(i + 1 != list.size() ? "\n" : "");
            }
        }

        return builder.toString();
    }

    public static String parse(final Location location) {
        return "(" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")";
    }

    public static String color(final String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static List<String> color(final List<String> input) {
        input.replaceAll(StringUtil::color);
        return input;
    }

    public static boolean isAlphanumeric(final String input) {
        return ALPHANUMERIC.matcher(input.replace(" ", "")).matches();
    }

    public static String join(final Object[] array, final String separator, final int startIndex, final int endIndex) {
        return COMMONS_LANG3
                ? org.apache.commons.lang3.StringUtils.join(array, separator, startIndex, endIndex)
                : org.apache.commons.lang.StringUtils.join(array, separator, startIndex, endIndex);
    }

    public static String join(final Collection<?> collection, final String separator) {
        return COMMONS_LANG3
                ? org.apache.commons.lang3.StringUtils.join(collection, separator)
                : org.apache.commons.lang.StringUtils.join(collection, separator);
    }

    public static String capitalize(final String s) {
        return COMMONS_LANG3
                ? org.apache.commons.lang3.StringUtils.capitalize(s)
                : org.apache.commons.lang.StringUtils.capitalize(s);
    }

    public static boolean containsIgnoreCase(final String str, final String searchStr) {
        return COMMONS_LANG3
                ? org.apache.commons.lang3.StringUtils.containsIgnoreCase(str, searchStr)
                : org.apache.commons.lang.StringUtils.containsIgnoreCase(str, searchStr);
    }
}