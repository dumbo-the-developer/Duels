package com.meteordevelopments.duels.util.util;


import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class CC {

    public static String translate(String input){
        if (input == null) return "";
        if (Pattern.compile("&#[0-9A-f]{6}").matcher(input).find()) {
            Matcher matcher = Pattern.compile("&(#[0-9A-f]{6})").matcher(input);
            while (matcher.find()) {
                input = input.replaceFirst(
                        matcher.group(),
                        ChatColor.of(matcher.group(1)).toString()
                );
            }
        }
        return ChatColor.translateAlternateColorCodes('&', input);
    }
    public static String getTimeDifferenceAndColor(long start, long end) {
        return getColorBasedOnSize((end - start), 20, 5000, 10000) + "" + (end - start) + "ms";
    }
    public static ChatColor getColorBasedOnSize(long num, int low, int med, int high) {
        if (num <= low) {
            return ChatColor.GREEN;
        } else if (num <= med) {
            return ChatColor.YELLOW;
        } else if (num <= high) {
            return ChatColor.RED;
        } else {
            return ChatColor.DARK_RED;
        }
    }
}
