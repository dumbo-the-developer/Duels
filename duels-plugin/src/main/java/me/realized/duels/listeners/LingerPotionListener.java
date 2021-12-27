package me.realized.duels.listeners;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.spectate.SpectateManagerImpl;
import me.realized.duels.util.compat.CompatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;

/**
 * Prevents spectators from being affected by lingering potions.
 */
public class LingerPotionListener {

    private final ArenaManagerImpl arenaManager;
    private final SpectateManagerImpl spectateManager;

    public LingerPotionListener(final DuelsPlugin plugin) {
        this.arenaManager = plugin.getArenaManager();
        this.spectateManager = plugin.getSpectateManager();

        // Lingering potions were released in MC 1.9
        if (CompatUtil.isPre1_9()) {
            return;
        }

        Bukkit.getPluginManager().registerEvents(new Post1_9Listener(), plugin);
    }

    public class Post1_9Listener implements Listener {

        @EventHandler
        public void on(final AreaEffectCloudApplyEvent event) {
            if (!(event.getEntity().getSource() instanceof Player)) {
                return;
            }

            final Player player = (Player) event.getEntity().getSource();

            if (!arenaManager.isInMatch(player)) {
                return;
            }

            event.getAffectedEntities().removeIf(entity -> entity instanceof Player && spectateManager.isSpectating((Player) entity));
        }
    }
}
