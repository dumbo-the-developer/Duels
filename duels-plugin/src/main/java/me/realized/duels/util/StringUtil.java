package me.realized.duels.util;

import java.util.List;
import java.util.function.Function;
import org.bukkit.ChatColor;

public final class StringUtil {

    private StringUtil() {}

    public static String fromList(final List<?> list) {
        StringBuilder builder = new StringBuilder();

        if (list != null && !list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                builder.append(list.get(i).toString()).append(i + 1 != list.size() ? "\n" : "");
            }
        }

        return builder.toString();
    }

    public static String color(final String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static List<String> color(final List<String> input) {
        return color(input, null);
    }

    public static List<String> color(final List<String> input, final Function<String, String> function) {
        input.replaceAll(s -> s = color(function != null ? function.apply(s) : s));
        return input;
    }
}
