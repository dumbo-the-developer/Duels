package com.meteordevelopments.duels.kit.edit;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.UUID;
import static io.papermc.lib.PaperLib.teleportAsync;
/**
 * Represents a kit editing session for a player.
 * Stores the original inventory snapshot and kit being edited.
 */
public class EditSession {
    
    private final UUID playerId;
    private final String playerName;
    private final String kitName;
    private final ItemStack[] originalInventory;
    private final ItemStack[] originalArmor;
    private final ItemStack originalOffhand;
    private final long sessionStartTime;
    private final Location playerLoc;
    
    public EditSession(Player player, String kitName) {
        this.playerId = player.getUniqueId();
        this.playerName = player.getName();
        this.kitName = kitName;
        this.sessionStartTime = System.currentTimeMillis();
        
        // Create deep copies of the original inventory
        PlayerInventory inv = player.getInventory();
        this.originalInventory = Arrays.stream(inv.getContents())
                .map(item -> item != null ? item.clone() : null)
                .toArray(ItemStack[]::new);
        this.originalArmor = Arrays.stream(inv.getArmorContents())
                .map(item -> item != null ? item.clone() : null)
                .toArray(ItemStack[]::new);
        this.originalOffhand = inv.getItemInOffHand() != null ? inv.getItemInOffHand().clone() : null;
        this.playerLoc = player.getLocation();
    }
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public String getKitName() {
        return kitName;
    }
    
    public long getSessionStartTime() {
        return sessionStartTime;
    }
    
    public ItemStack[] getOriginalInventory() {
        return Arrays.stream(originalInventory)
                .map(item -> item != null ? item.clone() : null)
                .toArray(ItemStack[]::new);
    }
    
    public ItemStack[] getOriginalArmor() {
        return Arrays.stream(originalArmor)
                .map(item -> item != null ? item.clone() : null)
                .toArray(ItemStack[]::new);
    }
    
    public ItemStack getOriginalOffhand() {
        return originalOffhand != null ? originalOffhand.clone() : null;
    }
    public Location getPlayerOriginalLoc(){
        return playerLoc;
    }
    
    /**
     * Restores the player's original inventory from this session.
     */
    public void restoreInventory(Player player) {
        PlayerInventory inv = player.getInventory();
        
        // Clear current inventory
        inv.clear();
        
        // Restore original inventory
        ItemStack[] restoredInventory = getOriginalInventory();
        for (int i = 0; i < restoredInventory.length; i++) {
            inv.setItem(i, restoredInventory[i]);
        }
        
        // Restore armor
        inv.setArmorContents(getOriginalArmor());
        
        // Restore offhand
        inv.setItemInOffHand(getOriginalOffhand());
        
        // Update player
        player.updateInventory();
    }

    public void returnToOldLocation(Player player){
        teleportAsync(player, getPlayerOriginalLoc());
    }
}
