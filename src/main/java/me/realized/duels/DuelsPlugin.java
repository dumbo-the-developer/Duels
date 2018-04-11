package me.realized.duels;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;
import lombok.Getter;
import me.realized.duels.api.Duels;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.command.commands.duel.DuelCommand;
import me.realized.duels.command.commands.duels.DuelsCommand;
import me.realized.duels.data.UserDataManager;
import me.realized.duels.duel.DuelManager;
import me.realized.duels.kit.KitManager;
import me.realized.duels.logging.LogManager;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.Log.LogSource;
import me.realized.duels.util.Reloadable;
import org.bukkit.plugin.java.JavaPlugin;

public class DuelsPlugin extends JavaPlugin implements Duels, LogSource {

    @Getter
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final List<Loadable> loadables = new ArrayList<>();
    private int lastLoad;

    @Getter
    private LogManager logManager;
    @Getter
    private UserDataManager userManager;
    @Getter
    private KitManager kitManager;
    @Getter
    private ArenaManager arenaManager;
    @Getter
    private DuelManager duelManager;

    @Override
    public void onEnable() {
        Log.addSource(this);
        loadables.add(logManager = new LogManager(this));
        Log.addSource(logManager);
        loadables.add(userManager = new UserDataManager(this));
        loadables.add(kitManager = new KitManager(this));
        loadables.add(arenaManager = new ArenaManager(this));
        loadables.add(duelManager = new DuelManager(this));

        if (!load()) {
            getPluginLoader().disablePlugin(this);
            return;
        }

        new DuelCommand(this).register();
        new DuelsCommand(this).register();
    }

    @Override
    public void onDisable() {
        unload();
        Log.clearSources();
    }

    /**
     * @return true if load was successful, otherwise false
     */
    private boolean load() {
        for (final Loadable loadable : loadables) {
            try {
                loadable.handleLoad();
                lastLoad = loadables.indexOf(loadable);
                Log.info("Loaded " + loadable.getClass().getSimpleName() + ".");
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
                Log.info("Unloaded " + loadable.getClass().getSimpleName() + ".");
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

    @Override
    public boolean reload() {
        if (!(unload() && load())) {
            getPluginLoader().disablePlugin(this);
            return false;
        }

        return true;
    }

    public boolean reload(final Loadable loadable) {
        final String name = loadable.getClass().getSimpleName();
        boolean unloaded = false;
        try {
            loadable.handleUnload();
            unloaded = true;
            Log.info("UnLoaded " + name + ".");
            loadable.handleLoad();
            Log.info("Loaded " + name + ".");
            return true;
        } catch (Exception ex) {
            Log.error("There was an error while " + (unloaded ? "loading " : "unloading ") + name
                + "! If you believe this is an issue from the plugin, please contact the developer.");
            Log.error("Cause of error: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public Optional<Loadable> find(final String name) {
        return loadables.stream().filter(loadable -> loadable.getClass().getSimpleName().equalsIgnoreCase(name)).findFirst();
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
