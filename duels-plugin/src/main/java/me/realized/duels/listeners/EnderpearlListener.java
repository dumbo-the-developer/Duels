package me.realized.duels.listeners;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.config.Lang;
import me.realized.duels.util.metadata.MetadataUtil;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

/**
 * Prevents players throwing an enderpearl before entering a duel and teleporting out with kit items.
 */
public class EnderpearlListener implements Listener {

    public static final String METADATA_KEY = "Duels-LastEnderpearlAt";

    private final DuelsPlugin plugin;
    private final Lang lang;
    private final ArenaManagerImpl arenaManager;

    public EnderpearlListener(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLang();
        this.arenaManager = plugin.getArenaManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(final ProjectileLaunchEvent event) {
        if (event.getEntityType() != EntityType.ENDER_PEARL) {
            return;
        }

        final EnderPearl enderPearl = (EnderPearl) event.getEntity();

        if (!(enderPearl.getShooter() instanceof Player)) {
            return;
        }

        // Cache last enderpearl thrown time for players
        MetadataUtil.put(plugin, (Player) enderPearl.getShooter(), METADATA_KEY, System.currentTimeMillis());
    }

    @EventHandler
    public void on(PlayerTeleportEvent event) {
        final Player player = event.getPlayer();

        if (event.getCause() != TeleportCause.ENDER_PEARL) {
            return;
        }

        final Object value = MetadataUtil.get(plugin, player, METADATA_KEY);

        if (value == null) {
            return;
        }

        final ArenaImpl arena = arenaManager.get(player);

        if (arena == null || arena.getMatch() == null || (Long) value > arena.getMatch().getStart()) {
            return;
        }

        // Cancel teleport if last pearl thrown is before start of the current match
        event.setCancelled(true);
        lang.sendMessage(player, "DUEL.prevent.teleportation");
    }
}
