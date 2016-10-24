package me.realized.duels.arena;

import me.realized.duels.Core;
import me.realized.duels.configuration.ConfigManager;
import me.realized.duels.configuration.ConfigType;
import me.realized.duels.configuration.MainConfig;
import me.realized.duels.utilities.Helper;
import me.realized.duels.utilities.gui.ICanHandleGUI;
import me.realized.duels.utilities.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Arena implements ICanHandleGUI {

    private final String name;

    private boolean disabled;
    private boolean used;
    private boolean counting;
    private Map<Integer, Location> positions = new HashMap<>();
    private List<UUID> players = new ArrayList<>();
    private Match current;
    private ItemStack displayed;

    private final MainConfig config = (MainConfig) ConfigManager.getConfig(ConfigType.MAIN);

    public Arena(String name, boolean disabled) {
        this.name = name;
        this.disabled = disabled;
        this.displayed = ItemBuilder.builder().type(Material.MAP).name(Helper.color(config.getGuiAvailableArenaDisplayname()).replace("{NAME}", name)).build();
    }

    public String getName() {
        return name;
    }

    public boolean isCounting() {
        return counting;
    }

    public void startCountdown() {
        if (!config.isCountdownEnabled()) {
            return;
        }

        List<String> messages = config.getCountdownMessages();

        if (!messages.isEmpty()) {
            new CountdownTask(messages).runTaskTimer(Core.getInstance(), 0L, 20L);
        }
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;

        ItemMeta meta = displayed.getItemMeta();

        if (!used) {
            players.clear();
            current = null;
            counting = false;
            meta.setDisplayName(Helper.color(config.getGuiAvailableArenaDisplayname()).replace("{NAME}", name));
        } else {
            current = new Match();
            meta.setDisplayName(Helper.color(config.getGuiInUseArenaDisplayname()).replace("{NAME}", name));
        }

        displayed.setItemMeta(meta);
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
        return !(pos1 == null || pos2 == null || pos1.getWorld() == null || pos2.getWorld() == null || !pos1.getWorld().getName().equals(pos2.getWorld().getName()));
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public void addPlayers(Player... players) {
        for (Player player : players) {
            this.players.add(player.getUniqueId());
        }
    }

    public boolean hasPlayer(Player player) {
        return players.contains(player.getUniqueId());
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
            result.add(Helper.format(entry.getValue()));
        }

        return result;
    }

    public void broadcast(String msg) {
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                Helper.pm(player, msg, false);
            }
        }
    }

    @Override
    public ItemStack toDisplay() {
        return displayed;
    }

    @Override
    public boolean filter() {
        return !disabled && isValid();
    }

    public class Match {

        private final long start;
        private final Map<UUID, Location> lastLocations = new HashMap<>();
        private final Map<UUID, InventoryData> inventories = new HashMap<>();
        private final Map<UUID, Boolean> dead = new HashMap<>();

        private long end;
        private double finishingHealth;

        Match() {
            this.start = System.currentTimeMillis();
        }

        public long getStart() {
            return start;
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

        public boolean wasDead(UUID uuid) {
            return dead.get(uuid);
        }

        public void setDead(UUID uuid) {
            dead.put(uuid, true);
        }

        public void setData(Player... players) {
            for (Player player : players) {
                lastLocations.put(player.getUniqueId(), player.getLocation().clone());
                inventories.put(player.getUniqueId(), new InventoryData(player.getInventory().getContents().clone(), player.getInventory().getArmorContents().clone()));
                dead.put(player.getUniqueId(), false);
            }
        }
    }

    public class InventoryData {

        private final ItemStack[] armor;
        private final ItemStack[] inventory;

        InventoryData(ItemStack[] inventory, ItemStack[] armor) {
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

    class CountdownTask extends BukkitRunnable {

        private final List<String> messages;

        private int index = 0;

        CountdownTask(List<String> messages) {
            this.messages = messages;
            counting = true;
        }

        @Override
        public void run() {
            broadcast(messages.get(index));
            index++;

            if (index >= messages.size()) {
                counting = false;
                cancel();
            }
        }
    }
}
