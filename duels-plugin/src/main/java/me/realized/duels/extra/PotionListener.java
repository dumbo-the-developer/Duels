package me.realized.duels.extra;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class PotionListener implements Listener {

    private final DuelsPlugin plugin;
    private final ArenaManager arenaManager;

    public PotionListener(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.arenaManager = plugin.getArenaManager();

        if (plugin.getConfiguration().isRemoveEmptyBottle()) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @EventHandler
    public void on(final PlayerItemConsumeEvent event) {
        final Player player = event.getPlayer();

        if (!arenaManager.isInMatch(player)) {
            return;
        }

        final ItemStack item = event.getItem();

        if (item.getType().name().endsWith("POTION")) {
            return;
        }

        plugin.doSync(() -> player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null));
    }
}
