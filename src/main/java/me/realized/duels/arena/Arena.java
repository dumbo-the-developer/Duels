package me.realized.duels.arena;

import me.realized.duels.utilities.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Arena {

    private final String name;

    private boolean disabled = false;
    private boolean used = false;
    private Map<Integer, Location> positions = new HashMap<>();
    private List<UUID> players = new ArrayList<>();
    private Match current;

    public Arena(String name, boolean disabled) {
        this.name = name;
        this.disabled = disabled;
    }

    public String getName() {
        return name;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        if (!used) {
            this.players.clear();
            this.current = null;
        } else {
            this.current = new Match();
        }

        this.used = used;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public Map<Integer, Location> getPositions() {
        return positions;
    }

    public boolean isValid() {
        Location pos1 = positions.get(1);
        Location pos2 = positions.get(2);

        return !(pos1 == null || pos2 == null) && !(pos1.getWorld() == null || pos2.getWorld() == null || !pos1.getWorld().getName().equals(pos2.getWorld().getName()));

    }

    public List<UUID> getPlayers() {
        return players;
    }

    public void addPlayers(Player... players) {
        for (Player player : players) {
            this.players.add(player.getUniqueId());
        }
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }

    public void addPosition(int position, Location location) {
        positions.put(position, location);
    }

    public Match getCurrentMatch() {
        return current;
    }

    public List<String> getFormattedPlayers() {
        List<String> result = new ArrayList<>();

        if (players.isEmpty()) {
            result.add("none");
            return result;
        }

        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                result.add(player.getName());
            }
        }

        return result;
    }

    public List<String> getFormattedLocations() {
        List<String> result = new ArrayList<>();

        if (positions.isEmpty()) {
            result.add("none");
            return result;
        }

        for (Map.Entry<Integer, Location> entry : positions.entrySet()) {
            result.add(LocationUtil.format(entry.getValue()));
        }

        return result;
    }

    public class Match {

        private final long start;
        private Map<UUID, Location> lastLocations = new HashMap<>();
        private Map<UUID, InventoryData> inventories = new HashMap<>();

        private long end;
        private double finishingHealth;

        public Match() {
            this.start = System.currentTimeMillis();
        }

        public int getDuration() {
            return (int) (end - start);
        }

        public void setEndTimeMillis(long value) {
            this.end = value;
        }

        public double getFinishingHealth() {
            return finishingHealth;
        }

        public void setFinishingHealth(double finishingHealth) {
            this.finishingHealth = finishingHealth;
        }

        public Location getLocation(UUID uuid) {
            return lastLocations.get(uuid);
        }

        public InventoryData getInventories(UUID uuid) {
            return inventories.get(uuid);
        }

        public void setData(Player... players) {
            for (Player player : players) {
                lastLocations.put(player.getUniqueId(), player.getLocation());
                inventories.put(player.getUniqueId(), new InventoryData(player.getInventory().getContents(), player.getInventory().getArmorContents()));
            }
        }
    }

    public class InventoryData {

        private final ItemStack[] armor;
        private final ItemStack[] inventory;

        public InventoryData(ItemStack[] inventory, ItemStack[] armor) {
            this.inventory = inventory;
            this.armor = armor;
        }

        public ItemStack[] getArmorContents() {
            return armor;
        }

        public ItemStack[] getInventoryContents() {
            return inventory;
        }
    }
}
