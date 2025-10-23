package com.meteordevelopments.duels.kit.edit;

import com.meteordevelopments.duels.DuelsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;

/**
 * Handles all events to prevent exploits during kit editing sessions.
 * Blocks all interactions except /kitsave command.
 */
public class KitEditListener implements Listener {
    
    private final DuelsPlugin plugin;
    
    public KitEditListener(DuelsPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Blocks all commands except /kitsave during kit editing.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        
        if (!KitEditManager.getInstance().isEditing(player)) {
            return;
        }
        
        String command = event.getMessage().toLowerCase().trim();

        if (!command.equals("/kit save") &&
            !command.startsWith("/kit save ")) {
            event.setCancelled(true);
            plugin.getLang().sendMessage(player, "KIT.EDIT.command-blocked");
        }
    }
    
    /**
     * Prevents item drops during kit editing.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        if (KitEditManager.getInstance().isEditing(player)) {
            event.setCancelled(true);
            plugin.getLang().sendMessage(player, "KIT.EDIT.drop-blocked");
        }
    }
    
    /**
     * Prevents item pickups during kit editing.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        if (KitEditManager.getInstance().isEditing(player)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Prevents inventory interactions during kit editing.
     * Temporarily disabled to allow kit editing - only blocks external inventories.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        if (KitEditManager.getInstance().isEditing(player)) {
            // Only block interactions with external inventories (chests, etc.)
            // Allow all player inventory interactions for kit editing
            if (event.getInventory().getType() != org.bukkit.event.inventory.InventoryType.PLAYER && 
                event.getInventory().getType() != org.bukkit.event.inventory.InventoryType.CRAFTING) {
                event.setCancelled(true);
                plugin.getLang().sendMessage(player, "KIT.EDIT.inventory-blocked");
            }
        }
    }
    
    /**
     * Prevents inventory drag events during kit editing.
     * Temporarily disabled to allow kit editing - only blocks external inventories.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        if (KitEditManager.getInstance().isEditing(player)) {
            // Only block drags with external inventories (chests, etc.)
            // Allow all player inventory drags for kit editing
            if (event.getInventory().getType() != org.bukkit.event.inventory.InventoryType.PLAYER && 
                event.getInventory().getType() != org.bukkit.event.inventory.InventoryType.CRAFTING) {
                event.setCancelled(true);
                plugin.getLang().sendMessage(player, "KIT.EDIT.inventory-blocked");
            }
        }
    }
    
    /**
     * Prevents opening external inventories during kit editing.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        
        if (KitEditManager.getInstance().isEditing(player)) {
            // Allow only the player's own inventory
            if (event.getInventory().getType() != org.bukkit.event.inventory.InventoryType.PLAYER) {
                event.setCancelled(true);
                plugin.getLang().sendMessage(player, "KIT.EDIT.inventory-blocked");
            }
        }
    }
    
    /**
     * Prevents item transfers during kit editing.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        
        if (KitEditManager.getInstance().isEditing(player)) {
            // Prevent any item transfers by clearing external inventories
            if (event.getInventory().getType() != org.bukkit.event.inventory.InventoryType.PLAYER) {
                event.getInventory().clear();
            }
        }
    }
    
    /**
     * Prevents death drops during kit editing.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        if (KitEditManager.getInstance().isEditing(player)) {
            event.setKeepInventory(true);
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }
    
    /**
     * Prevents block breaking during kit editing.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        if (KitEditManager.getInstance().isEditing(player)) {
            event.setCancelled(true);
            plugin.getLang().sendMessage(player, "KIT.EDIT.block-break-blocked");
        }
    }
    
    /**
     * Prevents block placing during kit editing.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        if (KitEditManager.getInstance().isEditing(player)) {
            event.setCancelled(true);
            plugin.getLang().sendMessage(player, "KIT.EDIT.block-place-blocked");
        }
    }
    
    /**
     * Prevents player interactions during kit editing.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (KitEditManager.getInstance().isEditing(player)) {
            // Allow only basic item usage (eating, drinking, etc.)
            if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                plugin.getLang().sendMessage(player, "KIT.EDIT.interact-blocked");
            }
        }
    }
    
    /**
     * Handles player quit during kit editing.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        if (KitEditManager.getInstance().isEditing(player)) {
            plugin.getLogger().info("Player " + player.getName() + " left during kit editing session. Restoring inventory.");
            KitEditManager.getInstance().handlePlayerQuit(player);
        }
    }
    
    /**
     * Handles player kick during kit editing.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        
        if (KitEditManager.getInstance().isEditing(player)) {
            plugin.getLogger().info("Player " + player.getName() + " was kicked during kit editing session. Restoring inventory.");
            KitEditManager.getInstance().handlePlayerQuit(player);
        }
    }
}
