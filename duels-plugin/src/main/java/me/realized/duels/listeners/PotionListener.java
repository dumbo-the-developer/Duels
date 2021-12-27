package me.realized.duels.listeners;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.util.compat.CompatUtil;
import org.bukkit.Bukkit;
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
            Bukkit.getPluginManager().registerEvents(this, plugin);
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

        plugin.doSync(() -> {
            if (item.getAmount() <= 1) {
                if (CompatUtil.isPre1_10()) {
                    player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
                } else {
                    final ItemStack held = player.getInventory().getItemInMainHand();

                    if (held.getType() == Material.GLASS_BOTTLE) {
                        player.getInventory().setItemInMainHand(null);
                    } else {
                        player.getInventory().setItemInOffHand(null);
                    }
                }
            } else {
                player.getInventory().removeItem(new ItemStack(Material.GLASS_BOTTLE, 1));
            }
        });
    }
}
