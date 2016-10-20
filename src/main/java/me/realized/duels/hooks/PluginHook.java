package me.realized.duels.hooks;

import me.realized.duels.Core;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class PluginHook {

    private final String name;

    public PluginHook(String name) {
        this.name = name;

        if (isEnabled()) {
            Core.getInstance().info("Successfully hooked into " + name + ".");
        }
    }

    public boolean isEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled(name);
    }

    public Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin(name);
    }
}
