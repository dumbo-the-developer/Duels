package com.meteordevelopments.duels.kit.edit;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.kit.KitImpl;
import com.meteordevelopments.duels.util.hook.PluginHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.papermc.lib.PaperLib.teleportAsync;

/**
 * Manages kit editing sessions for players.
 * Handles inventory snapshots, kit loading, and per-player kit storage.
 */
public class KitEditManager extends PluginHook<DuelsPlugin> {
    
    private static KitEditManager instance;
    private final Map<UUID, EditSession> activeSessions = new HashMap<>();
    private final File playerKitsDirectory;
    private final Yaml yaml;
    
    public KitEditManager(DuelsPlugin plugin) {
        super(plugin, "KitEditManager");
        instance = this;
        
        // Create player kits directory
        this.playerKitsDirectory = new File(plugin.getDataFolder(), "playerkits");
        if (!playerKitsDirectory.exists()) {
            playerKitsDirectory.mkdirs();
        }
        
        // Configure YAML
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        this.yaml = new Yaml(options);
    }
    
    /**
     * Gets the singleton instance of KitEditManager.
     */
    public static KitEditManager getInstance() {
        return instance;
    }
    
    /**
     * Starts a kit editing session for a player.
     */
    public boolean startEditSession(Player player, String kitName) {
        UUID playerId = player.getUniqueId();
        
        // Check if player is already editing
        if (activeSessions.containsKey(playerId)) {
            return false;
        }
        
        // Get the kit
        KitImpl kit = plugin.getKitManager().get(kitName);
        if (kit == null) {
            return false;
        }
        
        // Create edit session
        EditSession session = new EditSession(player, kitName);
        activeSessions.put(playerId, session);

        // Teleport player to Kit Lobby
        teleportAsync(player, plugin.getPlayerManager().getKitLobby());

        // Clear player inventory
        player.getInventory().clear();

        // Hide Player
        player.hidePlayer(plugin, player);
        
        // Load kit into player inventory - check for custom kit first
        if (hasPlayerKit(player, kitName)) {
            // Load player's custom kit
            loadPlayerKit(player, kitName);
        } else {
            // Load default kit
            loadKitToPlayer(player, kit);
        }
        
        return true;
    }
    
    /**
     * Saves the current kit and ends the editing session.
     */
    public boolean saveKit(Player player) {
        UUID playerId = player.getUniqueId();
        EditSession session = activeSessions.get(playerId);
        
        if (session == null) {
            return false;
        }
        
        // Validate that player only has items from the default kit
        if (!validateKitItems(player, session.getKitName())) {
            plugin.getLang().sendMessage(player, "KIT.EDIT.invalid-items");
            abortEditSession(player);
            return false;
        }
        
        try {
            // Save the current inventory as the kit
            savePlayerKit(player, session.getKitName());
            
            // Restore original inventory
            session.restoreInventory(player);

            // Restore original Location
            session.returnToOldLocation(player);

            // Unhide player
            player.showPlayer(plugin, player);
            
            // Remove from active sessions
            activeSessions.remove(playerId);
            
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save kit for player " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Aborts the editing session without saving.
     */
    public boolean abortEditSession(Player player) {
        UUID playerId = player.getUniqueId();
        EditSession session = activeSessions.get(playerId);
        
        if (session == null) {
            return false;
        }
        
        // Restore original inventory
        session.restoreInventory(player);

        // Restore original Location
        session.returnToOldLocation(player);

        // Unhide player
        player.showPlayer(plugin, player);
        
        // Remove from active sessions
        activeSessions.remove(playerId);
        
        return true;
    }
    
    /**
     * Forces a fresh editing session by clearing current session and starting new one.
     */
    public boolean forceFreshEditSession(Player player, String kitName) {
        // Abort current session if exists
        if (activeSessions.containsKey(player.getUniqueId())) {
            abortEditSession(player);
        }
        
        // Start fresh session
        return startEditSession(player, kitName);
    }
    
    /**
     * Checks if a player is currently editing a kit.
     */
    public boolean isEditing(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }
    
    /**
     * Gets the current edit session for a player.
     */
    public EditSession getEditSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }
    
    /**
     * Handles player quit - restores inventory and aborts session.
     */
    public void handlePlayerQuit(Player player) {
        UUID playerId = player.getUniqueId();
        EditSession session = activeSessions.get(playerId);
        
        if (session != null) {
            plugin.getLogger().info("Player " + player.getName() + " left during kit editing session. Restoring inventory.");
            
            // Restore original inventory
            session.restoreInventory(player);

            // Restore original Location
            session.returnToOldLocation(player);

            // Unhide player
            player.showPlayer(plugin, player);
            
            // Remove from active sessions
            activeSessions.remove(playerId);
        }
    }
    
    /**
     * Loads a kit into the player's inventory.
     */
    private void loadKitToPlayer(Player player, KitImpl kit) {
        // Clear inventory first
        player.getInventory().clear();
        
        // Get all items from the kit and load them individually (preserve item data)
        Map<String, Map<Integer, ItemStack>> kitItems = kit.getItems();
        int slot = 0;
        
        for (Map.Entry<String, Map<Integer, ItemStack>> entry : kitItems.entrySet()) {
            Map<Integer, ItemStack> slotItems = entry.getValue();
            for (ItemStack item : slotItems.values()) {
                if (item != null && item.getType() != org.bukkit.Material.AIR) {
                    try {
                        // Clone the item to preserve all data (enchantments, potion effects, etc.)
                        ItemStack clonedItem = item.clone();
                        player.getInventory().setItem(slot, clonedItem);
                        slot++;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        // Suppress the error - kit has too many items
                        break;
                    }
                }
            }
        }
        
        player.updateInventory();
    }
    
    /**
     * Saves the player's current inventory as a kit.
     */
    private void savePlayerKit(Player player, String kitName) throws IOException {
        UUID playerId = player.getUniqueId();
        
        // Create player-specific directory
        File playerDir = new File(playerKitsDirectory, playerId.toString());
        if (!playerDir.exists()) {
            playerDir.mkdirs();
        }
        
        // Create kit file
        File kitFile = new File(playerDir, kitName + ".yml");
        
        // Prepare kit data
        Map<String, Object> kitData = new HashMap<>();
        PlayerInventory inv = player.getInventory();
        
        // Save inventory contents
        ItemStack[] contents = inv.getContents();
        Map<String, Object> inventoryData = new HashMap<>();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                inventoryData.put(String.valueOf(i), contents[i].serialize());
            }
        }
        kitData.put("inventory", inventoryData);
        
