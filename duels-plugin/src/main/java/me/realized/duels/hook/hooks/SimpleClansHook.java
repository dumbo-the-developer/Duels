package me.realized.duels.hook.hooks;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Config;
import me.realized.duels.util.hook.PluginHook;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.entity.Player;

public class SimpleClansHook extends PluginHook<DuelsPlugin> {

    private final Config config;

    public SimpleClansHook(final DuelsPlugin plugin) {
        super(plugin, "SimpleClans");
        this.config = plugin.getConfiguration();

        try {
            getPlugin().getClass().getMethod("getClanManager");
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException("This version of " + getName() + " is not supported. Please try upgrading to the latest version.");
        }
    }

    public void removeDeath(final Player player) {
        if (!config.isPreventAddDeath()) {
            return;
        }

        final SimpleClans plugin = (SimpleClans) getPlugin();
        final ClanPlayer clanPlayer = plugin.getClanManager().getClanPlayer(player);

        if (clanPlayer == null) {
            return;
        }

        clanPlayer.setDeaths(clanPlayer.getDeaths() - 1);
    }
}
