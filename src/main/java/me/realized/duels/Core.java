package me.realized.duels;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.commands.BaseCommand;
import me.realized.duels.commands.admin.DuelsCommand;
import me.realized.duels.commands.duel.DuelCommand;
import me.realized.duels.commands.other.SpectateCommand;
import me.realized.duels.commands.other.StatsCommand;
import me.realized.duels.commands.other.ToggleCommand;
import me.realized.duels.configuration.ConfigManager;
import me.realized.duels.configuration.ConfigType;
import me.realized.duels.configuration.MainConfig;
import me.realized.duels.configuration.MessagesConfig;
import me.realized.duels.data.DataManager;
import me.realized.duels.dueling.DuelManager;
import me.realized.duels.dueling.RequestManager;
import me.realized.duels.dueling.SpectatorManager;
import me.realized.duels.hooks.*;
import me.realized.duels.kits.KitManager;
import me.realized.duels.utilities.ICanHandleReload;
import me.realized.duels.utilities.ReloadType;
import me.realized.duels.utilities.gui.GUIManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Core extends JavaPlugin {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private ConfigManager configManager;
    private HookManager hookManager;
    private RequestManager requestManager;
    private GUIManager guiManager;
    private DataManager dataManager;
    private ArenaManager arenaManager;
    private SpectatorManager spectatorManager;
    private KitManager kitManager;
    private DuelManager duelManager;

    private final List<ICanHandleReload> reloadables = new ArrayList<>();

    @Override
    public void onEnable() {
        configManager = new ConfigManager();
        configManager.register(ConfigType.MAIN, new MainConfig(this));
        configManager.register(ConfigType.MESSAGES, new MessagesConfig(this));
        reloadables.add(configManager);

        hookManager = new HookManager();
        hookManager.register("Factions", new FactionsHook(this));
        hookManager.register("WorldGuard", new WorldGuardHook(this));
        hookManager.register("Essentials", new EssentialsHook(this));
        hookManager.register("mcMMO", new McMMOHook(this));

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

        List<BaseCommand> commands = Arrays.asList(new StatsCommand(), new DuelsCommand(), new ToggleCommand(), new DuelCommand(), new SpectateCommand());

        for (BaseCommand command : commands) {
            getCommand(command.getName()).setExecutor(command);
        }
    }

    @Override
    public void onDisable() {
        dataManager.save();
        arenaManager.save();
        kitManager.save();
        duelManager.handleDisable();
    }

    public void reload(ReloadType type) {
        for (ICanHandleReload reloadable : reloadables) {
            reloadable.handleReload(type);
        }
    }

    public void info(String msg) {
        getLogger().info(msg);
    }

    public void warn(String msg) {
        getLogger().warning(msg);
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

    public static Core getInstance() {
        return getPlugin(Core.class);
    }
}