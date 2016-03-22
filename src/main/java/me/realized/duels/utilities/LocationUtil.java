package me.realized.duels.utilities;

import org.bukkit.Location;

public class LocationUtil {

    public static String format(Location location) {
        if (location == null || location.getWorld() == null) {
            return "Invalid world or location! (Please re-set spawnpoints)";
        }

        return "(" + location.getWorld().getName() + ", " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")";
    }
}
