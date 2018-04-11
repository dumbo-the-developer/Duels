package me.realized.duels.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationData {

    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private float pitch;
    private float yaw;

    public LocationData(World world, double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world.getName();
    }

    public LocationData(World world, double x, double y, double z, float pitch, float yaw) {
        this(world, x, y, z);
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public LocationData(Location location) {
        this(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
    }

    public World getWorld() {
        return Bukkit.getWorld(world);
    }

    public Location toLocation() {
        return new Location(getWorld(), x, y, z, yaw, pitch);
    }
}
