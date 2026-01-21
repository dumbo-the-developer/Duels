package com.meteordevelopments.duels.teleport;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.hook.hooks.EssentialsHook;
import com.meteordevelopments.duels.util.Loadable;
import com.meteordevelopments.duels.util.Log;
import com.meteordevelopments.duels.util.metadata.MetadataUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Handles force teleporting of players.
 */
public final class Teleport implements Loadable, Listener {

    public static final String METADATA_KEY = "Duels-Teleport";

    private final DuelsPlugin plugin;

    private EssentialsHook essentials;

    public Teleport(final DuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handleLoad() {
        this.essentials = plugin.getHookManager().getHook(EssentialsHook.class);

        // Late-register the listener to override previously registered listeners
        plugin.doSyncAfter(() -> plugin.registerListener(this), 1L);
    }

    @Override
    public void handleUnload() {
    }

    /**
     * Attempts to force-teleport a player by storing a metadata value in the player before teleportation
     * and uncancelling the teleport by the player in a MONITOR-priority listener if cancelled by other plugins.
     *
     * @param player   Player to force-teleport to a location
     * @param location Location to force-teleport the player
     */
    public void tryTeleport(final Player player, final Location location) {
        tryTeleport(player, location, null);
    }

    /**
     * Attempts to force-teleport a player by storing a metadata value in the player before teleportation
     * and uncancelling the teleport by the player in a MONITOR-priority listener if cancelled by other plugins.
     *
     * @param player   Player to force-teleport to a location
     * @param location Location to force-teleport the player
     * @param onComplete Optional callback to run after teleport completes (or immediately for sync teleport)
     */
    public void tryTeleport(final Player player, final Location location, final Runnable onComplete) {
        if (location == null || location.getWorld() == null) {
            Log.warn(this, "Could not teleport " + player.getName() + "! Location is null");
            return;
        }

        // Schedule passenger removal and bed exit on entity-specific scheduler for Folia compatibility
        DuelsPlugin.getSchedulerAdapter().runTask(player, () -> {
            for (Entity entity : player.getPassengers()) {
                player.removePassenger(entity);
            }
            // Force player to leave bed to prevent teleport failure and duplication exploits
            if (player.isSleeping()) {
                player.wakeup(true);
            }
        });

        // Don't close inventory here for Folia - schedule it after teleport completes to prevent world mismatch
        boolean isFolia = DuelsPlugin.getSchedulerAdapter().isFolia();

        if (essentials != null) {
            essentials.setBackLocation(player, location);
        }

        MetadataUtil.put(plugin, player, METADATA_KEY, location.clone());

        if (isFolia) {
            // Schedule closeInventory after teleport completes to prevent world mismatch and threading errors
            player.teleportAsync(location).thenAccept(success -> {
                if (!success) {
                    Log.warn(this, "Could not teleport " + player.getName() + "! TeleportAsync failed.");
                } else {
                    // Close inventory after teleport completes, on the correct thread and in the correct world
                    DuelsPlugin.getSchedulerAdapter().runTask(player, () -> {
                        player.closeInventory();
                        // Execute callback after teleport completes and inventory is closed
                        if (onComplete != null) {
                            // Schedule callback on entity-specific scheduler to ensure correct thread
                            DuelsPlugin.getSchedulerAdapter().runTask(player, () -> {
                                if (player.isOnline()) {
                                    onComplete.run();
                                }
                            });
                        }
                    });
                }
            });
        } else {
            // For non-Folia, close inventory before synchronous teleport (original behavior)
            DuelsPlugin.getSchedulerAdapter().runTask(player, player::closeInventory);
            if (!player.teleport(location)) {
                Log.warn(this, "Could not teleport " + player.getName() + "! Player is dead or is vehicle");
            } else if (onComplete != null) {
                // For sync teleport, run callback immediately
                onComplete.run();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(final PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        final Object value = MetadataUtil.removeAndGet(plugin, player, METADATA_KEY);

        // Only handle the case where teleport is cancelled and player has force teleport metadata value
        if (!event.isCancelled() || value == null) {
            return;
        }

        event.setCancelled(false);
        event.setTo((Location) value);
    }
}
