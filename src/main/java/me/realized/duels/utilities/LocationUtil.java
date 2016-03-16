package me.realized.duels.utilities;

import org.bukkit.Location;

public class LocationUtil {

    public static String format(Location location) {
        return "(" + location.getWorld().getName() + ", " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")";
    }
}
