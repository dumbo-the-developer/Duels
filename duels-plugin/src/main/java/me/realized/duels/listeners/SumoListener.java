package me.realized.duels.listeners;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.Arena;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.config.Config;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class SumoListener implements Listener {

    private final Config config;
    private final ArenaManager arenaManager;

    public SumoListener(final DuelsPlugin plugin) {
        this.config = plugin.getConfiguration();
        this.arenaManager = plugin.getArenaManager();

        if (config.isSumoEnabled()) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @EventHandler
    public void on(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getEntity();
        final Arena arena = arenaManager.get(player);

        if (arena == null || !arena.getName().startsWith(config.getSumoNameStartingWith())) {
            return;
        }

        event.setDamage(0);
    }

    @EventHandler
    public void on(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final Arena arena = arenaManager.get(player);

        if (player.isDead() || arena == null || !arena.getName().startsWith(config.getSumoNameStartingWith())) {
            return;
        }

        final Block block = event.getFrom().getBlock();

        if (!(block.getType().name().contains("WATER") || block.getType().name().contains("LAVA"))) {
            return;
        }

        player.setHealth(0);
    }
}
