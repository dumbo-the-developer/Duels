package me.realized.duels.util.hook;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import me.realized.duels.util.Loadable;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractHookManager<P extends JavaPlugin> implements Loadable {

    protected final P plugin;
    private final Map<Class<? extends PluginHook<P>>, PluginHook<P>> hooks = new HashMap<>();

    public AbstractHookManager(final P plugin) {
        this.plugin = plugin;
    }

    protected boolean register(final String name, final Class<? extends PluginHook<P>> clazz) {
        final Plugin target = Bukkit.getPluginManager().getPlugin(name);

        if (target == null || !target.isEnabled()) {
            return false;
        }

        try {
            final Constructor<? extends PluginHook<P>> constructor = clazz.getConstructor(plugin.getClass());
            final boolean result;

            if (result = constructor != null && hooks.putIfAbsent(clazz, constructor.newInstance(plugin)) == null) {
                plugin.getLogger().info("Successfully hooked into '" + name + "'!");
            }

            return result;
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ex) {
            plugin.getLogger().warning("Failed to hook into " + name + ": " + ex.getMessage());
        }

        return false;
    }

    public <T extends PluginHook<P>> T getHook(Class<T> clazz) {
        return clazz != null ? clazz.cast(hooks.get(clazz)) : null;
    }
}
