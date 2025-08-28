package com.meteordevelopments.duels.startup;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.listeners.*;
import com.meteordevelopments.duels.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Manages registration and lifecycle of event listeners
 */
public class ListenerManager {
    
    private final DuelsPlugin plugin;
    private final List<Listener> registeredListeners = new ArrayList<>();
    
    public ListenerManager(DuelsPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Registers all pre-startup listeners
     */
    public void registerPreListeners() {
        long start = System.currentTimeMillis();
        DuelsPlugin.sendMessage("&eRegistering listeners...");
        
        registerListener(new KitItemListener(plugin));
        registerListener(new DamageListener(plugin));
        registerListener(new PotionListener(plugin));
        registerListener(new TeleportListener(plugin));
        registerListener(new ProjectileHitListener(plugin));
        registerListener(new EnderpearlListener(plugin));
        registerListener(new KitOptionsListener(plugin));
        registerListener(new LingerPotionListener(plugin));
        
        DuelsPlugin.sendMessage("&dSuccessfully registered listeners in &f[" + CC.getTimeDifferenceAndColor(start, System.currentTimeMillis()) + "&f]");
    }
    
    /**
     * Registers a single listener
     * @param listener the listener to register
     */
    public void registerListener(Listener listener) {
        Objects.requireNonNull(listener, "listener");
        registeredListeners.add(listener);
        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }
    
    /**
     * Registers a listener with timing feedback (for post-startup listeners)
     * @param listener the listener to register
     */
    public void registerListenerWithTiming(Listener listener) {
        long start = System.currentTimeMillis();
        DuelsPlugin.sendMessage("&eRegistering post listeners...");
        
        registerListener(listener);
        
        DuelsPlugin.sendMessage("&dSuccessfully registered listeners after plugin startup in [" + CC.getTimeDifferenceAndColor(start, System.currentTimeMillis()) + ChatColor.WHITE + "]");
    }
    
    /**
     * Unregisters all managed listeners
     */
    public void unregisterAllListeners() {
        registeredListeners.forEach(HandlerList::unregisterAll);
        registeredListeners.clear();
    }
}