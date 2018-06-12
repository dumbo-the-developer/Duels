/*
 * This file is part of Duels, licensed under the MIT License.
 *
 * Copyright (c) Realized
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.realized.duels;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import lombok.Getter;
import me.realized.duels.api.Duels;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.betting.BettingManager;
import me.realized.duels.command.commands.SpectateCommand;
import me.realized.duels.command.commands.duel.DuelCommand;
import me.realized.duels.command.commands.duels.DuelsCommand;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.data.UserManager;
import me.realized.duels.duel.DuelManager;
import me.realized.duels.extra.KitItemListener;
import me.realized.duels.hooks.HookManager;
import me.realized.duels.kit.KitManager;
import me.realized.duels.logging.LogManager;
import me.realized.duels.player.PlayerInfoManager;
import me.realized.duels.request.RequestManager;
import me.realized.duels.setting.SettingManager;
import me.realized.duels.shaded.bstats.Metrics;
import me.realized.duels.spectate.SpectateManager;
import me.realized.duels.teleport.Teleport;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.Log.LogSource;
import me.realized.duels.util.Reloadable;
import me.realized.duels.util.gui.GuiListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.update.spiget.SpigetUpdate;
import org.inventivetalent.update.spiget.UpdateCallback;
import org.inventivetalent.update.spiget.comparator.VersionComparator;

public class DuelsPlugin extends JavaPlugin implements Duels, LogSource {

    private static final int RESOURCE_ID = 20171;

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
    private SettingManager settingManager;
    @Getter
    private PlayerInfoManager playerManager;
    @Getter
    private SpectateManager spectateManager;
    @Getter
    private BettingManager bettingManager;
    @Getter
    private DuelManager duelManager;
    @Getter
    private RequestManager requestManager;
    @Getter
    private HookManager hookManager;
    @Getter
    private Teleport teleport;

    @Override
    public void onEnable() {
        Log.addSource(this);
        loadables.add(logManager = new LogManager(this));
        Log.addSource(logManager);
        loadables.add(configuration = new Config(this));
        loadables.add(lang = new Lang(this));
        loadables.add(userManager = new UserManager(this));
        loadables.add(guiListener = new GuiListener<>(this));
        loadables.add(arenaManager = new ArenaManager(this));
        loadables.add(kitManager = new KitManager(this));
        loadables.add(settingManager = new SettingManager(this));
        loadables.add(playerManager = new PlayerInfoManager(this));
        loadables.add(spectateManager = new SpectateManager(this));
        loadables.add(bettingManager = new BettingManager(this));
        loadables.add(duelManager = new DuelManager(this));
        loadables.add(requestManager = new RequestManager(this));
        loadables.add(hookManager = new HookManager(this));
        loadables.add(teleport = new Teleport(this));
        new KitItemListener(this);

        if (!load()) {
            getPluginLoader().disablePlugin(this);
            return;
        }

        new DuelCommand(this).register();
        new DuelsCommand(this).register();
        new SpectateCommand(this).register();

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

    public void doAsyncRepeat(final Runnable runnable, final long delay, final long period) {
        getServer().getScheduler().runTaskTimerAsynchronously(this, runnable, delay, period);
    }

    public void cancelTask(final int taskId) {
        getServer().getScheduler().cancelTask(taskId);
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
