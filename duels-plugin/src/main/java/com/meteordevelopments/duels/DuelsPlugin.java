package com.meteordevelopments.duels;

import com.google.common.collect.Lists;
import com.meteordevelopments.duels.command.commands.party.PartyCommand;
import com.meteordevelopments.duels.config.CommandsConfig;
import com.meteordevelopments.duels.listeners.*;
import com.meteordevelopments.duels.party.PartyManagerImpl;
import com.meteordevelopments.duels.util.*;
import com.meteordevelopments.duels.validator.ValidatorManager;
import com.meteordevelopments.duels.api.folialib.FoliaLib;
import lombok.Getter;
import com.meteordevelopments.duels.api.Duels;
import com.meteordevelopments.duels.api.command.SubCommand;
import com.meteordevelopments.duels.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.betting.BettingManager;
import com.meteordevelopments.duels.command.commands.SpectateCommand;
import com.meteordevelopments.duels.command.commands.KitCommand;
import com.meteordevelopments.duels.command.commands.duel.DuelCommand;
import com.meteordevelopments.duels.command.commands.duels.DuelsCommand;
import com.meteordevelopments.duels.command.commands.queue.QueueCommand;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.config.Lang;
import com.meteordevelopments.duels.data.ItemData;
import com.meteordevelopments.duels.data.ItemData.ItemDataDeserializer;
import com.meteordevelopments.duels.data.UserManagerImpl;
import com.meteordevelopments.duels.duel.DuelManager;
import com.meteordevelopments.duels.extension.ExtensionClassLoader;
import com.meteordevelopments.duels.extension.ExtensionManager;
import com.meteordevelopments.duels.hook.HookManager;
import com.meteordevelopments.duels.inventories.InventoryManager;
import com.meteordevelopments.duels.kit.KitManagerImpl;
import com.meteordevelopments.duels.logging.LogManager;
import com.meteordevelopments.duels.player.PlayerInfoManager;
import com.meteordevelopments.duels.queue.QueueManager;
import com.meteordevelopments.duels.queue.sign.QueueSignManagerImpl;
import com.meteordevelopments.duels.request.RequestManager;
import com.meteordevelopments.duels.setting.SettingsManager;
import com.meteordevelopments.duels.shaded.bstats.Metrics;
import com.meteordevelopments.duels.spectate.SpectateManagerImpl;
import com.meteordevelopments.duels.teleport.Teleport;
import com.meteordevelopments.duels.util.Log.LogSource;
import com.meteordevelopments.duels.util.command.AbstractCommand;
import com.meteordevelopments.duels.util.gui.GuiListener;
import com.meteordevelopments.duels.util.json.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import com.meteordevelopments.duels.api.folialib.task.WrappedTask;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class DuelsPlugin extends JavaPlugin implements Duels, LogSource {

    private static final int BSTATS_ID = 20778;
    private static final String SPIGOT_INSTALLATION_URL = "https://www.spigotmc.org/wiki/spigot-installation/";
    @Getter
    private UpdateManager updateManager;

    @Getter
    private static DuelsPlugin instance;
    @Getter
    private static FoliaLib foliaLib;

    private final List<Loadable> loadables = new ArrayList<>();
    private final Map<String, AbstractCommand<DuelsPlugin>> commands = new HashMap<>();
    private final Map<String, String> commandKeyMap = new HashMap<>();
    private final List<Listener> registeredListeners = new ArrayList<>();
    private int lastLoad;
    @Getter
    private LogManager logManager;
    @Getter
    private Config configuration;
    @Getter
    private Lang lang;
    @Getter
    private UserManagerImpl userManager;
    @Getter
    private GuiListener<DuelsPlugin> guiListener;
    @Getter
    private KitManagerImpl kitManager;
    @Getter
    private ArenaManagerImpl arenaManager;
    @Getter
    private SettingsManager settingManager;
    @Getter
    private PlayerInfoManager playerManager;
    @Getter
    private SpectateManagerImpl spectateManager;
    @Getter
    private BettingManager bettingManager;
    @Getter
    private InventoryManager inventoryManager;
    @Getter
    private DuelManager duelManager;
    @Getter
    private QueueManager queueManager;
    @Getter
    private QueueSignManagerImpl queueSignManager;
    @Getter
    private RequestManager requestManager;
    @Getter
    private HookManager hookManager;
    @Getter
    private Teleport teleport;
    @Getter
    private ExtensionManager extensionManager;
    @Getter
    private PartyManagerImpl partyManager;
    @Getter
    private ValidatorManager validatorManager;
    private CommandsConfig commandsConfig;
    private static final Logger LOGGER = Logger.getLogger("[Duels-Optimised]");

    @Override
    public void onEnable() {

        instance = this;
        foliaLib = new FoliaLib(this);
        Log.addSource(this);
        JsonUtil.registerDeserializer(ItemData.class, ItemDataDeserializer.class);

        sendBanner();

        long start = System.currentTimeMillis();

        loadLogManager();
        initLoadables();
        registerAllCommands();
        loadExtensions();
        loadPreListeners();

        long end = System.currentTimeMillis();
        sendMessage("&2Successfully enabled Duels in " + CC.getTimeDifferenceAndColor(start, end) + "&a.");
        checkForUpdatesAndMetrics();
    }

    @Override
    public void onDisable() {
        final long start = System.currentTimeMillis();
        long last = start;
        logManager.debug("onDisable start -> " + start + "\n");
        unload();
        logManager.debug("unload done (took " + Math.abs(last - (last = System.currentTimeMillis())) + "ms)");
        Log.clearSources();
        logManager.debug("Log#clearSources done (took " + Math.abs(last - System.currentTimeMillis()) + "ms)");
        logManager.handleDisable();
        instance = null;
        sendMessage("&2Disable process took " + (System.currentTimeMillis() - start) + "ms.");
    }

    /**
     * @return true if load was successful, otherwise false
     */
    private boolean load() {
        for (final Loadable loadable : loadables) {
            final String name = loadable.getClass().getSimpleName();

            try {
                final long now = System.currentTimeMillis();
                logManager.debug("Starting load of " + name + " at " + now);
                loadable.handleLoad();
                logManager.debug(name + " has been loaded. (took " + (System.currentTimeMillis() - now) + "ms)");
                lastLoad = loadables.indexOf(loadable);
            } catch (Exception ex) {
                // Print the stacktrace to help with debugging
                LOGGER.log(Level.SEVERE, "Error loading " + name, ex);

                // Handles the case of exceptions from LogManager not being logged in file
                if (loadable instanceof LogSource) {
                    LOGGER.log(Level.SEVERE, "Error loading ", ex);
                }

                sendMessage("&c&lThere was an error while loading " + name + "! If you believe this is an issue from the plugin, please contact the developer.");
                return false;
            }
        }

        return true;
    }

    /**
     * @return true if unload was successful, otherwise false
     */
    private boolean unload() {
        registeredListeners.forEach(HandlerList::unregisterAll);
        registeredListeners.clear();
        // Unregister all extension listeners that isn't using the method Duels#registerListener
        HandlerList.getRegisteredListeners(this)
                .stream()
                .filter(listener -> listener.getListener().getClass().getClassLoader().getClass().isAssignableFrom(ExtensionClassLoader.class))
                .forEach(listener -> HandlerList.unregisterAll(listener.getListener()));
        commands.clear();

        for (final Loadable loadable : Lists.reverse(loadables)) {
            final String name = loadable.getClass().getSimpleName();

            try {
                if (loadables.indexOf(loadable) > lastLoad) {
                    continue;
                }

                final long now = System.currentTimeMillis();
                logManager.debug("Starting unload of " + name + " at " + now);
                loadable.handleUnload();
                logManager.debug(name + " has been unloaded. (took " + (System.currentTimeMillis() - now) + "ms)");
            } catch (Exception ex) {
                sendMessage("&c&lThere was an error while unloading " + name + "! If you believe this is an issue from the plugin, please contact the developer.");
                return false;
            }
        }

        return true;
    }

    private void registerAllCommands() {
        sendMessage("&eRegistering commands...");
        long start = System.currentTimeMillis();

        // Build commands based on commands.yml
        final CommandsConfig.CommandSettings duel = commandsConfig.get(CommandsConfig.CommandKey.DUEL);
        final CommandsConfig.CommandSettings party = commandsConfig.get(CommandsConfig.CommandKey.PARTY);
        final CommandsConfig.CommandSettings queue = commandsConfig.get(CommandsConfig.CommandKey.QUEUE);
        final CommandsConfig.CommandSettings spectate = commandsConfig.get(CommandsConfig.CommandKey.SPECTATE);
        final CommandsConfig.CommandSettings duels = commandsConfig.get(CommandsConfig.CommandKey.DUELS);
        final CommandsConfig.CommandSettings kit = commandsConfig.get(CommandsConfig.CommandKey.KIT);

        // Store mappings from original keys to actual names for API compatibility
        commandKeyMap.put("duel", duel.getName().toLowerCase());
        commandKeyMap.put("party", party.getName().toLowerCase());
        commandKeyMap.put("queue", queue.getName().toLowerCase());
        commandKeyMap.put("spectate", spectate.getName().toLowerCase());
        commandKeyMap.put("duels", duels.getName().toLowerCase());
        commandKeyMap.put("kit", kit.getName().toLowerCase());

        registerCommands(
            new DuelCommand(this, duel),
            new PartyCommand(this, party),
            new QueueCommand(this, queue),
            new SpectateCommand(this, spectate),
            new DuelsCommand(this, duels),
            new KitCommand(this, kit)
        );

        sendMessage("&dSuccessfully registered commands [" + CC.getTimeDifferenceAndColor(start, System.currentTimeMillis()) + ChatColor.WHITE + "]");
    }

    @SafeVarargs
    private void registerCommands(final AbstractCommand<DuelsPlugin>... commands) {
        final CommandMap commandMap = getCommandMap();
        if (commandMap == null) {
            getLogger().severe("Could not access Bukkit CommandMap for dynamic registration.");
            return;
        }

        // Get knownCommands map for direct registration
        final Map<String, org.bukkit.command.Command> knownCommands = getKnownCommands(commandMap);

        for (final AbstractCommand<DuelsPlugin> command : commands) {
            this.commands.put(command.getName().toLowerCase(), command);
            final PluginCommand pc = createPluginCommand(command.getName());
            if (pc == null) {
                getLogger().warning("Failed to create PluginCommand for '" + command.getName() + "'. Skipping.");
                continue;
            }
            pc.setAliases(command.getAliases());
            pc.setDescription("Duels Optimised command: " + command.getName());
            command.register(pc);

            // Register with CommandMap
            final String prefix = getDescription().getName().toLowerCase();
            commandMap.register(prefix, pc);

            // Also register in knownCommands directly (primary name + aliases)
            if (knownCommands != null) {
                final String primaryName = command.getName().toLowerCase();
                knownCommands.put(primaryName, pc);
                knownCommands.put(prefix + ":" + primaryName, pc);

                for (final String alias : command.getAliases()) {
                    final String aliasLower = alias.toLowerCase();
                    knownCommands.put(aliasLower, pc);
                    knownCommands.put(prefix + ":" + aliasLower, pc);
                }
            }

            getLogger().info("Registered command: /" + command.getName() + 
                           (command.getAliases().isEmpty() ? "" : " (aliases: " + String.join(", ", command.getAliases()) + ")"));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, org.bukkit.command.Command> getKnownCommands(final CommandMap commandMap) {
        // Try multiple possible field names across different Paper/Spigot versions
        final String[] fieldNames = {"knownCommands", "commands"};
        
        for (final String fieldName : fieldNames) {
            Class<?> clazz = commandMap.getClass();
            while (clazz != null) {
                try {
                    final java.lang.reflect.Field f = clazz.getDeclaredField(fieldName);
                    f.setAccessible(true);
                    return (Map<String, org.bukkit.command.Command>) f.get(commandMap);
                } catch (NoSuchFieldException ignored) {
                    clazz = clazz.getSuperclass();
                } catch (Throwable t) {
                    // Log only severe issues, not field not found
                    if (!(t instanceof NoSuchFieldException)) {
                        logManager.debug("Failed to access field '" + fieldName + "': " + t.getMessage());
                    }
                }
            }
        }
        
        // Commands should still work via CommandMap.register(), so just debug log this
        logManager.debug("Could not access knownCommands map - commands will still work via CommandMap.register()");
        return null;
    }

    private CommandMap getCommandMap() {
        // Try CraftServer#getCommandMap()
        try {
            final java.lang.reflect.Method m = Bukkit.getServer().getClass().getMethod("getCommandMap");
            final Object map = m.invoke(Bukkit.getServer());
            return (CommandMap) map;
        } catch (Throwable ignored) {
        }

        // Fallback: reflect field from PluginManager class hierarchy
        try {
            final org.bukkit.plugin.PluginManager pm = Bukkit.getPluginManager();
            Class<?> c = pm.getClass();
            while (c != null) {
                try {
                    final java.lang.reflect.Field f = c.getDeclaredField("commandMap");
                    f.setAccessible(true);
                    return (CommandMap) f.get(pm);
                } catch (NoSuchFieldException ex) {
                    c = c.getSuperclass();
                }
            }
        } catch (Throwable t) {
            getLogger().log(Level.SEVERE, "Failed to access CommandMap", t);
        }
        return null;
    }

    private PluginCommand createPluginCommand(final String name) {
        try {
            final java.lang.reflect.Constructor<PluginCommand> ctor = PluginCommand.class.getDeclaredConstructor(String.class, org.bukkit.plugin.Plugin.class);
            ctor.setAccessible(true);
            return ctor.newInstance(name, this);
        } catch (Throwable t) {
            getLogger().log(Level.SEVERE, "Failed to construct PluginCommand for '" + name + "'", t);
            return null;
        }
    }

    @Override
    public boolean registerSubCommand(@NotNull final String command, @NotNull final SubCommand subCommand) {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(subCommand, "subCommand");

        final String commandLower = command.toLowerCase();
        
        // Debug logging
        getLogger().info("Attempting to register subcommand '" + subCommand.getName() + "' to parent command '" + commandLower + "'");
        getLogger().info("Available commands: " + commands.keySet());
        getLogger().info("Command key mappings: " + commandKeyMap);
        
        // Try direct lookup first, then check if it's an original key that maps to a different name
        AbstractCommand<DuelsPlugin> result = commands.get(commandLower);
        if (result == null) {
            final String mappedName = commandKeyMap.get(commandLower);
            getLogger().info("Direct lookup failed, trying mapped name: " + mappedName);
            if (mappedName != null) {
                result = commands.get(mappedName);
            }
        }

        if (result == null) {
            getLogger().warning("Could not find parent command '" + commandLower + "' for subcommand '" + subCommand.getName() + "'");
            return false;
        }
        
        if (result.isChild(subCommand.getName().toLowerCase())) {
            getLogger().warning("Subcommand '" + subCommand.getName() + "' is already registered to '" + commandLower + "'");
            return false;
        }

        result.child(new AbstractCommand<>(this, subCommand) {
            @Override
            protected void execute(final CommandSender sender, final String label, final String[] args) {
                subCommand.execute(sender, label, args);
            }
        });
        
        getLogger().info("Successfully registered subcommand '" + subCommand.getName() + "' to '" + commandLower + "'");
        return true;
    }

    @Override
    public void registerListener(@NotNull final Listener listener) {
        sendMessage("&eRegistering post listeners...");
        long start = System.currentTimeMillis();

        Objects.requireNonNull(listener, "listener");
        registeredListeners.add(listener);
        Bukkit.getPluginManager().registerEvents(listener, this);

        sendMessage("&dSuccessfully registered listeners after plugin startup in [" + CC.getTimeDifferenceAndColor(start, System.currentTimeMillis()) + ChatColor.WHITE + "]");
    }

    @Override
    public boolean reload() {
        if (!(unload() && load())) {
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
        boolean unloaded = false;
        try {
            loadable.handleUnload();
            unloaded = true;
            loadable.handleLoad();
            return true;
        } catch (Exception ex) {
            sendMessage("&c&lThere was an error while " + (unloaded ? "loading " : "unloading ")
                    + loadable.getClass().getSimpleName()
                    + "! If you believe this is an issue from the plugin, please contact the developer.");
            return false;
        }
    }

    @Override
    public WrappedTask doSync(@NotNull final Runnable task) {
        Objects.requireNonNull(task, "task");
        return DuelsPlugin.foliaLib.getScheduler().runLater(task, 1L);
    }

    @Override
    public WrappedTask doSyncAfter(@NotNull final Runnable task, final long delay) {
        Objects.requireNonNull(task, "task");
        return DuelsPlugin.foliaLib.getScheduler().runLater(task, delay);
    }

    @Override
    public WrappedTask doSyncRepeat(@NotNull final Runnable task, final long delay, final long period) {
        Objects.requireNonNull(task, "task");
        long safeDelay = Math.max(1, delay);
        return DuelsPlugin.foliaLib.getScheduler().runTimer(task, safeDelay, period);
    }


    @Override
    public WrappedTask doAsync(@NotNull final Runnable task) {
        Objects.requireNonNull(task, "task");
        return DuelsPlugin.foliaLib.getScheduler().runLaterAsync(task, 1L);
    }

    @Override
    public WrappedTask doAsyncAfter(@NotNull final Runnable task, final long delay) {
        Objects.requireNonNull(task, "task");
        return DuelsPlugin.foliaLib.getScheduler().runLaterAsync(task, delay);
    }

    @Override
    public WrappedTask doAsyncRepeat(@NotNull final Runnable task, final long delay, final long period) {
        Objects.requireNonNull(task, "task");
        return DuelsPlugin.foliaLib.getScheduler().runTimerAsync(task, delay, period);
    }

    @Override
    public void cancelTask(@NotNull final WrappedTask task) {
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
        return loadables.stream().filter(loadable -> loadable.getClass().getSimpleName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<String> getReloadables() {
        return loadables.stream()
                .filter(loadable -> loadable instanceof Reloadable)
                .map(loadable -> loadable.getClass().getSimpleName())
                .collect(Collectors.toList());
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
        return ChatColor.translateAlternateColorCodes('&', "&b&lDuels Optimised &7» ");
    }

    public static void sendMessage(String message) {
        Bukkit.getConsoleSender().sendMessage(getPrefix() + CC.translate(message));
    }

    private void sendBanner(){
        String[] banner = {
                "╔═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗",
                "║  ██████╗ ██╗   ██╗███████╗██╗     ███████╗     ██████╗ ██████╗ ████████╗██╗███╗   ███╗██╗███████╗███████╗██████╗  ║",
                "║  ██╔══██╗██║   ██║██╔════╝██║     ██╔════╝    ██╔═══██╗██╔══██╗╚══██╔══╝██║████╗ ████║██║██╔════╝██╔════╝██╔══██╗ ║",
                "║  ██║  ██║██║   ██║█████╗  ██║     ███████╗    ██║   ██║██████╔╝   ██║   ██║██╔████╔██║██║███████╗█████╗  ██║  ██║ ║",
                "║  ██║  ██║██║   ██║██╔══╝  ██║     ╚════██║    ██║   ██║██╔═══╝    ██║   ██║██║╚██╔╝██║██║╚════██║██╔══╝  ██║  ██║ ║",
                "║  ██████╔╝╚██████╔╝███████╗███████╗███████║    ╚██████╔╝██║        ██║   ██║██║ ╚═╝ ██║██║███████║███████╗██████╔╝ ║",
                "║  ╚═════╝  ╚═════╝ ╚══════╝╚══════╝╚══════╝     ╚═════╝ ╚═╝        ╚═╝   ╚═╝╚═╝     ╚═╝╚═╝╚══════╝╚══════╝╚═════╝  ║",
                "╚═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════╝"
        };

        for (String lines : banner){
            Bukkit.getConsoleSender().sendMessage(CC.translate("&a"+ lines));
        }

    }

    private void loadLogManager(){
        long start = System.currentTimeMillis();

        sendMessage("&eStarting to load log manager");
        try {
            logManager = new LogManager(this);
        } catch (IOException ex) {
            sendMessage("&c&lCould not load LogManager. Please contact the developer.");

            LOGGER.log(Level.SEVERE, "Could not load LogManager. Please contact the developer.", ex);
            // Manually print the stacktrace since Log#error only prints errors to non-plugin log sources.

            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Log.addSource(logManager);
        logManager.debug("onEnable start -> " + System.currentTimeMillis() + "\n");

        try {
            Class.forName("org.spigotmc.SpigotConfig");
        } catch (ClassNotFoundException ex) {
            sendMessage("&c&l================= *** DUELS LOAD FAILURE *** =================");
            sendMessage("&c&lDuels requires a spigot server to run, but this server was not running on spigot!");
            sendMessage("&c&lTo run your server on spigot, follow this guide: " + SPIGOT_INSTALLATION_URL);
            sendMessage("&c&lSpigot is compatible with CraftBukkit/Bukkit plugins.");
            sendMessage("&c&l================= *** DUELS LOAD FAILURE *** =================");
            getServer().getPluginManager().disablePlugin(this);
        }

        sendMessage("&dSuccessfully loaded Log Manager in &f[" + CC.getTimeDifferenceAndColor(start, System.currentTimeMillis()) + "&f]");
    }

    private void initLoadables() {
        long start = System.currentTimeMillis();
        sendMessage("&eStarting to load loadables");

        loadAndTrack("config", () -> loadables.add(configuration = new Config(this)));
        loadAndTrack("commands config", () -> loadables.add(commandsConfig = new CommandsConfig(this)));
        loadAndTrack("lang", () -> loadables.add(lang = new Lang(this)));
        loadAndTrack("user manager", () -> loadables.add(userManager = new UserManagerImpl(this)));
        loadAndTrack("gui listener", () -> loadables.add(guiListener = new GuiListener<>(this)));
        loadAndTrack("party manager", () -> loadables.add(partyManager = new PartyManagerImpl(this)));
        loadAndTrack("kit manager", () -> loadables.add(kitManager = new KitManagerImpl(this)));
        loadAndTrack("arena manager", () -> loadables.add(arenaManager = new ArenaManagerImpl(this)));
        loadAndTrack("settings manager", () -> loadables.add(settingManager = new SettingsManager(this)));
        loadAndTrack("player manager", () -> loadables.add(playerManager = new PlayerInfoManager(this)));
        loadAndTrack("spectate manager", () -> loadables.add(spectateManager = new SpectateManagerImpl(this)));
        loadAndTrack("betting manager", () -> loadables.add(bettingManager = new BettingManager(this)));
        loadAndTrack("inventory manager", () -> loadables.add(inventoryManager = new InventoryManager(this)));
        loadAndTrack("duel manager", () -> loadables.add(duelManager = new DuelManager(this)));
        loadAndTrack("queue manager", () -> loadables.add(queueManager = new QueueManager(this)));
        loadAndTrack("queue signs", () -> loadables.add(queueSignManager = new QueueSignManagerImpl(this)));
        loadAndTrack("request manager", () -> loadables.add(requestManager = new RequestManager(this)));
        loadAndTrack("hook manager", () -> hookManager = new HookManager(this));
        loadAndTrack("validator manager", () -> loadables.add(validatorManager = new ValidatorManager(this)));
        loadAndTrack("teleport manager", () -> loadables.add(teleport = new Teleport(this)));

        if (!load()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        sendMessage("&dSuccessfully loaded all loadables in &f[" + CC.getTimeDifferenceAndColor(start, System.currentTimeMillis()) + "&f]");
    }

    private void loadAndTrack(String name, Runnable task) {
        long start = System.currentTimeMillis();
        try {
            task.run();
            sendMessage("&2Successfully loaded " + name + " in &f[" + CC.getTimeDifferenceAndColor(start, System.currentTimeMillis()) + "&f]");
        } catch (Exception e) {
            sendMessage("&cFailed to load " + name + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadExtensions() {
        long start = System.currentTimeMillis();
        sendMessage("&eLoading extensions...");
        try {
            loadables.add(extensionManager = new ExtensionManager(this));
            extensionManager.handleLoad();
            sendMessage("&dSuccessfully loaded extensions in &f[" + CC.getTimeDifferenceAndColor(start, System.currentTimeMillis()) + "&f]");
        } catch (Exception e) {
            sendMessage("&cFailed to load extensions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPreListeners(){
        long start = System.currentTimeMillis();

        sendMessage("&eStarting to load pre-listeners");
        new KitItemListener(this);
        new DamageListener(this);
        new ExplosionOwnershipListener(this);
        new TeamDamageListener(this);
        new PotionListener(this);
        new TeleportListener(this);
        new ProjectileHitListener(this);
        new EnderpearlListener(this);
        new KitOptionsListener(this);
        new LingerPotionListener(this);
        new com.meteordevelopments.duels.kit.edit.KitEditManager(this);
        registerListener(new com.meteordevelopments.duels.kit.edit.KitEditListener(this));

        sendMessage("&dSuccessfully loaded pre-listeners in &f[" + CC.getTimeDifferenceAndColor(start, System.currentTimeMillis()) + "&f]");
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
}
