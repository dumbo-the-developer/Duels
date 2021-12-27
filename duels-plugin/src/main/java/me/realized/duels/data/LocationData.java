package me.realized.duels.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationData {

    public static LocationData fromLocation(final Location location) {
        return new LocationData(location);
    }

    private String world;
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;

    private LocationData() {}

    private LocationData(final World world, final double x, final double y, final double z, final float pitch, final float yaw) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world.getName();
        this.pitch = pitch;
        this.yaw = yaw;
    }

    private LocationData(final Location location) {
        this(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
    }

    public World getWorld() {
        return Bukkit.getWorld(world);
    }

    public Location toLocation() {
        return new Location(getWorld(), x, y, z, yaw, pitch);
    }
}
