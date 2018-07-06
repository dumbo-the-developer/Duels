package me.realized.duels.setting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.Arena;
import me.realized.duels.gui.setting.SettingsGui;
import me.realized.duels.kit.Kit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Settings {

    private final DuelsPlugin plugin;
    private final SettingsGui gui;

    @Getter
    private UUID target;
    @Getter
    @Setter
    private Kit kit;
    @Getter
    @Setter
    private Arena arena;
    @Getter
    @Setter
    private int bet;
    @Getter
    @Setter
    private boolean itemBetting;
    @Getter
    private Map<UUID, LocationInfo> locations = new HashMap<>();

    public Settings(final DuelsPlugin plugin, final Player player) {
        this.plugin = plugin;
        this.gui = player != null ? plugin.getGuiListener().addGui(player, new SettingsGui(plugin)) : null;
    }

    public Settings(final DuelsPlugin plugin) {
        this(plugin, null);
    }

    public void reset() {
        target = null;
        kit = null;
        arena = null;
        bet = 0;
        itemBetting = false;
    }

    public void setTarget(final Player target) {
        if (this.target != null && !this.target.equals(target.getUniqueId())) {
            reset();
        }

        this.target = target.getUniqueId();
    }

    public void updateGui(final Player player) {
        if (gui != null) {
            gui.update(player);
        }
    }

    public void openGui(final Player player) {
        gui.open(player);
    }

    public void setBaseLoc(final Player player) {
        locations.computeIfAbsent(player.getUniqueId(), result -> new LocationInfo()).location = player.getLocation().clone();
    }

    public Location getBaseLoc(final Player player) {
        final LocationInfo info = locations.get(player.getUniqueId());

        if (info == null) {
            return null;
        }

        return info.location;
    }

    public void setDuelzone(final Player player, final String duelzone) {
        locations.computeIfAbsent(player.getUniqueId(), result -> new LocationInfo()).duelzone = duelzone;
    }

    public String getDuelzone(final Player player) {
        final LocationInfo info = locations.get(player.getUniqueId());

        if (info == null) {
            return null;
        }

        return info.duelzone;
    }

    // Don't copy the gui since it won't be required to start a match
    public Settings lightCopy() {
        final Settings copy = new Settings(plugin);
        copy.target = target;
        copy.kit = kit;
        copy.arena = arena;
        copy.bet = bet;
        copy.itemBetting = itemBetting;
        copy.locations = locations;
        return copy;
    }

    private class LocationInfo {

        private Location location;
        private String duelzone;

    }
}
