package com.meteordevelopments.duels.kit.edit;

import com.meteordevelopments.duels.DuelsPlugin;
import lombok.Getter;
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
    
    @Getter
    private final UUID playerId;
    @Getter
    private final String playerName;
    @Getter
    private final String kitName;
    private final ItemStack[] originalInventory;
    private final ItemStack[] originalArmor;
    private final ItemStack originalOffhand;
    @Getter
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
     * FIXED: Always schedules on entity-specific scheduler for Folia compatibility.
     */
    public void restoreInventory(Player player) {
        // FIXED: Schedule inventory operations on entity-specific scheduler for Folia compatibility
        // This ensures safety even when called from async callbacks
        DuelsPlugin.getSchedulerAdapter().runTask(player, () -> {
            if (!player.isOnline()) {
                return;
            }
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
        });
    }

    /**
     * Returns player to their original location.
     * FIXED: Uses teleportAsync with proper callback handling for Folia compatibility.
     */
    public void returnToOldLocation(Player player){
        final Location oldLoc = getPlayerOriginalLoc();
        if (oldLoc == null || oldLoc.getWorld() == null) {
            return;
        }
        
        // FIXED: Use teleportAsync with callback to ensure proper thread handling
        boolean isFolia = DuelsPlugin.getSchedulerAdapter().isFolia();
        if (isFolia) {
            player.teleportAsync(oldLoc).thenAccept(success -> {
                // Teleport completed, no additional operations needed here
                // Inventory restoration is handled separately in restoreInventory()
            });
        } else {
            player.teleport(oldLoc);
        }
    }
}
