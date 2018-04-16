package me.realized.duels.betting;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.cache.SettingCache;
import org.bukkit.event.Listener;

public class BettingManager implements Listener {

    private final DuelsPlugin plugin;
    private final SettingCache cache;

    public BettingManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.cache = plugin.getSettingCache();
    }
}
