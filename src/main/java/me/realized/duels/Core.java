package me.realized.duels;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
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
import me.realized.duels.kits.KitManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class Core extends JavaPlugin {

    private static Core instance = null;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private WorldGuardPlugin worldguard = null;
    private Logger logger;
    private Config config;
    private DuelManager duelManager;
    private DataManager dataManager;
    private ArenaManager arenaManager;
    private RequestManager requestManager;
    private KitManager kitManager;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            worldguard = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
        }

        instance = this;
        logger = Bukkit.getLogger();
        config = new Config(this);

        dataManager = new DataManager(this);
        dataManager.load();

        arenaManager = new ArenaManager(this);
        arenaManager.load();

        kitManager = new KitManager(this);
        kitManager.load();

        requestManager = new RequestManager();
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
        logger.info("[Duels] " + msg);
    }

    public void warn(String msg) {
        logger.warning("[Duels] " + msg);
    }

    public WorldGuardPlugin getWorldGuard() {
        return worldguard;
    }

    public Gson getGson() {
        return gson;
    }

    public Config getConfiguration() {
        return config;
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
        return instance;
    }
}