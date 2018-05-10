package me.realized.duels.config;

import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.config.AbstractConfiguration;
import org.bukkit.configuration.file.FileConfiguration;

public class Config extends AbstractConfiguration<DuelsPlugin> {

    @Getter
    private int version;
    @Getter
    private boolean checkForUpdates;
    @Getter
    private boolean allowArenaSelecting;
    @Getter
    private boolean useOwnInventoryEnabled;
    @Getter
    private boolean useOwnInventoryKeepItems;
    @Getter
    private boolean requiresClearedInventory;

    public Config(final DuelsPlugin plugin) {
        super(plugin, "_config");
    }

    @Override
    protected void loadValues(final FileConfiguration configuration) {
        version = configuration.getInt("config-version");
        checkForUpdates = configuration.getBoolean("check-for-updates", true);
        allowArenaSelecting = configuration.getBoolean("setting.allow-arena-selecting", true);
        useOwnInventoryEnabled = configuration.getBoolean("setting.allow-arena-selecting", false);
        useOwnInventoryKeepItems = configuration.getBoolean("setting.use-own-inventory.keep-items", false);
        requiresClearedInventory = configuration.getBoolean("setting.requires-cleared-inventory", true);
    }
}
