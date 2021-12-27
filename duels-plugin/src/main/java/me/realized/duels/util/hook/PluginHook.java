package me.realized.duels.util.hook;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginHook<P extends JavaPlugin> {

    protected final P plugin;

    private final String name;

    public PluginHook(final P plugin, final String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin(name);
    }
}
