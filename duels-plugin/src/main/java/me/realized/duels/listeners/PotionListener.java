package me.realized.duels.listeners;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaManagerImpl;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Removes empty potion bottle after consumption if enabled.
 */
public class PotionListener implements Listener {

    private final DuelsPlugin plugin;
    private final ArenaManagerImpl arenaManager;

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

        if (!item.getType().name().endsWith("POTION")) {
            return;
        }

        final int amount = item.getAmount();
        final int heldSlot = player.getInventory().getHeldItemSlot();
        plugin.doSync(() -> {
            if (amount <= 1) {
                player.getInventory().setItem(heldSlot, null);
            } else {
                player.getInventory().removeItem(new ItemStack(Material.GLASS_BOTTLE, 1));
            }
        });
    }
}
