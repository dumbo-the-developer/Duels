package com.meteordevelopments.duels.hook.hooks;

import com.artillexstudios.axgraves.api.events.GravePreSpawnEvent;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.util.hook.PluginHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AxGravesHook extends PluginHook<DuelsPlugin> {
    public static final String NAME = "AxGraves";

    private final Config config;
    private final ArenaManagerImpl arenaManager;

    public AxGravesHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();
        this.arenaManager = plugin.getArenaManager();

        try {
            Class.forName("com.artillexstudios.axgraves.api.events.GravePreSpawnEvent");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("This version of " + getName() + " is not supported. Please try upgrading to the latest version.");
        }
        Bukkit.getPluginManager().registerEvents(new AxGravesListener(), plugin);
    }

    public class AxGravesListener implements Listener{
        @EventHandler(ignoreCancelled = true)
        public void on(GravePreSpawnEvent event){
            if (!config.isAXpreventGraves()){
                return;
            }
            final Player player = event.getPlayer();

            if (!arenaManager.isInMatch(player)) {
                return;
            }

            event.getGrave().remove();
            event.setCancelled(true);
        }
    }
}
