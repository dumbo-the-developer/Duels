package com.meteordevelopments.duels.hook.hooks.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.logging.Level;
import java.util.Objects;

public class WorldGuard7Handler implements WorldGuardHandler {

    @Override
    public String findRegion(final Player player, final Collection<String> regions) {
        // Null checks for input parameters
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (regions == null) {
            throw new IllegalArgumentException("Regions collection cannot be null");
        }
        
        // Handle empty regions collection early
        if (regions.isEmpty()) {
            return null;
        }
        try {
            // Get WorldGuard instance and region container
            RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
            
            // Get region manager for the player's world
            RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(player.getWorld()));
            
            // Handle unloaded worlds - region manager can be null
            if (regionManager == null) {
                return null;
            }
            
            // Convert player location to BlockVector3
            BlockVector3 vector = BlockVector3.at(
                player.getLocation().getBlockX(),
                player.getLocation().getBlockY(), 
                player.getLocation().getBlockZ()
            );
            
            // Get applicable regions at the player's location
            for (ProtectedRegion region : regionManager.getApplicableRegions(vector)) {
                if (regions.contains(region.getId())) {
                    return region.getId();
                }
            }
            
            return null;
        } catch (Exception e) {
            // Log error with proper context instead of printStackTrace
            Bukkit.getLogger().log(Level.WARNING, "Failed to check WorldGuard regions for player " + player.getName(), e);
            return null;
        }
    }
}