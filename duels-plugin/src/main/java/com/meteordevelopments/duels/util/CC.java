package com.meteordevelopments.duels.util;

import org.bukkit.ChatColor;

public class CC {

    public static String translate(String message){
        return ChatColor.translateAlternateColorCodes('&', message);
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