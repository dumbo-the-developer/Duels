package me.realized.duels.setting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.Loadable;
import org.bukkit.entity.Player;

public class SettingManager implements Loadable {

    private final DuelsPlugin plugin;
    private final Map<UUID, Setting> cache = new HashMap<>();

    public SettingManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handleLoad() {}

    @Override
    public void handleUnload() {
        cache.clear();
    }

    public Setting getSafely(final Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), result -> new Setting(plugin, player));
    }

    public Setting remove(final Player player) {
        return cache.remove(player.getUniqueId());
    }
}
