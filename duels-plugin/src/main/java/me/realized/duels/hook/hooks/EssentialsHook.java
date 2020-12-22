package me.realized.duels.hook.hooks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import java.lang.reflect.Field;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Config;
import me.realized.duels.util.compat.ReflectionUtil;
import me.realized.duels.util.hook.PluginHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EssentialsHook extends PluginHook<DuelsPlugin> {

    private static final Field LAST_LOC_FIELD;

    static {
        final Class<?> USERDATA_CLASS = ReflectionUtil.getClassUnsafe("com.earth2me.essentials.UserData");
        LAST_LOC_FIELD = ReflectionUtil.getDeclaredField(USERDATA_CLASS, "lastLocation");
    }

    public static final String NAME = "Essentials";

    private final Config config;

    public EssentialsHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();
    }

    public void tryUnvanish(final Player player) {
        if (!config.isAutoUnvanish()) {
            return;
        }

        final Essentials plugin = (Essentials) getPlugin();
        final User user = plugin.getUser(player);

        if (user != null && user.isVanished()) {
            user.setVanished(false);
        }
    }

    // Use reflection to prevent Essentials saving userdata file every time
    public void setBackLocation(final Player player, final Location location) {
        if (!config.isSetBackLocation()) {
            return;
        }

        final Essentials plugin = (Essentials) getPlugin();
        final User user = plugin.getUser(player);

        if (user != null) {
            try {
                LAST_LOC_FIELD.set(user, location);
            } catch (IllegalAccessException ignored) {}
        }
    }
}
