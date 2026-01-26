package com.meteordevelopments.duels.hook.hooks;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.util.hook.PluginHook;
import nl.marido.deluxecombat.api.DeluxeCombatAPI;
import nl.marido.deluxecombat.events.CombatStateChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DeluxeCombatHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "DeluxeCombat";

    private final Config config;
    private final ArenaManagerImpl arenaManager;
    DeluxeCombatAPI api = new DeluxeCombatAPI();

    public DeluxeCombatHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();
        this.arenaManager = plugin.getArenaManager();

        try {
            Class.forName("nl.marido.deluxecombat.events.CombatStateChangeEvent");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("This version of " + getName() + " is not supported. Please try upgrading to the latest version.");
        }

        Bukkit.getPluginManager().registerEvents(new DeluxeCombatListener(), plugin);
    }

    public boolean isTagged(final Player player) {
        return config.isDcPreventDuel() && api.isInCombat(player);
    }

    public class DeluxeCombatListener implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void on(final CombatStateChangeEvent event) {
            if (!config.isDcPreventTag()) {
                return;
            }

            final Player player = event.getPlayer();

            if (!arenaManager.isInMatch(player)) {
                return;
            }

            api.untag(player);
        }
    }

}
