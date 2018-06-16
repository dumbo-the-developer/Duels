package me.realized.duels.extra;

import java.util.Set;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.config.Lang;
import me.realized.duels.spectate.SpectateManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportListener implements Listener {

    private final Lang lang;
    private final ArenaManager arenaManager;
    private final SpectateManager spectateManager;

    public TeleportListener(final DuelsPlugin plugin) {
        this.lang = plugin.getLang();
        this.arenaManager = plugin.getArenaManager();
        this.spectateManager = plugin.getSpectateManager();

        if (plugin.getConfiguration().isPreventTpToMatchPlayers()) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void on(final PlayerTeleportEvent event) {
        final Player player = event.getPlayer();

        if (player.isOp() || player.hasPermission(Permissions.ADMIN) || player.hasPermission(Permissions.TP_BYPASS)
            || arenaManager.isInMatch(player) || spectateManager.isSpectating(player)) {
            return;
        }

        final Location to = event.getTo();
        final Set<Player> players = arenaManager.getPlayers();
        players.addAll(spectateManager.getPlayers());

        for (final Player target : players) {
            if (player.equals(target) || !target.isOnline() || !isSimilar(target.getLocation(), to)) {
                continue;
            }

            event.setCancelled(true);
            lang.sendMessage(player, "ERROR.prevent-teleportation");
            return;
        }
    }

    private boolean isSimilar(final Location first, final Location second) {
        return Math.abs(first.getX() - second.getX()) + Math.abs(first.getY() - second.getY()) + Math.abs(first.getZ() - second.getZ()) < 5;
    }
}
