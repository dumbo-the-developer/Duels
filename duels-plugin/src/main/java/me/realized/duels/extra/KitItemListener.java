package me.realized.duels.extra;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.util.compat.Tags;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

// TODO: 13/06/2018 sendMessage
public class KitItemListener implements Listener {

    private final ArenaManager arenaManager;

    public KitItemListener(final DuelsPlugin plugin) {
        this.arenaManager = plugin.getArenaManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void on(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();

        if (!shouldCancel(player)) {
            return;
        }

        final Inventory clicked = event.getClickedInventory();

        if (clicked == null || !(clicked instanceof PlayerInventory)) {
            return;
        }

        final ItemStack item = event.getCurrentItem();

        if (!isKitItem(item)) {
            return;
        }

        event.setCurrentItem(null);
    }

    @EventHandler
    public void on(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (!shouldCancel(player)) {
            return;
        }

        final ItemStack item = event.getItem();

        if (!isKitItem(item)) {
            return;
        }

        event.setCancelled(true);
        player.getInventory().remove(item);
    }

    @EventHandler
    public void on(final PlayerPickupItemEvent event) {
        final Player player = event.getPlayer();

        if (!shouldCancel(player)) {
            return;
        }

        final Item item = event.getItem();

        if (!isKitItem(item.getItemStack())) {
            return;
        }

        event.setCancelled(true);
        item.remove();
    }

    private boolean shouldCancel(final Player player) {
        return !player.isOp() && !player.hasPermission(Permissions.ADMIN) && !arenaManager.isInMatch(player);
    }

    private boolean isKitItem(final ItemStack item) {
        return item != null && item.getType() != Material.AIR && Tags.hasKey(item, "DuelsKitContent");
    }
}
