package me.realized.duels;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.Getter;
import me.realized.duels.api.Duels;
import me.realized.duels.api.command.SubCommand;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.betting.BettingManager;
import me.realized.duels.command.commands.SpectateCommand;
import me.realized.duels.command.commands.duel.DuelCommand;
import me.realized.duels.command.commands.duels.DuelsCommand;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.data.UserManager;
import me.realized.duels.duel.DuelManager;
import me.realized.duels.extension.ExtensionManager;
import me.realized.duels.extra.DamageListener;
import me.realized.duels.extra.KitItemListener;
import me.realized.duels.extra.Permissions;
import me.realized.duels.extra.PotionListener;
import me.realized.duels.extra.SoupListener;
import me.realized.duels.extra.Teleport;
import me.realized.duels.extra.TeleportListener;
import me.realized.duels.hooks.HookManager;
import me.realized.duels.inventories.InventoryManager;
import me.realized.duels.kit.KitManager;
import me.realized.duels.logging.LogManager;
import me.realized.duels.player.PlayerInfoManager;
import me.realized.duels.queue.QueueManager;
import me.realized.duels.request.RequestManager;
import me.realized.duels.setting.SettingsManager;
import me.realized.duels.shaded.bstats.Metrics;
import me.realized.duels.spectate.SpectateManager;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.Log.LogSource;
import me.realized.duels.util.Reloadable;
import me.realized.duels.util.ServerUtil;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.command.AbstractCommand;
import me.realized.duels.util.compat.Players;
import me.realized.duels.util.gui.GuiListener;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.update.spiget.SpigetUpdate;
import org.inventivetalent.update.spiget.UpdateCallback;
import org.inventivetalent.update.spiget.comparator.VersionComparator;

public class DuelsPlugin extends JavaPlugin implements Duels, LogSource {

    private static final int RESOURCE_ID = 20171;
    private static final String SPIGOT_INSTALLATION_URL = "https://www.spigotmc.org/wiki/spigot-installation/";
    private static final String ADMIN_UPDATE_MESSAGE = "&9[Duels] &bThere is an update available for Duels. Download at &f%s";

    @Getter
    private static DuelsPlugin instance;

