package me.realized.duels;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.commands.BaseCommand;
import me.realized.duels.commands.admin.DuelsCommand;
import me.realized.duels.commands.duel.DuelCommand;
import me.realized.duels.commands.other.StatsCommand;
import me.realized.duels.commands.other.ToggleCommand;
import me.realized.duels.configuration.Config;
import me.realized.duels.data.DataManager;
import me.realized.duels.dueling.DuelManager;
import me.realized.duels.dueling.RequestManager;
import me.realized.duels.gui.GUIManager;
import me.realized.duels.hooks.EssentialsHook;
import me.realized.duels.hooks.HookManager;
import me.realized.duels.hooks.WorldGuardHook;
import me.realized.duels.kits.KitManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class Core extends JavaPlugin {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Config config;
    private HookManager hookManager;
    private GUIManager guiManager;
    private DuelManager duelManager;
    private DataManager dataManager;
    private ArenaManager arenaManager;
    private RequestManager requestManager;
    private KitManager kitManager;

    @Override
    public void onEnable() {
        config = new Config(this);

        hookManager = new HookManager();
        hookManager.register("WorldGuard", new WorldGuardHook(this));
        hookManager.register("Essentials", new EssentialsHook(this));

        requestManager = new RequestManager();
        guiManager = new GUIManager(this);

        dataManager = new DataManager(this);
        dataManager.load();

        arenaManager = new ArenaManager(this);
        arenaManager.load();

        kitManager = new KitManager(this);
        kitManager.load();

        duelManager = new DuelManager(this);

        registerCommands();
    }

    @Override
    public void onDisable() {
        dataManager.save();
        arenaManager.save();
        kitManager.save();
    }

    private void registerCommands() {
        List<BaseCommand> commands = Arrays.asList(new StatsCommand(), new DuelsCommand(), new ToggleCommand(), new DuelCommand());

        for (BaseCommand command : commands) {
            command.register();
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

    public Config getConfiguration() {
        return config;
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