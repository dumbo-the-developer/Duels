package com.meteordevelopments.duels.hook.hooks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.util.hook.PluginHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EssentialsHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "Essentials";

    private final Config config;

    public EssentialsHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();
    }

    @Override
    public Essentials getPlugin() {
        return (Essentials) super.getPlugin();
    }

    public void tryUnvanish(final Player player) {
        if (!config.isAutoUnvanish()) {
            return;
        }

        final User user = getPlugin().getUser(player);

        if (user != null && user.isVanished()) {
            user.setVanished(false);
        }
    }

    public void setBackLocation(final Player player, final Location location) {
        if (!config.isSetBackLocation()) {
            return;
        }

        final User user = getPlugin().getUser(player);

        if (user != null) {
            user.setLastLocation(location);
        }
    }

    public boolean isVanished(final Player player) {
        final User user;
        return (user = getPlugin().getUser(player)) != null && user.isVanished();
    }
}