    @Getter
    private final Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).setPrettyPrinting().create();
    private final List<Loadable> loadables = new ArrayList<>();
    private int lastLoad;

    @Getter
    private LogManager logManager;
    @Getter
    private Config configuration;
    @Getter
    private Lang lang;
    @Getter
    private UserManager userManager;
    @Getter
    private GuiListener<DuelsPlugin> guiListener;
    @Getter
    private ArenaManager arenaManager;
    @Getter
    private KitManager kitManager;
    @Getter
    private SettingsManager settingManager;
    @Getter
    private PlayerInfoManager playerManager;
    @Getter
    private SpectateManager spectateManager;
    @Getter
    private BettingManager bettingManager;
    @Getter
    private InventoryManager inventoryManager;
    @Getter
    private DuelManager duelManager;
    @Getter
    private QueueManager queueManager;
    @Getter
    private RequestManager requestManager;
    @Getter
    private HookManager hookManager;
    @Getter
    private Teleport teleport;
    @Getter
    private ExtensionManager extensionManager;
    private final Map<String, AbstractCommand<DuelsPlugin>> commands = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        Log.addSource(this);
        loadables.add(logManager = new LogManager(this));
        Log.addSource(logManager);

        if (!ServerUtil.isUsingSpigot()) {
            Log.error("================= *** DUELS LOAD FAILURE *** =================");
            Log.error("Duels requires a spigot server to run, but this server was not running on spigot!");
            Log.error("To run your server on spigot, follow this guide: " + SPIGOT_INSTALLATION_URL);
            Log.error("Spigot is compatible with CraftBukkit/Bukkit plugins.");
            Log.error("================= *** DUELS LOAD FAILURE *** =================");
            getPluginLoader().disablePlugin(this);
            return;
        }

        loadables.add(configuration = new Config(this));
        loadables.add(lang = new Lang(this));
        loadables.add(userManager = new UserManager(this));
        loadables.add(guiListener = new GuiListener<>(this));
        loadables.add(arenaManager = new ArenaManager(this));
        loadables.add(kitManager = new KitManager(this));
        loadables.add(settingManager = new SettingsManager(this));
        loadables.add(playerManager = new PlayerInfoManager(this));
        loadables.add(spectateManager = new SpectateManager(this));
        loadables.add(bettingManager = new BettingManager(this));
        loadables.add(inventoryManager = new InventoryManager(this));
        loadables.add(duelManager = new DuelManager(this));
        loadables.add(queueManager = new QueueManager(this));
        loadables.add(requestManager = new RequestManager(this));
        hookManager = new HookManager(this);
        loadables.add(teleport = new Teleport(this));
        loadables.add(extensionManager = new ExtensionManager(this));

        registerCommands(
            new DuelCommand(this),
            new SpectateCommand(this),
            new DuelsCommand(this)
        );

        if (!load()) {
            getPluginLoader().disablePlugin(this);
            return;
        }

        new KitItemListener(this);
        new DamageListener(this);
        new PotionListener(this);
        new TeleportListener(this);
        new SoupListener(this);

        new Metrics(this);

        if (!configuration.isCheckForUpdates()) {
            return;
        }

        final SpigetUpdate updateChecker = new SpigetUpdate(this, RESOURCE_ID);
        updateChecker.setVersionComparator(VersionComparator.SEM_VER_SNAPSHOT);
        updateChecker.checkForUpdate(new UpdateCallback() {
            @Override
            public void updateAvailable(final String newVersion, final String downloadUrl, final boolean hasDirectDownload) {
                Log.info("===============================================");
                Log.info("An update for " + getName() + " is available!");
                Log.info("Download " + getName() + " v" + newVersion + " here:");
                Log.info(downloadUrl);
                Log.info("===============================================");
                doSync(() -> Players.getOnlinePlayers().stream()
                    .filter(player -> player.isOp() || player.hasPermission(Permissions.ADMIN))
                    .forEach(player -> player.sendMessage(StringUtil.color(String.format(ADMIN_UPDATE_MESSAGE, downloadUrl)))));
            }

            @Override
            public void upToDate() {
                Log.info("No updates were available. You are on the latest version!");
            }
        });
    }

    @Override
    public void onDisable() {
        unload();
        Log.clearSources();
        instance = null;
    }

    /**
     * @return true if load was successful, otherwise false
     */
    private boolean load() {
        for (final Loadable loadable : loadables) {
            try {
                loadable.handleLoad();
                lastLoad = loadables.indexOf(loadable);
            } catch (Exception ex) {
                Log.error("There was an error while loading " + loadable.getClass().getSimpleName()
                    + "! If you believe this is an issue from the plugin, please contact the developer.");
                Log.error("Cause of error: " + ex.getMessage());
                ex.printStackTrace();
                return false;
            }
        }

        return true;
    }

    /**
     * @return true if unload was successful, otherwise false
     */
    private boolean unload() {
        for (final Loadable loadable : Lists.reverse(loadables)) {
            try {
                if (loadables.indexOf(loadable) > lastLoad) {
                    continue;
                }

                loadable.handleUnload();
            } catch (Exception ex) {
                Log.error("There was an error while unloading " + loadable.getClass().getSimpleName()
                    + "! If you believe this is an issue from the plugin, please contact the developer.");
                Log.error("Cause of error: " + ex.getMessage());
                ex.printStackTrace();
                return false;
            }
        }

        return true;
    }

    @SafeVarargs
    private final void registerCommands(final AbstractCommand<DuelsPlugin>... commands) {
        for (final AbstractCommand<DuelsPlugin> command : commands) {
            this.commands.put(command.getName().toLowerCase(), command);
            command.register();
        }
    }

    @Override
    public boolean registerSubCommand(@Nonnull final String command, @Nonnull final SubCommand subCommand) {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(subCommand, "subCommand");

        final AbstractCommand<DuelsPlugin> result = commands.get(command.toLowerCase());

        if (result == null || result.isChild(subCommand.getName())) {
            return false;
        }

        result.child(new AbstractCommand<DuelsPlugin>(this, subCommand) {
            @Override
            protected void execute(final CommandSender sender, final String label, final String[] args) {
                subCommand.execute(sender, label, args);
            }
        });
        return true;
    }

    @Override
    public boolean reload() {
        if (!(unload() && load())) {
            getPluginLoader().disablePlugin(this);
            return false;
        }

        return true;
    }

    public boolean reload(final Loadable loadable) {
        boolean unloaded = false;
        try {
            loadable.handleUnload();
            unloaded = true;
            loadable.handleLoad();
            return true;
        } catch (Exception ex) {
            Log.error("There was an error while " + (unloaded ? "loading " : "unloading ")
                + loadable.getClass().getSimpleName()
                + "! If you believe this is an issue from the plugin, please contact the developer.");
            Log.error("Cause of error: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }


    public void doSync(final Runnable runnable) {
        getServer().getScheduler().runTask(this, runnable);
    }

    public void doSyncAfter(final Runnable runnable, final long delay) {
        getServer().getScheduler().runTaskLater(this, runnable, delay);
    }

    public int doSyncRepeat(final Runnable runnable, final long delay, final long period) {
        return getServer().getScheduler().runTaskTimer(this, runnable, delay, period).getTaskId();
    }

    public void doAsync(final Runnable runnable) {
        getServer().getScheduler().runTaskAsynchronously(this, runnable);
    }

    public void doAsyncRepeat(final Runnable runnable, final long delay, final long period) {
        getServer().getScheduler().runTaskTimerAsynchronously(this, runnable, delay, period);
    }

    public void cancelTask(final int taskId) {
        getServer().getScheduler().cancelTask(taskId);
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
}
