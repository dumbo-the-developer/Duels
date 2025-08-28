package com.meteordevelopments.duels.util.hook;

import com.meteordevelopments.duels.DuelsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractHookManager<P extends JavaPlugin> {

    protected final P plugin;
    private final Map<Class<? extends PluginHook<P>>, PluginHook<P>> hooks = new HashMap<>();

    public AbstractHookManager(final P plugin) {
        this.plugin = plugin;
    }

    protected void register(final String name, final Class<? extends PluginHook<P>> clazz) {
        final Plugin target = Bukkit.getPluginManager().getPlugin(name);

        if (target == null || !target.isEnabled()) {
            return;
        }

        try {
            if (hooks.putIfAbsent(clazz, clazz.getConstructor(plugin.getClass()).newInstance(plugin)) != null) {
                plugin.getLogger().warning("Failed to hook into " + name + ": There was already a hook registered with same name");
                return;
            }

            DuelsPlugin.sendMessage("&aSuccessfully hooked into '" + name + "'!");
        } catch (Throwable throwable) {
            Throwable throwable1 = throwable;
            if (throwable1.getCause() != null) {
                throwable1 = throwable1.getCause();
            }

            DuelsPlugin.sendMessage("&b&lFailed to hook into " + name + ": " + throwable1.getMessage());
        }
    }

    public <T extends PluginHook<P>> T getHook(Class<T> clazz) {
        return clazz != null ? clazz.cast(hooks.get(clazz)) : null;
    }
}
