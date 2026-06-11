package com.meteordevelopments.duelsffa.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class LocationUtil {

    private LocationUtil() {
    }

    public static String serialize(final Location location) {
        if (location == null || location.getWorld() == null) {
            return "";
        }
        return location.getWorld().getName() + ";" + location.getX() + ";" + location.getY() + ";" + location.getZ() + ";" + location.getYaw() + ";" + location.getPitch();
    }

    public static Location deserialize(final String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String[] parts = value.split(";");
        if (parts.length < 4) {
            return null;
        }
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) {
            return null;
        }
        try {
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0.0f;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0.0f;
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static List<String> serializeAll(final Collection<Location> locations) {
        List<String> values = new ArrayList<>();
        if (locations == null) return values;
        for (Location location : locations) {
            String serialized = serialize(location);
            if (!serialized.isEmpty()) {
                values.add(serialized);
            }
        }
        return values;
    }

    public static List<Location> deserializeAll(final Collection<String> values) {
        List<Location> locations = new ArrayList<>();
        if (values == null) return locations;
        for (String value : values) {
            Location location = deserialize(value);
            if (location != null) {
                locations.add(location);
            }
        }
        return locations;
    }
}
