package com.meteordevelopments.duels.core.kit.edit;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.util.Loadable;
import com.meteordevelopments.duels.util.Log;
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
    private final DuelsPlugin plugin;
    
    public EditSession(DuelsPlugin plugin, Player player, String kitName) {
        this.plugin = plugin;
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

    public void returnToOldLocation(Player player) {
        teleportAsync(player, getPlayerOriginalLoc()).thenAccept(success -> {
            if (!success) {
                Log.warn((Loadable) this, "Could not teleport " + player.getName() + " back to original kit edit location!");
            }
        }).exceptionally(throwable -> {
            Log.warn((Loadable) this, "Failed to teleport " + player.getName() + " back to original location: " + throwable.getMessage());
            return null;
        });
    }
}
