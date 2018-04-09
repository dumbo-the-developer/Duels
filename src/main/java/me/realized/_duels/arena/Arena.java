package me.realized._duels.arena;

import me.realized._duels.Core;
import me.realized._duels.configuration.MainConfig;
import me.realized._duels.event.MatchEndEvent;
import me.realized._duels.utilities.Helper;
import me.realized._duels.utilities.gui.GUIItem;
import me.realized._duels.utilities.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Arena implements GUIItem {

    private static final MainConfig config = Core.getInstance().getConfiguration();

    private final String name;

    private boolean disabled;
    private boolean used;
    private boolean counting;
    private Map<Integer, Location> positions = new HashMap<>();
    private List<UUID> players = new ArrayList<>();
    private Match current;
    private ItemStack displayed;

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
            if (current != null) {
                current.setDead(player.getUniqueId(), false);
            }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Arena arena = (Arena) o;

        return name != null ? name.equals(arena.name) : arena.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
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
        private final Map<UUID, Boolean> dead = new HashMap<>();

        private String kit;
        private MatchEndEvent.EndReason reason = MatchEndEvent.EndReason.OTHER;

        Match() {
            this.start = System.currentTimeMillis();
        }

        public long getStart() {
            return start;
        }

        public MatchEndEvent.EndReason getEndReason() {
            return reason;
        }

        public String getKit() {
            return kit;
        }

        public void setKit(String kit) {
            this.kit = kit;
        }

        public void setEndReason(MatchEndEvent.EndReason reason) {
            if (reason != MatchEndEvent.EndReason.OTHER) {
                return;
            }

            this.reason = reason;
        }

        public long getDuration() {
            return System.currentTimeMillis() - start;
        }

        public boolean wasDead(UUID uuid) {
            return dead.get(uuid);
        }

        public void setDead(UUID uuid, boolean value) {
            dead.put(uuid, value);
        }

        public Collection<UUID> getMatchStarters() {
            return dead.keySet();
        }
    }

    private class CountdownTask extends BukkitRunnable {

        private final List<String> messages;

        private int index = 0;

        CountdownTask(List<String> messages) {
            this.messages = messages;
            counting = true;
        }

        @Override
        public void run() {
            Helper.broadcast(Arena.this, messages.get(index), false);
            index++;

            if (index >= messages.size()) {
                counting = false;
                cancel();
            }
        }
    }
}
