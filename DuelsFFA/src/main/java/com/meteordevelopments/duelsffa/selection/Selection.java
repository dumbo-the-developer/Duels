package com.meteordevelopments.duelsffa.selection;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Location;
import org.bukkit.World;

public class Selection {

    private Location first;
    private Location second;
    private Region region;
    private World world;

    public void setFirst(final Location first) {
        this.first = first;
        this.world = first == null ? this.world : first.getWorld();
        this.region = null;
    }

    public void setSecond(final Location second) {
        this.second = second;
        this.world = second == null ? this.world : second.getWorld();
        this.region = null;
    }

    public Location getFirst() {
        return first;
    }

    public Location getSecond() {
        return second;
    }

    public World getWorld() {
        if (this.world != null) {
            return this.world;
        }
        if (this.first != null) {
            return this.first.getWorld();
        }
        return this.second == null ? null : this.second.getWorld();
    }

    public Region toRegion() {
        if (this.region != null) {
            return this.region;
        }
        if (this.first == null || this.second == null || this.getWorld() == null) {
            return null;
        }

        BlockVector3 firstPoint = BlockVector3.at(this.first.getBlockX(), this.first.getBlockY(), this.first.getBlockZ());
        BlockVector3 secondPoint = BlockVector3.at(this.second.getBlockX(), this.second.getBlockY(), this.second.getBlockZ());
        return new CuboidRegion(BukkitAdapter.adapt(this.getWorld()), firstPoint, secondPoint);
    }

    public void clear() {
        this.first = null;
        this.second = null;
        this.region = null;
        this.world = null;
    }

    public boolean isSelected() {
        return this.region != null || (this.first != null && this.second != null);
    }
}
