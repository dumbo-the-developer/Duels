package com.meteordevelopments.duels.listeners;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.util.CC;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WandListener implements Listener {

    private final DuelsPlugin plugin;

    public WandListener(final DuelsPlugin plugin) {
        this.plugin = plugin;
        plugin.registerListener(this);
    }

    public static ItemStack getWandItem() {
        ItemStack wand = new ItemStack(Material.WOODEN_HOE);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(CC.translate("&b&lDuels Arena Wand"));
        }
        wand.setItemMeta(meta);
        return wand;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.WOODEN_HOE || !item.hasItemMeta() || 
            !item.getItemMeta().hasDisplayName() || 
            !item.getItemMeta().getDisplayName().equals(CC.translate("&b&lDuels Arena Wand"))) {
            return;
        }

        event.setCancelled(true);

        if (event.getClickedBlock() == null) {
            return;
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            plugin.getRegionSelectionManager().setPos1(event.getPlayer(), event.getClickedBlock().getLocation());
            event.getPlayer().sendMessage(CC.translate("&aPosition 1 gesetzt!"));
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            plugin.getRegionSelectionManager().setPos2(event.getPlayer(), event.getClickedBlock().getLocation());
            event.getPlayer().sendMessage(CC.translate("&aPosition 2 gesetzt!"));
        }
    }
}
