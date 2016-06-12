package me.realized.duels.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class PluginHook {

    private final String name;

    public PluginHook(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled(name);
    }

    public Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin(name);
    }
}
