package com.meteordevelopments.duels.hook.hooks;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.util.hook.PluginHook;
import me.chancesd.pvpmanager.event.PlayerTagEvent;
import me.chancesd.pvpmanager.player.CombatPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PvPManagerHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "PvPManager";

    private final Config config;
    private final ArenaManagerImpl arenaManager;

    public PvPManagerHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();
        this.arenaManager = plugin.getArenaManager();

        try {
            Class.forName("me.chancesd.pvpmanager.event.PlayerTagEvent");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("This version of " + getName() + " is not supported. Please try upgrading to the latest version.");
        }

        Bukkit.getPluginManager().registerEvents(new PvPManagerListener(), plugin);
    }

    public boolean isTagged(final Player player) {
        if (config.isPmPreventDuel()) {
            return false;
        }

        final CombatPlayer pvPlayer = CombatPlayer.get(player);
        return pvPlayer != null && pvPlayer.isInCombat();
    }

    public class PvPManagerListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerTagEvent event) {
            if (!config.isPmPreventTag()) {
                return;
            }

            final Player player = event.getPlayer();

            if (!arenaManager.isInMatch(player)) {
                return;
            }

            event.setCancelled(true);
        }
    }
}
