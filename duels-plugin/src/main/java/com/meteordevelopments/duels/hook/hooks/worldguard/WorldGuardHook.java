package com.meteordevelopments.duels.hook.hooks.worldguard;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.util.hook.PluginHook;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.logging.Level;

public class WorldGuardHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "WorldGuard";

    private final Config config;
    private WorldGuardHandler handler;

    public WorldGuardHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();
        try {
            this.handler = new WorldGuard7Handler();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to initialize WorldGuard integration. WorldGuard features will be disabled.", e);
            this.handler = null;
        }
    }

    public String findDuelZone(final Player player) {
        if (!config.isDuelzoneEnabled() || handler == null) {
            return null;
        }

        final Collection<String> allowedRegions = config.getDuelzones();

        if (allowedRegions.isEmpty()) {
            return null;
        }

        return handler.findRegion(player, allowedRegions);
    }
}
