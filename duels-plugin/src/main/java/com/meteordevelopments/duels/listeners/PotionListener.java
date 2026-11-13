package com.meteordevelopments.duels.listeners;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.core.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.util.compat.CompatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles potion consumption and removes empty potion bottles after consumption if enabled.
 */
public class PotionListener implements Listener {
    private final DuelsPlugin plugin;
    private final Config config;
    private final ArenaManagerImpl arenaManager;

    public PotionListener(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.arenaManager = plugin.getArenaManager();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(final PlayerItemConsumeEvent event) {
        final Player player = event.getPlayer();

        if (!arenaManager.isInMatch(player)) {
            return;
        }

        if (!config.isRemoveEmptyBottle()) {
            return;
        }

        final ItemStack item = event.getItem();
        if (!item.getType().name().endsWith("POTION")) {
            return;
        }

        DuelsPlugin.getFoliaLib().getScheduler().runAtEntity(player, task -> {
            if (item.getAmount() <= 1) {
                if (CompatUtil.isPre1_10()) {
                    player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
                } else {
                    final ItemStack held = player.getInventory().getItemInMainHand();

                    if (held != null && held.getType() == Material.GLASS_BOTTLE) {
                        player.getInventory().setItemInMainHand(null);
                    } else {
                        final ItemStack offHand = player.getInventory().getItemInOffHand();
                        if (offHand != null && offHand.getType() == Material.GLASS_BOTTLE) {
                            player.getInventory().setItemInOffHand(null);
                        }
                    }
                }
            } else {
                player.getInventory().removeItem(new ItemStack(Material.GLASS_BOTTLE, 1));
            }
        });
    }
}
