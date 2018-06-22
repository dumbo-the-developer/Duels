package me.realized.duels.hooks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.hook.PluginHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EssentialsHook extends PluginHook<DuelsPlugin> {

    public EssentialsHook(final DuelsPlugin plugin) {
        super(plugin, "Essentials");
    }

    public void tryUnvanish(final Player player) {
        if (!plugin.getConfiguration().isForceUnvanish()) {
            return;
        }

        final Essentials plugin = (Essentials) getPlugin();
        final User user = plugin.getUser(player);

        if (user != null && user.isVanished()) {
            user.setVanished(false);
        }
    }

    public void setBackLocation(final Player player, final Location location) {
        final Essentials plugin = (Essentials) getPlugin();
        final User user = plugin.getUser(player);

        if (user != null) {
            user.setLastLocation(location);
        }
    }
}
