package com.meteordevelopments.duels.listeners;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.spectate.SpectateManagerImpl;
import com.meteordevelopments.duels.util.compat.CompatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;

/**
 * Prevents spectators from being affected by lingering potions.
 */
public class LingerPotionListener implements Listener {

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
            if (!(event.getEntity().getSource() instanceof Player player)) {
                return;
            }

            if (!arenaManager.isInMatch(player)) {
                return;
            }

            event.getAffectedEntities().removeIf(entity -> entity instanceof Player && spectateManager.isSpectating((Player) entity));
        }
    }
}
