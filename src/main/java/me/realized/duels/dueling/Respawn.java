package me.realized.duels.dueling;

import me.realized.duels.arena.Arena;
import org.bukkit.Location;

public class Respawn {

    private final Arena.InventoryData inventoryData;
    private final Location respawnLocation;

    public Respawn(Arena.InventoryData inventoryData, Location respawnLocation) {
        this.inventoryData = inventoryData;
        this.respawnLocation = respawnLocation;
    }

    public Arena.InventoryData getInventoryData() {
        return inventoryData;
    }

    public Location getRespawnLocation() {
        return respawnLocation;
    }
}
