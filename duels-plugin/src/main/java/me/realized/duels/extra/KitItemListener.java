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

public class KitItemListener implements Listener {

    private final ArenaManager arenaManager;

    public KitItemListener(final DuelsPlugin plugin) {
        this.arenaManager = plugin.getArenaManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void on(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final Inventory clicked = event.getClickedInventory();

        if (clicked == null || !(clicked instanceof PlayerInventory) || arenaManager.isInMatch(player)) {
            return;
        }

        ItemStack item = event.getCurrentItem();

        if (item == null || item.getType() == Material.AIR || Tags.hasNoKey(item, "KitItem")) {
            return;
        }

        event.setCurrentItem(null);
    }

    @EventHandler
    public void on(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (!event.hasItem() || arenaManager.isInMatch(player)) {
            return;
        }

        final ItemStack item = event.getItem();

        if (item.getType() == Material.AIR || Tags.hasNoKey(item, "KitItem")) {
            return;
        }

        event.setCancelled(true);
        player.getInventory().remove(item);
    }

    @EventHandler
    public void on(final PlayerPickupItemEvent event) {
        if (arenaManager.isInMatch(event.getPlayer())) {
            return;
        }

        final Item item = event.getItem();

        if (item.getItemStack().getType() == Material.AIR || Tags.hasNoKey(item.getItemStack(), "KitItem")) {
            return;
        }

        event.setCancelled(true);
        item.remove();
    }
}
