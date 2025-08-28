package com.meteordevelopments.duels.startup;

import com.google.common.collect.Lists;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.util.Loadable;
import com.meteordevelopments.duels.util.Reloadable;
import com.meteordevelopments.duels.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.betting.BettingManager;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.config.Lang;
import com.meteordevelopments.duels.data.UserManagerImpl;
import com.meteordevelopments.duels.duel.DuelManager;
import com.meteordevelopments.duels.extension.ExtensionClassLoader;
import com.meteordevelopments.duels.extension.ExtensionManager;
import com.meteordevelopments.duels.hook.HookManager;
import com.meteordevelopments.duels.inventories.InventoryManager;
import com.meteordevelopments.duels.kit.KitManagerImpl;
import com.meteordevelopments.duels.lb.manager.LeaderboardManager;
import com.meteordevelopments.duels.logging.LogManager;
import com.meteordevelopments.duels.party.PartyManagerImpl;
import com.meteordevelopments.duels.player.PlayerInfoManager;
import com.meteordevelopments.duels.queue.QueueManager;
import com.meteordevelopments.duels.queue.sign.QueueSignManagerImpl;
import com.meteordevelopments.duels.rank.manager.RankManager;
import com.meteordevelopments.duels.request.RequestManager;
import com.meteordevelopments.duels.setting.SettingsManager;
import com.meteordevelopments.duels.spectate.SpectateManagerImpl;
import com.meteordevelopments.duels.teleport.Teleport;
import com.meteordevelopments.duels.util.CC;
import com.meteordevelopments.duels.util.gui.GuiListener;
import com.meteordevelopments.duels.validator.ValidatorManager;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Manages the lifecycle of all loadable components
 */
public class LoadableManager {
    
    private static final Logger LOGGER = Logger.getLogger("[Duels-Optimised]");
    
    private final DuelsPlugin plugin;
    private final List<Loadable> loadables = new ArrayList<>();
    private int lastLoad = -1;
    
