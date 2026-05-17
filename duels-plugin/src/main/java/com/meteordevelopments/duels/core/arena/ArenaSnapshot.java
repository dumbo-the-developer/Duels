package com.meteordevelopments.duels.core.arena;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.HashMap;
import java.util.Map;

public class ArenaSnapshot {

    private final Map<Location, BlockData> blocks = new HashMap<>();

    public void capture(Location pos1, Location pos2) {
        if (pos1 == null || pos2 == null || pos1.getWorld() == null || !pos1.getWorld().equals(pos2.getWorld())) {
            return;
        }

        World world = pos1.getWorld();
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        blocks.clear();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    blocks.put(block.getLocation(), block.getBlockData().clone());
                }
            }
        }
    }

    public void restore() {
        blocks.forEach((loc, data) -> loc.getBlock().setBlockData(data, false));
    }
}
