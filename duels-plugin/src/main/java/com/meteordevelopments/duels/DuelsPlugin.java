package com.meteordevelopments.duels;

import com.meteordevelopments.duels.startup.*;
import com.meteordevelopments.duels.util.CC;
import com.meteordevelopments.duels.util.UpdateManager;
import com.meteordevelopments.duels.party.PartyManagerImpl;
import com.meteordevelopments.duels.validator.ValidatorManager;
import com.meteordevelopments.duels.util.Log;
import com.meteordevelopments.duels.util.Log.LogSource;
import com.meteordevelopments.duels.util.json.JsonUtil;
import com.meteordevelopments.duels.data.ItemData;
import com.meteordevelopments.duels.data.ItemData.ItemDataDeserializer;
import com.meteordevelopments.duels.shaded.bstats.Metrics;
import com.meteordevelopments.duels.util.Loadable;
import com.meteordevelopments.duels.config.DatabaseConfig;
import com.meteordevelopments.duels.mongo.MongoService;
import com.meteordevelopments.duels.redis.RedisService;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.config.Lang;
import com.meteordevelopments.duels.data.UserManagerImpl;
import com.meteordevelopments.duels.util.gui.GuiListener;
import com.meteordevelopments.duels.kit.KitManagerImpl;
import com.meteordevelopments.duels.setting.SettingsManager;
import com.meteordevelopments.duels.player.PlayerInfoManager;
import com.meteordevelopments.duels.spectate.SpectateManagerImpl;
import com.meteordevelopments.duels.inventories.InventoryManager;
import com.meteordevelopments.duels.duel.DuelManager;
import com.meteordevelopments.duels.queue.QueueManager;
import com.meteordevelopments.duels.queue.sign.QueueSignManagerImpl;
import com.meteordevelopments.duels.request.RequestManager;
import com.meteordevelopments.duels.hook.HookManager;
import com.meteordevelopments.duels.teleport.Teleport;
import com.meteordevelopments.duels.extension.ExtensionManager;
import com.meteordevelopments.duels.lb.manager.LeaderboardManager;
import com.meteordevelopments.duels.rank.manager.RankManager;
import com.meteordevelopments.duels.logging.LogManager;
import lombok.Getter;
import lombok.Setter;
import com.meteordevelopments.duels.api.Duels;
import com.meteordevelopments.duels.api.command.SubCommand;
import com.meteordevelopments.duels.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.betting.BettingManager;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import space.arim.morepaperlib.MorePaperLib;
import space.arim.morepaperlib.scheduling.ScheduledTask;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;


public class DuelsPlugin extends JavaPlugin implements Duels, LogSource {

    private static final int BSTATS_ID = 20778;
    
    @Getter
    private UpdateManager updateManager;
    @Getter
    private static DuelsPlugin instance;
    @Getter
    private static MorePaperLib morePaperLib;

    
    // Managers
    private StartupManager startupManager;
    private LoadableManager loadableManager;
    private CommandRegistrar commandRegistrar;
    private ListenerManager listenerManager;
    // Plugin components
    @Getter @Setter private LogManager logManager;
    @Getter @Setter private Config configuration;
    @Getter @Setter private Lang lang;
    @Getter @Setter private UserManagerImpl userManager;
    @Getter @Setter private GuiListener<DuelsPlugin> guiListener;
    @Getter @Setter private KitManagerImpl kitManager;
    @Getter @Setter private ArenaManagerImpl arenaManager;
    @Getter @Setter private SettingsManager settingManager;
    @Getter @Setter private PlayerInfoManager playerManager;
    @Getter @Setter private SpectateManagerImpl spectateManager;
    @Getter @Setter private BettingManager bettingManager;
    @Getter @Setter private InventoryManager inventoryManager;
    @Getter @Setter private DuelManager duelManager;
    @Getter @Setter private QueueManager queueManager;
    @Getter @Setter private QueueSignManagerImpl queueSignManager;
    @Getter @Setter private RequestManager requestManager;
    @Getter @Setter private HookManager hookManager;
    @Getter @Setter private Teleport teleport;
    @Getter @Setter private ExtensionManager extensionManager;
    @Getter @Setter private PartyManagerImpl partyManager;
    @Getter @Setter private ValidatorManager validatorManager;
    @Getter @Setter private LeaderboardManager leaderboardManager;
    @Getter @Setter private RankManager rankManager;
    @Getter @Setter private MongoService mongoService;
    @Getter @Setter private RedisService redisService;
    @Getter @Setter private DatabaseConfig databaseConfig;
    private redis.clients.jedis.JedisPubSub redisSubscriber;
    private static final Logger LOGGER = Logger.getLogger("[Duels-Optimised]");