    public LoadableManager(DuelsPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Initializes all loadable components
     * @return true if successful, false otherwise
     */
    public boolean initializeLoadables() {
        long start = System.currentTimeMillis();
        DuelsPlugin.sendMessage("&eInitializing components...");
        
        // Initialize all loadables
        addLoadable("config", () -> plugin.setConfiguration(new Config(plugin)));
        addLoadable("lang", () -> plugin.setLang(new Lang(plugin)));
        addLoadable("user manager", () -> plugin.setUserManager(new UserManagerImpl(plugin)));
        addLoadable("gui listener", () -> plugin.setGuiListener(new GuiListener<>(plugin)));
        addLoadable("party manager", () -> plugin.setPartyManager(new PartyManagerImpl(plugin)));
        addLoadable("kit manager", () -> plugin.setKitManager(new KitManagerImpl(plugin)));
        addLoadable("arena manager", () -> plugin.setArenaManager(new ArenaManagerImpl(plugin)));
        addLoadable("settings manager", () -> plugin.setSettingManager(new SettingsManager(plugin)));
        addLoadable("player manager", () -> plugin.setPlayerManager(new PlayerInfoManager(plugin)));
        addLoadable("spectate manager", () -> plugin.setSpectateManager(new SpectateManagerImpl(plugin)));
        addLoadable("betting manager", () -> plugin.setBettingManager(new BettingManager(plugin)));
        addLoadable("inventory manager", () -> plugin.setInventoryManager(new InventoryManager(plugin)));
        addLoadable("duel manager", () -> plugin.setDuelManager(new DuelManager(plugin)));
        addLoadable("queue manager", () -> plugin.setQueueManager(new QueueManager(plugin)));
        addLoadable("queue signs", () -> plugin.setQueueSignManager(new QueueSignManagerImpl(plugin)));
        addLoadable("request manager", () -> plugin.setRequestManager(new RequestManager(plugin)));
        addLoadable("hook manager", () -> plugin.setHookManager(new HookManager(plugin)));
        addLoadable("validator manager", () -> plugin.setValidatorManager(new ValidatorManager(plugin)));
        addLoadable("leaderboard manager", () -> plugin.setLeaderboardManager(new LeaderboardManager(plugin)));
        addLoadable("rank manager", () -> plugin.setRankManager(new RankManager(plugin)));
        addLoadable("teleport manager", () -> plugin.setTeleport(new Teleport(plugin)));
        addLoadable("extension manager", () -> plugin.setExtensionManager(new ExtensionManager(plugin)));

        if (!loadAll()) {
            return false;
        }

        DuelsPlugin.sendMessage("&dSuccessfully initialized all components in &f[" + CC.getTimeDifferenceAndColor(start, System.currentTimeMillis()) + "&f]");
        return true;
    }
    
    /**
     * Loads all initialized loadables
     * @return true if successful, false otherwise
     */
    public boolean loadAll() {
        for (final Loadable loadable : loadables) {
            final String name = loadable.getClass().getSimpleName();

            try {
                final long now = System.currentTimeMillis();
                plugin.getLogManager().debug("Starting load of " + name + " at " + now);
                loadable.handleLoad();
                plugin.getLogManager().debug(name + " has been loaded. (took " + (System.currentTimeMillis() - now) + "ms)");
                lastLoad = loadables.indexOf(loadable);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error loading " + name, ex);

                if (loadable instanceof LogManager) {
                    LOGGER.log(Level.SEVERE, "Error loading LogManager", ex);
                }

                DuelsPlugin.sendMessage("&c&lThere was an error while loading " + name + "! If you believe this is an issue from the plugin, please contact the developer.");
                return false;
            }
        }
        return true;
    }
    
    /**
     * Unloads all loadables in reverse order
     * @return true if successful, false otherwise
     */
    public boolean unloadAll() {
        for (final Loadable loadable : Lists.reverse(loadables)) {
            final String name = loadable.getClass().getSimpleName();

            try {
                if (loadables.indexOf(loadable) > lastLoad) {
                    continue;
                }

                final long now = System.currentTimeMillis();
                plugin.getLogManager().debug("Starting unload of " + name + " at " + now);
                loadable.handleUnload();
                plugin.getLogManager().debug(name + " has been unloaded. (took " + (System.currentTimeMillis() - now) + "ms)");
            } catch (Exception ex) {
                DuelsPlugin.sendMessage("&c&lThere was an error while unloading " + name + "! If you believe this is an issue from the plugin, please contact the developer.");
                return false;
            }
        }
        return true;
    }
    
    /**
     * Reloads a specific loadable
     * @param loadable the loadable to reload
     * @return true if successful, false otherwise
     */
    public boolean reload(final Loadable loadable) {
        boolean unloaded = false;
        try {
            loadable.handleUnload();
            unloaded = true;
            loadable.handleLoad();
            return true;
        } catch (Exception ex) {
            DuelsPlugin.sendMessage("&c&lThere was an error while " + (unloaded ? "loading " : "unloading ")
                    + loadable.getClass().getSimpleName()
                    + "! If you believe this is an issue from the plugin, please contact the developer.");
            return false;
        }
    }
    
    /**
     * Finds a loadable by name
     * @param name the class simple name
     * @return the loadable or null if not found
     */
    public Loadable find(final String name) {
        return loadables.stream()
                .filter(loadable -> loadable.getClass().getSimpleName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Gets all reloadable component names
     * @return list of reloadable names
     */
    public List<String> getReloadableNames() {
        return loadables.stream()
                .filter(loadable -> loadable instanceof Reloadable)
                .map(loadable -> loadable.getClass().getSimpleName())
                .collect(Collectors.toList());
    }
    
    /**
     * Cleans up extension listeners
     */
    public void cleanupExtensionListeners() {
        HandlerList.getRegisteredListeners(plugin)
                .stream()
                .filter(listener -> listener.getListener().getClass().getClassLoader().getClass().isAssignableFrom(ExtensionClassLoader.class))
                .forEach(listener -> HandlerList.unregisterAll(listener.getListener()));
    }
    
    private void addLoadable(String name, Runnable initializer) {
        try {
            initializer.run();
        } catch (Exception e) {
            DuelsPlugin.sendMessage("&cFailed to initialize " + name + ": " + e.getMessage());
            throw new RuntimeException("Failed to initialize " + name, e);
        }
    }
    
    public void addLoadable(Loadable loadable) {
        loadables.add(loadable);
    }
}