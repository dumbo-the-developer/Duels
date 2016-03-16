package me.realized.duels.utilities;

public class NumberUtil {

    public static boolean isInt(String input, boolean negative) {
        try {
            int result = Integer.parseInt(input);
            return !(!negative && result < 0);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
