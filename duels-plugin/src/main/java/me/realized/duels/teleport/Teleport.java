package me.realized.duels.teleport;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.hook.hooks.EssentialsHook;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.metadata.MetadataUtil;
import org.bukkit.Location;
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
    public void handleUnload() {}

    /**
     * Attempts to force-teleport a player by storing a metadata value in the player before teleportation
     * and uncancelling the teleport by the player in a MONITOR-priority listener if cancelled by other plugins.
     *
     * @param player Player to force-teleport to a location
     * @param location Location to force-teleport the player
     */
    public void tryTeleport(final Player player, final Location location) {
        if (location == null || location.getWorld() == null) {
            Log.warn(this, "Could not teleport " + player.getName() + "! Location is null");
            return;
        }

        if (essentials != null) {
            essentials.setBackLocation(player, location);
        }

        MetadataUtil.put(plugin, player, METADATA_KEY, location.clone());

        if (!player.teleport(location)) {
            Log.warn(this, "Could not teleport " + player.getName() + "! Player is dead or is vehicle");
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
