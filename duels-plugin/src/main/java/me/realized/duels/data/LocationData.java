/*
 * This file is part of Duels, licensed under the MIT License.
 *
 * Copyright (c) Realized
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