    @Override
    public void onEnable() {
        instance = this;
        morePaperLib = new MorePaperLib(this);
        Log.addSource(this);
        JsonUtil.registerDeserializer(ItemData.class, ItemDataDeserializer.class);
        
        // Initialize managers
        startupManager = new StartupManager(this);
        loadableManager = new LoadableManager(this);
        commandRegistrar = new CommandRegistrar(this);
        listenerManager = new ListenerManager(this);
        
        // Handle startup
        if (!startupManager.startup()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Initialize and load components
        if (!loadableManager.initializeLoadables()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Register commands and listeners
        commandRegistrar.registerCommands();
        listenerManager.registerPreListeners();
        
        // Setup Redis subscriptions after managers are loaded
        if (redisService != null) {
            setupRedisSubscriptions();
        }
        
        checkForUpdatesAndMetrics();
    }

    @Override
    public void onDisable() {
        final long start = System.currentTimeMillis();
        long last = start;
        
        if (logManager != null) {
            logManager.debug("onDisable start -> " + start + "\n");
        }
        
        // Unload components
        if (loadableManager != null) {
            loadableManager.unloadAll();
            loadableManager.cleanupExtensionListeners();
        }
        
        // Unregister listeners
        if (listenerManager != null) {
            listenerManager.unregisterAllListeners();
        }
        
        // Clear commands
        if (commandRegistrar != null) {
            commandRegistrar.clearCommands();
        }
        
        if (logManager != null) {
            logManager.debug("unload done (took " + Math.abs(last - (last = System.currentTimeMillis())) + "ms)");
        }
        
        Log.clearSources();
        
        if (logManager != null) {
            logManager.debug("Log#clearSources done (took " + Math.abs(last - System.currentTimeMillis()) + "ms)");
            logManager.handleDisable();
        }
        
        // Close database connections
        if (mongoService != null) {
            mongoService.close();
        }
        if (redisService != null) {
            try {
                if (redisSubscriber != null) {
                    redisSubscriber.unsubscribe();
                }
            } catch (Exception ignored) {}
            redisService.close();
            redisSubscriber = null;
        }
        
        instance = null;
        sendMessage("&2Disable process took " + (System.currentTimeMillis() - start) + "ms.");
    }



    @Override
    public boolean registerSubCommand(@NotNull final String command, @NotNull final SubCommand subCommand) {
        return commandRegistrar.registerSubCommand(command, subCommand);
    }

    @Override
    public void registerListener(@NotNull final Listener listener) {
        listenerManager.registerListenerWithTiming(listener);
    }

    @Override
    public boolean reload() {
        if (!(loadableManager.unloadAll() && loadableManager.loadAll())) {
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
        return true;
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }

    public boolean reload(final Loadable loadable) {
        return loadableManager.reload(loadable);
    }

    @Override
    public ScheduledTask doSync(@NotNull final Runnable task) {
        Objects.requireNonNull(task, "task");
        return DuelsPlugin.morePaperLib.scheduling().globalRegionalScheduler().run(task);
    }

    @Override
    public ScheduledTask doSyncAfter(@NotNull final Runnable task, final long delay) {
        Objects.requireNonNull(task, "task");
        return DuelsPlugin.morePaperLib.scheduling().globalRegionalScheduler().runDelayed(task, delay);
    }

    @Override
    public ScheduledTask doSyncRepeat(@NotNull final Runnable task, final long delay, final long period) {
        Objects.requireNonNull(task, "task");

        long safeDelay = Math.max(1, delay);

        return DuelsPlugin.morePaperLib.scheduling()
                .globalRegionalScheduler()
                .runAtFixedRate(task, safeDelay, period);
    }


    @Override
    public ScheduledTask doAsync(@NotNull final Runnable task) {
        Objects.requireNonNull(task, "task");
        return DuelsPlugin.morePaperLib.scheduling().asyncScheduler().run(task);
    }

    @Override
    public ScheduledTask doAsyncAfter(@NotNull final Runnable task, final long delay) {
        Objects.requireNonNull(task, "task");
        return DuelsPlugin.morePaperLib.scheduling().asyncScheduler().runDelayed(task, Duration.ofMillis(delay * 50));
    }

    @Override
    public ScheduledTask doAsyncRepeat(@NotNull final Runnable task, final long delay, final long period) {
        Objects.requireNonNull(task, "task");
        return DuelsPlugin.morePaperLib.scheduling().asyncScheduler().runAtFixedRate(task, Duration.ofMillis(delay * 50), Duration.ofMillis(period * 50));
    }

    @Override
    public void cancelTask(@NotNull final ScheduledTask task) {
        Objects.requireNonNull(task, "task");
        task.cancel();
    }

    @Override
    public void info(@NotNull final String message) {
        Objects.requireNonNull(message, "message");
        Log.info(message);
    }

    @Override
    public void warn(@NotNull final String message) {
        Objects.requireNonNull(message, "message");
        Log.warn(message);
    }

    @Override
    public void error(@NotNull final String message) {
        Objects.requireNonNull(message, "message");
        Log.error(message);
    }

    @Override
    public void error(@NotNull final String message, @NotNull final Throwable thrown) {
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(thrown, "thrown");
        Log.error(message, thrown);
    }

    public Loadable find(final String name) {
        return loadableManager.find(name);
    }

    public List<String> getReloadables() {
        return loadableManager.getReloadableNames();
    }

    @Override
    public void log(final Level level, final String s) {
        getLogger().log(level, s);
    }

    @Override
    public void log(final Level level, final String s, final Throwable thrown) {
        getLogger().log(level, s, thrown);
    }

    public static String getPrefix() {
        return CC.translate("&b&lDuels Optimised &7» ");
    }

    public static void sendMessage(String message) {
        Bukkit.getConsoleSender().sendMessage(getPrefix() + CC.translate(message));
    }

    /**
     * Initializes the log manager (called by StartupManager)
     * @throws IOException if log manager cannot be created
     */
    public void initializeLogManager() throws IOException {
        logManager = new LogManager(this);
        Log.addSource(logManager);
        logManager.debug("onEnable start -> " + System.currentTimeMillis() + "\n");
    }

    private void checkForUpdatesAndMetrics() {
        new Metrics(this, BSTATS_ID);

        if (!configuration.isCheckForUpdates()) {
            return;
        }

        this.updateManager = new UpdateManager(this);
        this.updateManager.checkForUpdate();
        if (updateManager.updateIsAvailable()){
            sendMessage("&a===============================================");
            sendMessage("&aAn update for " + getName() + " is available!");
            sendMessage("&aDownload " + getName() + " v" + updateManager.getLatestVersion() + " here:");
            sendMessage("&e" + getDescription().getWebsite());
            sendMessage("&a===============================================");
        }
    }

    private void setupRedisSubscriptions() {
        try {
            // Ensure we don't stack multiple subscribers across reloads
            if (this.redisSubscriber != null) {
                try {
                    this.redisSubscriber.unsubscribe();
                } catch (Exception ignored) {}
                this.redisSubscriber = null;
            }
            final var sub = new redis.clients.jedis.JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    doSync(() -> {
                        // Expect messages formatted as "serverId:payload"; ignore self-originated
                        String payload = message;
                        try {
                            final int idx = message.indexOf(':');
                            if (idx > 0) {
                                final String origin = message.substring(0, idx);
                                if (origin.equals(getSelfServerId())) {
                                    return;
                                }
                                payload = message.substring(idx + 1);
                            }
                        } catch (Exception ignored) {}
                        switch (channel) {
                            case RedisService.CHANNEL_INVALIDATE_USER -> {
                                try {
                                    final UUID uuid = UUID.fromString(payload);
                                    if (userManager != null) userManager.reloadUser(uuid);
                                } catch (Exception ignored) {
                                }
                            }
                            case RedisService.CHANNEL_INVALIDATE_KIT -> {
                                if (kitManager != null) kitManager.reloadKit(payload);
                            }
                            case RedisService.CHANNEL_INVALIDATE_ARENA -> {
                                if (arenaManager != null) arenaManager.reloadArena(payload);
                            }
                        }
                    });
                }
            };
            this.redisSubscriber = sub;
            redisService.subscribe(sub,
                    com.meteordevelopments.duels.redis.RedisService.CHANNEL_INVALIDATE_USER,
                    com.meteordevelopments.duels.redis.RedisService.CHANNEL_INVALIDATE_KIT,
                    com.meteordevelopments.duels.redis.RedisService.CHANNEL_INVALIDATE_ARENA
            );
        } catch (Exception ex) {
            sendMessage("&eFailed to subscribe to Redis channels; continuing without cross-server sync.");
        }
    }

    private String getSelfServerId() {
        final String configured = databaseConfig != null ? databaseConfig.getServerId() : null;
        if (configured != null && !configured.trim().isEmpty()) {
            return configured.trim();
        }
        final int port = getServer().getPort();
        return port > 0 ? String.valueOf(port) : "default";
    }
}
