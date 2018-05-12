package me.realized.duels.cache;

import me.realized.duels.DuelsPlugin;
import org.bukkit.entity.Player;

public class SettingCache extends Cache<Setting> {

    private final DuelsPlugin plugin;

    public SettingCache(final DuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    Setting create(final Player player) {
        return new Setting(plugin);
    }
}
