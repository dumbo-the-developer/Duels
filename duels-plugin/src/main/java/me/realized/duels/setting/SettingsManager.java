package me.realized.duels.setting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.Loadable;
import org.bukkit.entity.Player;

public class SettingsManager implements Loadable {

    private final DuelsPlugin plugin;
    private final Map<UUID, Settings> cache = new HashMap<>();

    public SettingsManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handleLoad() {}

    @Override
    public void handleUnload() {
        cache.clear();
    }

    public Settings getSafely(final Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), result -> new Settings(plugin, player));
    }

    public Settings remove(final Player player) {
        return cache.remove(player.getUniqueId());
    }
}
