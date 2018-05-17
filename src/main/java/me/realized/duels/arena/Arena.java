package me.realized.duels.arena;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.cache.Setting;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.kit.Kit;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Arena extends BaseButton {

    @Getter
    private final String name;
    @Getter
    private Map<Integer, Location> positions = new HashMap<>();
    @Getter
    private final Set<UUID> players = new HashSet<>();
    @Getter
    @Setter
    private boolean disabled;
    @Getter
    private boolean used;
    @Getter
    private Match current;

    public Arena(final DuelsPlugin plugin, final String name) {
        super(plugin, ItemBuilder.of(Material.EMPTY_MAP).name("&e" + name).build());
        this.name = name;
    }

    public boolean isAvailable() {
        return !disabled && !used && positions.get(1) != null && positions.get(2) != null;
    }

    public boolean has(final UUID uuid) {
        return players.contains(uuid);
    }

    public boolean hasPlayer(final Player player) {
        return has(player.getUniqueId());
    }

    public void addPlayer(final Player player) {
        players.add(player.getUniqueId());
    }

    public void removePlayer(final Player player) {
        players.remove(player.getUniqueId());
    }

    public UUID getFirst() {
        return players.iterator().next();
    }

    public void setPosition(final int pos, final Location location) {
        positions.put(pos, location);
    }

    public void setUsed(final boolean used) {
        this.used = used;

        if (!used) {
            this.players.clear();
            this.current = null;
        }
    }

    public void setMatch(final Kit kit, final Map<UUID, List<ItemStack>> items, final int bet) {
        this.current = new Match(kit, items, bet);
    }

    @Override
    public void onClick(final Player player) {
        final Setting setting = settingCache.get(player);
        setting.setArena(this);
        setting.openGui(player);
        player.sendMessage(ChatColor.GREEN + "Selected Arena: " + name);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        final Arena arena = (Arena) other;
        return Objects.equals(name, arena.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
