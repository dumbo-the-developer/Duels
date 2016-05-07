package me.realized.duels.utilities;

import org.bukkit.ChatColor;

import java.util.List;
import java.util.regex.Pattern;

public class StringUtil {

    private static final Pattern ALPHANUMERIC = Pattern.compile("^[a-zA-Z0-9]*$");

    public static boolean isAlphanumeric(String input) {
        return ALPHANUMERIC.matcher(input).matches();
    }

    public static String toHumanReadableTime(long ms) {
        if (ms < 1000) {
            return "0 second";
        }

        long seconds = ms / 1000 + (ms % 1000 > 0 ? 1 : 0);
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

        StringBuilder builder = new StringBuilder();

        if (years > 0) {
            builder.append(years).append(years > 1 ? " years" : " year");
        }

        if (months > 0) {
            if (years > 0) {
                builder.append(" ");
            }

            builder.append(months).append(months > 1 ? " months" : " month");
        }

        if (weeks > 0) {
            if (years + months > 0) {
                builder.append(" ");
            }

            builder.append(weeks).append(weeks > 1 ? " weeks" : " week");
        }

        if (days > 0) {
            if (years + months + weeks > 0) {
                builder.append(" ");
            }

            builder.append(days).append(days > 1 ? " days" : " day");
        }

        if (hours > 0) {
            if (years + months + weeks + days > 0) {
                builder.append(" ");
            }

            builder.append(hours).append(hours > 1 ? " hours" : " hour");
        }

        if (minutes > 0) {
            if (years + months + weeks + days + hours > 0) {
                builder.append(" ");
            }

            builder.append(minutes).append(minutes > 1 ? " minutes" : " minute");
        }

        if (seconds > 0) {
            if (years + months + weeks + days + hours + minutes > 0) {
                builder.append(" ");
            }

            builder.append(seconds).append(seconds > 1 ? " seconds" : " second");
        }

        return builder.toString();
    }

    public static String replaceWithArgs(String txt, Object... parameters) {
        for (int i = 0; i < parameters.length; i++) {
            if (parameters.length > i + 1) {
                String current = String.valueOf(parameters[i]);
                String next = String.valueOf(parameters[i + 1]);
                txt = txt.replace(current, next);
                i++;
            }
        }

        return txt;
    }

    public static String join(List<String> list, String joiner) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < list.size() - 1; i++) {
            builder.append(list.get(i)).append(joiner);
        }

        return builder.append(list.get(list.size() - 1)).toString();
    }

    public static String color(String txt) {
        return ChatColor.translateAlternateColorCodes('&', txt);
    }
}
