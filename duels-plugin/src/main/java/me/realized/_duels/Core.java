package me.realized._duels;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import me.realized._duels.arena.ArenaManager;
import me.realized._duels.commands.BaseCommand;
import me.realized._duels.commands.admin.DuelsCommand;
import me.realized._duels.commands.duel.DuelCommand;
import me.realized._duels.commands.other.SpectateCommand;
import me.realized._duels.configuration.ConfigManager;
import me.realized._duels.configuration.ConfigType;
import me.realized._duels.configuration.MainConfig;
import me.realized._duels.configuration.MessagesConfig;
import me.realized._duels.data.DataManager;
import me.realized._duels.data.PlayerManager;
import me.realized._duels.dueling.DuelManager;
import me.realized._duels.dueling.RequestManager;
import me.realized._duels.dueling.SpectatorManager;
import me.realized._duels.extension.ExtensionManager;
import me.realized._duels.hooks.CombatTagPlusHook;
import me.realized._duels.hooks.EssentialsHook;
import me.realized._duels.hooks.FactionsUUIDHook;
import me.realized._duels.hooks.HookManager;
import me.realized._duels.hooks.MVdWPlaceholderHook;
import me.realized._duels.hooks.WorldGuardHook;
import me.realized._duels.hooks.mcMMOHook;
import me.realized._duels.kits.KitManager;
import me.realized._duels.logging.LogManager;
import me.realized._duels.utilities.Reloadable;
import me.realized._duels.utilities.gui.GUIManager;
import me.realized._duels.utilities.location.Teleport;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final List<Reloadable> reloadables = new ArrayList<>();
    private LogManager logManager;
    private ConfigManager configManager;
    private Teleport teleport;
    private PlayerManager playerManager;
    private HookManager hookManager;
    private RequestManager requestManager;
    private GUIManager guiManager;
    private DataManager dataManager;
    private ArenaManager arenaManager;
    private SpectatorManager spectatorManager;
    private KitManager kitManager;
    private DuelManager duelManager;
    private ExtensionManager extensionManager;

    public static Core getInstance() {
        return getPlugin(Core.class);
    }

    @Override
    public void onEnable() {
        logManager = new LogManager(this);
        logManager.init();

        configManager = new ConfigManager();
        configManager.register(ConfigType.MAIN, new MainConfig(this));
        configManager.register(ConfigType.MESSAGES, new MessagesConfig(this));
        reloadables.add(configManager);

        teleport = new Teleport(this);

        playerManager = new PlayerManager();
        reloadables.add(playerManager);

        hookManager = new HookManager();
        hookManager.register("CombatTagPlus", new CombatTagPlusHook(this));
        hookManager.register("MVdWPlaceholderAPI", new MVdWPlaceholderHook(this));
        hookManager.register("Factions", new FactionsUUIDHook(this));
        hookManager.register("WorldGuard", new WorldGuardHook(this));
        hookManager.register("Essentials", new EssentialsHook(this));
        hookManager.register("mcMMO", new mcMMOHook(this));

        requestManager = new RequestManager();
        reloadables.add(requestManager);

        guiManager = new GUIManager(this);

        dataManager = new DataManager(this);
        dataManager.load();
        reloadables.add(dataManager);

        arenaManager = new ArenaManager(this);
        arenaManager.load();
        reloadables.add(arenaManager);

        spectatorManager = new SpectatorManager(this);

        kitManager = new KitManager(this);
        kitManager.load();
        reloadables.add(kitManager);

        duelManager = new DuelManager(this);

        List<BaseCommand> commands = Arrays.asList(new DuelsCommand(), new DuelCommand(), new SpectateCommand());

        for (BaseCommand command : commands) {
            getCommand(command.getName()).setExecutor(command);
        }

        extensionManager = new ExtensionManager(this);
        extensionManager.load();
        reloadables.add(extensionManager);
    }

    @Override
    public void onDisable() {
        logManager.handleDisable();
        dataManager.save();
        arenaManager.save();
        kitManager.save();
        duelManager.handleDisable();
        extensionManager.handleDisable();
    }

    public void reload(Reloadable.ReloadType type) {
        for (Reloadable reloadable : reloadables) {
            reloadable.handleReload(type);
        }
    }

    public void info(String msg) {
        getLogger().info(msg);
        logToFile(this, msg, Level.INFO);
    }

    public void warn(String msg) {
        getLogger().warning(msg);
        logToFile(this, msg, Level.WARNING);
    }

    public void logToFile(Object self, String msg, Level level) {
        if (level == Level.INFO) {
            logManager.getLogger().info("(" + self.getClass().getSimpleName() + ") " + msg);
        } else {
            logManager.getLogger().warning("(" + self.getClass().getSimpleName() + ") " + msg);
        }
    }

    public Gson getGson() {
        return gson;
    }

    public MainConfig getConfiguration() {
        return (MainConfig) configManager.getConfigByType(ConfigType.MAIN);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public GUIManager getGUIManager() {
        return guiManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public SpectatorManager getSpectatorManager() {
        return spectatorManager;
    }

    public RequestManager getRequestManager() {
        return requestManager;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public DuelManager getDuelManager() {
        return duelManager;
    }

    public Teleport getTeleport() {
        return teleport;
    }

    // Allow a custom manager to listen to /duels reload
    public boolean registerReloadable(Reloadable reloadable) {
        if (!reloadables.contains(reloadable)) {
            reloadables.add(reloadable);
            return true;
        }

        return false;
    }
}