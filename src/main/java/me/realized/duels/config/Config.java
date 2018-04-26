package me.realized.duels.config;

import java.io.IOException;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.config.AbstractConfiguration;
import org.bukkit.configuration.file.FileConfiguration;

public class Config extends AbstractConfiguration<DuelsPlugin> {

    public Config(final DuelsPlugin plugin) {
        super(plugin, "config");
    }

    @Override
    protected void loadValues(final FileConfiguration configuration) throws IOException {

    }
}
