package com.meteordevelopments.duels.hook.hooks.worldguard;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.util.hook.PluginHook;
import org.bukkit.entity.Player;

import java.util.Collection;

public class WorldGuardHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "WorldGuard";

    private final Config config;
    private final WorldGuardHandler handler;

    public WorldGuardHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();
        try {
            this.handler = new UnifiedWorldGuardHandler();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize WorldGuard integration", e);
        }
    }

    public String findDuelZone(final Player player) {
        if (!config.isDuelzoneEnabled()) {
            return null;
        }

        final Collection<String> allowedRegions = config.getDuelzones();

        if (allowedRegions.isEmpty()) {
            return null;
        }

        return handler.findRegion(player, allowedRegions);
    }
}
