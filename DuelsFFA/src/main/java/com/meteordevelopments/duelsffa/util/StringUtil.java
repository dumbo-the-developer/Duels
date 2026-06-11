package com.meteordevelopments.duelsffa.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtil {

    private StringUtil() {
    }

    public static String color(String s) {
        if (s == null) return "";
        if (Pattern.compile("&#[0-9A-f]{6}").matcher(s).find()) {
            Matcher matcher = Pattern.compile("&(#[0-9A-f]{6})").matcher(s);
            while (matcher.find()) {
                s = s.replaceFirst(
                        matcher.group(),
                        ChatColor.of(matcher.group(1)).toString()
                );
            }
        }
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String from(final Location location) {
        return "(" + location.getWorld().getName() + ", " + location.getX() + ", " + location.getY() + ", " + location.getZ() + ")";
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
}