        // Save armor
        ItemStack[] armor = inv.getArmorContents();
        Map<String, Object> armorData = new HashMap<>();
        for (int i = 0; i < armor.length; i++) {
            if (armor[i] != null) {
                armorData.put(String.valueOf(i), armor[i].serialize());
            }
        }
        kitData.put("armor", armorData);
        
        // Save offhand
        ItemStack offhand = inv.getItemInOffHand();
        if (offhand != null && !offhand.getType().isAir()) {
            kitData.put("offhand", offhand.serialize());
        }
        
        // Save metadata
        kitData.put("name", kitName);
        kitData.put("player", player.getName());
        kitData.put("saved_at", System.currentTimeMillis());
        
        // Write to file
        try (FileWriter writer = new FileWriter(kitFile)) {
            yaml.dump(kitData, writer);
        }
    }
    
    /**
     * Gets all active editing sessions.
     */
    public Map<UUID, EditSession> getActiveSessions() {
        return new HashMap<>(activeSessions);
    }
    
    /**
     * Checks if a player has a custom kit for the given kit name.
     */
    public boolean hasPlayerKit(Player player, String kitName) {
        UUID playerId = player.getUniqueId();
        File playerDir = new File(playerKitsDirectory, playerId.toString());
        File kitFile = new File(playerDir, kitName + ".yml");
        return kitFile.exists();
    }
    
    /**
     * Aborts kit editing session if player joins queue or match.
     */
    public void checkAndAbortIfInQueueOrMatch(Player player) {
        if (isEditing(player)) {
            // Check if player is in queue or match
            if (plugin.getQueueManager().get(player) != null || 
                plugin.getArenaManager().isInMatch(player)) {
                abortEditSession(player);
                plugin.getLang().sendMessage(player, "KIT.EDIT.session-aborted");
            }
        }
    }
    
    /**
     * Validates that the player's inventory only contains items from the default kit.
     */
    private boolean validateKitItems(Player player, String kitName) {
        try {
            // Get the default kit
            KitImpl defaultKit = plugin.getKitManager().get(kitName);
            if (defaultKit == null) {
                return false;
            }
            
            // Get all items from the default kit
            List<ItemStack> defaultItems = getAllItemsFromKit(defaultKit);
            
            // Get all items from the player's inventory
            List<ItemStack> playerItems = getAllItemsFromPlayer(player);
            
            // Compare the lists - they should match
            return itemListsMatch(defaultItems, playerItems);
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error validating kit items for " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets all items from the default kit.
     */
    private List<ItemStack> getAllItemsFromKit(KitImpl kit) {
        // Count items by type to avoid duplicates
        Map<org.bukkit.Material, Integer> itemCounts = new HashMap<>();
        
        // Get all items from the kit
        Map<String, Map<Integer, ItemStack>> kitItems = kit.getItems();
        for (Map.Entry<String, Map<Integer, ItemStack>> entry : kitItems.entrySet()) {
            Map<Integer, ItemStack> slotItems = entry.getValue();
            for (ItemStack item : slotItems.values()) {
                if (item != null && item.getType() != org.bukkit.Material.AIR) {
                    itemCounts.put(item.getType(), itemCounts.getOrDefault(item.getType(), 0) + 1);
                }
            }
        }
        
        // Convert counts back to item list
        List<ItemStack> items = new ArrayList<>();
        for (Map.Entry<org.bukkit.Material, Integer> entry : itemCounts.entrySet()) {
            org.bukkit.Material material = entry.getKey();
            int count = entry.getValue();
            items.add(new ItemStack(material, count));
        }
        
        return items;
    }
    
    /**
     * Gets all items from the player's inventory.
     */
    private List<ItemStack> getAllItemsFromPlayer(Player player) {
        List<ItemStack> items = new ArrayList<>();
        
        PlayerInventory inv = player.getInventory();
        
        // Add inventory contents (filter out AIR items)
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() != org.bukkit.Material.AIR) {
                items.add(item);
            }
        }
        
        // Add armor (filter out AIR items)
        for (ItemStack item : inv.getArmorContents()) {
            if (item != null && item.getType() != org.bukkit.Material.AIR) {
                items.add(item);
            }
        }
        
        // Add offhand (filter out AIR items)
        ItemStack offhand = inv.getItemInOffHand();
        if (offhand != null && offhand.getType() != org.bukkit.Material.AIR) {
            items.add(offhand);
        }
        
        return items;
    }
    
    /**
     * Checks if two item lists match (same item types, ignoring quantities and positions).
     */
    private boolean itemListsMatch(List<ItemStack> list1, List<ItemStack> list2) {
        // Get unique item types from both lists
        Set<org.bukkit.Material> defaultTypes = new HashSet<>();
        Set<org.bukkit.Material> playerTypes = new HashSet<>();
        
        // Get default kit item types
        for (ItemStack item : list1) {
            if (item != null) {
                defaultTypes.add(item.getType());
            }
        }
        
        // Get player item types
        for (ItemStack item : list2) {
            if (item != null) {
                playerTypes.add(item.getType());
            }
        }
        
        // Check if player has any item types not in default kit
        for (org.bukkit.Material playerType : playerTypes) {
            if (!defaultTypes.contains(playerType)) {
                return false;
            }
        }
        
        return true; // All player item types are from default kit
    }
    
    /**
     * Compares two items to see if they match (ignoring quantity and data).
     */
    private boolean itemsMatch(ItemStack item1, ItemStack item2) {
        if (item1 == null && item2 == null) {
            return true;
        }
        if (item1 == null || item2 == null) {
            return false;
        }
        
        // Only compare type, ignore quantity and data
        return item1.getType() == item2.getType();
    }
    
    /**
     * Loads a player's custom kit into their inventory.
     */
    public boolean loadPlayerKit(Player player, String kitName) {
        try {
            UUID playerId = player.getUniqueId();
            File playerDir = new File(playerKitsDirectory, playerId.toString());
            File kitFile = new File(playerDir, kitName + ".yml");
            
            if (!kitFile.exists()) {
                return false;
            }
            
            // Load kit data from YAML
            Map<String, Object> kitData = yaml.load(new java.io.FileReader(kitFile));
            
            PlayerInventory inv = player.getInventory();
            inv.clear();
            
            // Load inventory
            if (kitData.containsKey("inventory")) {
                Map<String, Object> inventoryData = (Map<String, Object>) kitData.get("inventory");
                for (Map.Entry<String, Object> entry : inventoryData.entrySet()) {
                    int slot = Integer.parseInt(entry.getKey());
                    Map<String, Object> itemData = (Map<String, Object>) entry.getValue();
                    ItemStack item = ItemStack.deserialize(itemData);
                    inv.setItem(slot, item);
                }
            }
            
            // Load armor
            if (kitData.containsKey("armor")) {
                Map<String, Object> armorData = (Map<String, Object>) kitData.get("armor");
                ItemStack[] armor = new ItemStack[4];
                for (Map.Entry<String, Object> entry : armorData.entrySet()) {
                    int slot = Integer.parseInt(entry.getKey());
                    Map<String, Object> itemData = (Map<String, Object>) entry.getValue();
                    ItemStack item = ItemStack.deserialize(itemData);
                    armor[slot] = item;
                }
                inv.setArmorContents(armor);
            }
            
            // Load offhand
            if (kitData.containsKey("offhand")) {
                Map<String, Object> offhandData = (Map<String, Object>) kitData.get("offhand");
                ItemStack offhand = ItemStack.deserialize(offhandData);
                inv.setItemInOffHand(offhand);
            }
            
            player.updateInventory();
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load player kit " + kitName + " for " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
}
