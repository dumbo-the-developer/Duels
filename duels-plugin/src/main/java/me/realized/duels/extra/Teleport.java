package me.realized.duels.extra;

import java.util.function.Consumer;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.hooks.CombatTagPlusHook;
import me.realized.duels.hooks.EssentialsHook;
import me.realized.duels.hooks.PvPManagerHook;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.metadata.MetadataUtil;
import org.bukkit.Chunk;
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

    private static final String METADATA_KEY = Teleport.class.getSimpleName();

    private final DuelsPlugin plugin;

    private EssentialsHook essentials;
    private CombatTagPlusHook combatTagPlus;
    private PvPManagerHook pvpManager;

    public Teleport(final DuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handleLoad() {
        this.essentials = plugin.getHookManager().getHook(EssentialsHook.class);
        this.combatTagPlus = plugin.getHookManager().getHook(CombatTagPlusHook.class);
        this.pvpManager = plugin.getHookManager().getHook(PvPManagerHook.class);
        plugin.doSyncAfter(() -> plugin.getServer().getPluginManager().registerEvents(this, plugin), 1L);
    }

    @Override
    public void handleUnload() {}

    public void tryTeleport(final Player player, final Location location, final Consumer<Player> failHandler) {
        if (location == null || location.getWorld() == null) {
            Log.warn(this, "Could not teleport " + player.getName() + "! Location is null");

            if (failHandler != null) {
                failHandler.accept(player);
            }
            return;
        }

        if (essentials != null) {
            essentials.setBackLocation(player, location);
        }

        if (combatTagPlus != null) {
            combatTagPlus.removeTag(player);
        }

        if (pvpManager != null) {
            pvpManager.removeTag(player);
        }

        final Chunk chunk = location.getChunk();

        if (!chunk.isLoaded()) {
            chunk.load();
        }

        location.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
        MetadataUtil.put(plugin, player, METADATA_KEY, location.clone());

        if (!player.teleport(location)) {
            Log.warn(this, "Could not teleport " + player.getName() + "! Player is dead or is vehicle");

            if (failHandler != null) {
                failHandler.accept(player);
            }
        }
    }

    public void tryTeleport(final Player player, final Location location) {
        tryTeleport(player, location, null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(final PlayerTeleportEvent event) {
        final Player player = event.getPlayer();

        if (!event.isCancelled()) {
            MetadataUtil.remove(plugin, player, METADATA_KEY);
            return;
        }

        final Object value = MetadataUtil.removeAndGet(plugin, player, METADATA_KEY);

        if (value != null) {
            event.setCancelled(false);
            event.setTo((Location) value);
        }
    }
}
