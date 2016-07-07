package me.realized.duels.hooks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import me.realized.duels.Core;
import me.realized.duels.configuration.Config;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EssentialsHook extends PluginHook {

    private final Config config;

    public EssentialsHook(Core instance) {
        super("Essentials");
        this.config = instance.getConfiguration();

        if (isEnabled()) {
            instance.info("Hooked into Essentials! 'patcher' options are now functional.");
        }
    }

    public void setUnvanished(Player player) {
        if (!config.fixVanish() || !isEnabled()) {
            return;
        }

        Essentials essentials = (Essentials) getPlugin();
        User user = essentials.getUser(player);

        if (user != null) {
            user.setVanished(true);
            user.setVanished(false);
        }
    }

    public void setBackLocation(Player player, Location location) {
        if (!config.fixBack() || !isEnabled()) {
            return;
        }

        Essentials essentials = (Essentials) getPlugin();
        User user = essentials.getUser(player);

        if (user != null) {
            user.setLastLocation(location);
        }
    }
}
