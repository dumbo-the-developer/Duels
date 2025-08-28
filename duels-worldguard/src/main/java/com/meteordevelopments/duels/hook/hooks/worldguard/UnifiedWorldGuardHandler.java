package com.meteordevelopments.duels.hook.hooks.worldguard;

import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Collection;

public class UnifiedWorldGuardHandler implements WorldGuardHandler {
    
    private final boolean isVersion7;
    private final Method getRegionManagerMethod;
    private final Object worldGuardInstance;
    private final Class<?> protectedRegionClass;
    private final Method getIdMethod;
    
    public UnifiedWorldGuardHandler() throws Exception {
        // Detect WorldGuard version by checking for v7 classes
        isVersion7 = isClassAvailable("com.sk89q.worldguard.WorldGuard");
        
        if (isVersion7) {
            // Initialize WorldGuard v7 components
            Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Method getInstanceMethod = worldGuardClass.getMethod("getInstance");
            worldGuardInstance = getInstanceMethod.invoke(null);
            
            protectedRegionClass = Class.forName("com.sk89q.worldguard.protection.regions.ProtectedRegion");
            getIdMethod = protectedRegionClass.getMethod("getId");
            
            // We'll set this in the findRegion method since it's more complex for v7
            getRegionManagerMethod = null;
        } else {
            // Initialize WorldGuard v6 components
            Class<?> worldGuardPluginClass = Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin");
            Method instMethod = worldGuardPluginClass.getMethod("inst");
            worldGuardInstance = instMethod.invoke(null);
            
            protectedRegionClass = Class.forName("com.sk89q.worldguard.protection.regions.ProtectedRegion");
            getIdMethod = protectedRegionClass.getMethod("getId");
            
            // Get the getRegionManager method for v6
            getRegionManagerMethod = worldGuardPluginClass.getMethod("getRegionManager", 
                Class.forName("org.bukkit.World"));
        }
    }
    
    @Override
    public String findRegion(final Player player, final Collection<String> regions) {
        try {
            if (isVersion7) {
                return findRegionV7(player, regions);
            } else {
                return findRegionV6(player, regions);
            }
        } catch (Exception e) {
            // Log error but don't crash the plugin
            e.printStackTrace();
            return null;
        }
    }
    
    private String findRegionV7(Player player, Collection<String> regions) throws Exception {
        // Get WorldGuard instance -> getPlatform() -> getRegionContainer() -> get(world) -> getApplicableRegions(vector)
        Object platform = worldGuardInstance.getClass().getMethod("getPlatform").invoke(worldGuardInstance);
        Object regionContainer = platform.getClass().getMethod("getRegionContainer").invoke(platform);
        
        // Convert Bukkit world to WorldEdit world using BukkitAdapter
        Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
        Method adaptWorldMethod = bukkitAdapterClass.getMethod("adapt", Class.forName("org.bukkit.World"));
        Object worldEditWorld = adaptWorldMethod.invoke(null, player.getWorld());
        
        // Get region manager for the world
        Method getMethod = regionContainer.getClass().getMethod("get", Class.forName("com.sk89q.worldedit.world.World"));
        Object regionManager = getMethod.invoke(regionContainer, worldEditWorld);
        
        if (regionManager == null) {
            return null;
        }
        
        // Convert location to BlockVector3
        Class<?> blockVector3Class = Class.forName("com.sk89q.worldedit.math.BlockVector3");
        Method atMethod = blockVector3Class.getMethod("at", int.class, int.class, int.class);
        Object vector = atMethod.invoke(null, player.getLocation().getBlockX(), 
                                             player.getLocation().getBlockY(), 
                                             player.getLocation().getBlockZ());
        
        // Get applicable regions
        Method getApplicableRegionsMethod = regionManager.getClass().getMethod("getApplicableRegions", blockVector3Class);
        Object applicableRegions = getApplicableRegionsMethod.invoke(regionManager, vector);
        
        // Iterate through regions
        Iterable<?> regionIterable = (Iterable<?>) applicableRegions;
        for (Object region : regionIterable) {
            String regionId = (String) getIdMethod.invoke(region);
            if (regions.contains(regionId)) {
                return regionId;
            }
        }
        
        return null;
    }
    
    private String findRegionV6(Player player, Collection<String> regions) throws Exception {
        // Get region manager for the world
        Object regionManager = getRegionManagerMethod.invoke(worldGuardInstance, player.getWorld());
        
        // Handle unloaded worlds - region manager can be null
        if (regionManager == null) {
            return null;
        }
        
        // Get applicable regions
        Method getApplicableRegionsMethod = regionManager.getClass().getMethod("getApplicableRegions", 
            Class.forName("org.bukkit.Location"));
        Object applicableRegions = getApplicableRegionsMethod.invoke(regionManager, player.getLocation());
        
        // Iterate through regions
        Iterable<?> regionIterable = (Iterable<?>) applicableRegions;
        for (Object region : regionIterable) {
            String regionId = (String) getIdMethod.invoke(region);
            if (regions.contains(regionId)) {
                return regionId;
            }
        }
        
        return null;
    }
    
    private boolean isClassAvailable(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}