package me.realized.duels.arena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.cache.Setting;
import me.realized.duels.cache.SettingCache;
import me.realized.duels.util.gui.Button;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Arena extends Button {

    private final SettingCache cache;
    @Getter
    private final String name;
    @Getter
    private Map<Integer, Location> positions = new HashMap<>();
    private final List<UUID> players = new ArrayList<>();
    @Getter
    @Setter
    private boolean disabled, used;

    public Arena(final SettingCache cache, final String name) {
        super(ItemBuilder.of(Material.EMPTY_MAP).name("&e" + name).build());
        this.cache = cache;
        this.name = name;
    }

    public void setPosition(final int pos, final Location location) {
        positions.put(pos, location);
    }

    @Override
    public void onClick(final Player player) {
        final Setting setting = cache.get(player);
        setting.setArena(this);
        setting.getGui().open(player);
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
