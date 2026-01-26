package com.meteordevelopments.duels.listeners;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

/**
 * Prevents players from being in duel worlds when they're not in a match.
 * Teleports them to spawn if they log into or enter a duel world without being in a match.
 * This is an edge-case safety feature that prevents players from logging into arena worlds
 * when they shouldn't be there (e.g., after server crash, plugin reload, or unexpected disconnections).
 */
public class DuelWorldsListener implements Listener {

    private final DuelsPlugin plugin;
    private final Config config;
    private final ArenaManagerImpl arenaManager;

    public DuelWorldsListener(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.arenaManager = plugin.getArenaManager();

        // Only register if feature is enabled
        if (config.isDuelWorldsEnabled()) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }
    }

    /**
     * Normalizes world name to handle namespaced worlds (e.g., "minecraft:world" -> "world")
     * Matches the implementation in DuelManager for consistency.
     */
    private String normalizeWorldName(final String worldName) {
        if (worldName == null) {
            return "";
        }
        // Remove namespace prefix if present (e.g., "minecraft:world" -> "world")
        if (worldName.contains(":")) {
            return worldName.substring(worldName.indexOf(":") + 1);
        }
        return worldName;
    }

    /**
     * Checks if the given world is in the duel-worlds list
     */
    private boolean isDuelWorld(final World world) {
        if (world == null || !config.isDuelWorldsEnabled()) {
            return false;
        }

        final List<String> duelWorlds = config.getDuelWorlds();
        if (duelWorlds == null || duelWorlds.isEmpty()) {
            return false;
        }

        final String worldName = normalizeWorldName(world.getName());
        return duelWorlds.stream()
                .anyMatch(duelWorld -> normalizeWorldName(duelWorld).equalsIgnoreCase(worldName));
    }

    /**
     * Checks if player should be teleported away from duel world
     */
    private boolean shouldTeleportAway(final Player player) {
        // Skip if player is in a match (they're allowed to be in duel world)
        if (arenaManager.isInMatch(player)) {
            return false;
        }

        // Check if player is in a duel world
        final World playerWorld = player.getWorld();
        return isDuelWorld(playerWorld);
    }

    /**
     * Teleports player to spawn location
     */
    private void teleportToSpawn(final Player player) {
        final World world = player.getWorld();
        if (world == null) {
            return;
        }

        final Location spawnLocation = world.getSpawnLocation();
        
        // Use Folia-compatible teleport
        DuelsPlugin.getSchedulerAdapter().runTask(player, () -> {
            if (player.isOnline() && player.getWorld() == world) {
                plugin.getTeleport().tryTeleport(player, spawnLocation, null);
                
                // Apply gamemode if configured
                if (config.isForceGamemodeOnJoin()) {
                    final GameMode gamemode = config.getForceGamemodeOnJoinMode();
                    if (gamemode != null) {
                        DuelsPlugin.getSchedulerAdapter().runTask(player, () -> {
                            if (player.isOnline()) {
                                player.setGameMode(gamemode);
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * Handles player join - teleports them away if they're in a duel world
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        
        // Delay check to ensure player is fully loaded
        DuelsPlugin.getSchedulerAdapter().runTaskLater(player, () -> {
            if (player.isOnline() && shouldTeleportAway(player)) {
                teleportToSpawn(player);
            }
        }, 5L); // 5 tick delay to ensure player is fully loaded
    }

    /**
     * Handles world change - teleports them away if they enter a duel world
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
        final Player player = event.getPlayer();
        final World newWorld = event.getPlayer().getWorld();
        
        // Only check if new world is a duel world
        if (isDuelWorld(newWorld) && shouldTeleportAway(player)) {
            teleportToSpawn(player);
        }
    }

    /**
     * Handles teleport - prevents teleporting to duel worlds if not in match
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        final Location to = event.getTo();
        
        if (to == null || to.getWorld() == null) {
            return;
        }

        // Check if teleporting TO a duel world
        if (isDuelWorld(to.getWorld()) && !arenaManager.isInMatch(player)) {
            // Cancel teleport and teleport to spawn instead
            event.setCancelled(true);
            teleportToSpawn(player);
        }
    }
}

