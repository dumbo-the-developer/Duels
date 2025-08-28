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
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
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
        addLoadable("config", () -> {
            Config config = new Config(plugin);
            plugin.setConfiguration(config);
            return config;
        });
        addLoadable("lang", () -> {
            Lang lang = new Lang(plugin);
            plugin.setLang(lang);
            return lang;
        });
        addLoadable("user manager", () -> {
            UserManagerImpl userManager = new UserManagerImpl(plugin);
            plugin.setUserManager(userManager);
            return userManager;
        });
        addLoadable("gui listener", () -> {
            GuiListener<DuelsPlugin> guiListener = new GuiListener<>(plugin);
            plugin.setGuiListener(guiListener);
            return guiListener;
        });
        addLoadable("party manager", () -> {
            PartyManagerImpl partyManager = new PartyManagerImpl(plugin);
            plugin.setPartyManager(partyManager);
            return partyManager;
        });
        addLoadable("kit manager", () -> {
            KitManagerImpl kitManager = new KitManagerImpl(plugin);
            plugin.setKitManager(kitManager);
            return kitManager;
        });
        addLoadable("arena manager", () -> {
            ArenaManagerImpl arenaManager = new ArenaManagerImpl(plugin);
            plugin.setArenaManager(arenaManager);
            return arenaManager;
        });
        addLoadable("settings manager", () -> {
            SettingsManager settingsManager = new SettingsManager(plugin);
            plugin.setSettingManager(settingsManager);
            return settingsManager;
        });
        addLoadable("player manager", () -> {
            PlayerInfoManager playerManager = new PlayerInfoManager(plugin);
            plugin.setPlayerManager(playerManager);
            return playerManager;
        });
        addLoadable("spectate manager", () -> {
            SpectateManagerImpl spectateManager = new SpectateManagerImpl(plugin);
            plugin.setSpectateManager(spectateManager);
            return spectateManager;
        });
        addLoadable("betting manager", () -> {
            BettingManager bettingManager = new BettingManager(plugin);
            plugin.setBettingManager(bettingManager);
            return bettingManager;
        });
        addLoadable("inventory manager", () -> {
            InventoryManager inventoryManager = new InventoryManager(plugin);
            plugin.setInventoryManager(inventoryManager);
            return inventoryManager;
        });
        addLoadable("duel manager", () -> {
            DuelManager duelManager = new DuelManager(plugin);
            plugin.setDuelManager(duelManager);
            return duelManager;
        });
        addLoadable("queue manager", () -> {
            QueueManager queueManager = new QueueManager(plugin);
            plugin.setQueueManager(queueManager);
            return queueManager;
        });
        addLoadable("queue signs", () -> {
            QueueSignManagerImpl queueSignManager = new QueueSignManagerImpl(plugin);
            plugin.setQueueSignManager(queueSignManager);
            return queueSignManager;
        });
        addLoadable("request manager", () -> {
            RequestManager requestManager = new RequestManager(plugin);
            plugin.setRequestManager(requestManager);
            return requestManager;
        });
        addLoadable("validator manager", () -> {
            ValidatorManager validatorManager = new ValidatorManager(plugin);
            plugin.setValidatorManager(validatorManager);
            return validatorManager;
        });
        addLoadable("leaderboard manager", () -> {
            LeaderboardManager leaderboardManager = new LeaderboardManager(plugin);
            plugin.setLeaderboardManager(leaderboardManager);
            return leaderboardManager;
        });
        addLoadable("rank manager", () -> {
            RankManager rankManager = new RankManager(plugin);
            plugin.setRankManager(rankManager);
            return rankManager;
        });
        addLoadable("teleport manager", () -> {
            Teleport teleport = new Teleport(plugin);
            plugin.setTeleport(teleport);
            return teleport;
        });
        addLoadable("extension manager", () -> {
            ExtensionManager extensionManager = new ExtensionManager(plugin);
            plugin.setExtensionManager(extensionManager);
            return extensionManager;
        });
        
        // Hook manager is not a Loadable, so handle it separately
        try {
            HookManager hookManager = new HookManager(plugin);
            plugin.setHookManager(hookManager);
        } catch (Exception e) {
            DuelsPlugin.sendMessage("&cFailed to initialize hook manager: " + e.getMessage());
            throw new RuntimeException("Failed to initialize hook manager", e);
        }

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
        HandlerList.getRegisteredListeners(plugin).stream()
                .filter(listener -> ExtensionClassLoader.class.isInstance(listener.getListener().getClass().getClassLoader()))
                .forEach(listener -> HandlerList.unregisterAll(listener.getListener()));
    }
    
    private void addLoadable(String name, Supplier<Loadable> supplier) {
        try {
            Loadable loadable = supplier.get();
            loadables.add(loadable);
        } catch (Exception e) {
            DuelsPlugin.sendMessage("&cFailed to initialize " + name + ": " + e.getMessage());
            throw new RuntimeException("Failed to initialize " + name, e);
        }
    }
    
    public void addLoadable(Loadable loadable) {
        loadables.add(loadable);
    }
}