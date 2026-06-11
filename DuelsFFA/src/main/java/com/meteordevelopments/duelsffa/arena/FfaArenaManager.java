package com.meteordevelopments.duelsffa.arena;

import com.meteordevelopments.duels.api.Duels;
import com.meteordevelopments.duels.api.folialib.task.WrappedTask;
import com.meteordevelopments.duelsffa.FfaExtension;
import com.meteordevelopments.duelsffa.config.FfaConfig;
import com.meteordevelopments.duelsffa.selection.Selection;
import com.meteordevelopments.duelsffa.util.Callback;
import com.meteordevelopments.duelsffa.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FfaArenaManager {

    private final FfaExtension extension;
    private final Duels api;
    private final FfaConfig config;
    private final File file;
    private final YamlConfiguration data;

    private final Map<String, FfaArena> arenas = new HashMap<>();
    private final Map<String, WrappedTask> regenTasks = new HashMap<>();
    private final RegenZoneManager zoneManager;

    public FfaArenaManager(final FfaExtension extension) {
        this.extension = extension;
        this.api = extension.getApi();
        this.config = extension.getConfiguration();
        this.file = new File(extension.getDataFolder(), "ffa.yml");

        if (!file.exists()) {
            extension.saveResource("ffa.yml");
        }

        this.data = YamlConfiguration.loadConfiguration(file);
        loadArenas();
        this.zoneManager = new RegenZoneManager(extension, this);
        scheduleAll();
    }

    public FfaArena getArena(final String name) {
        if (name == null) return null;
        return arenas.get(normalize(name));
    }

    public Collection<FfaArena> getArenas() {
        return arenas.values();
    }

    public RegenZoneManager getZoneManager() {
        return zoneManager;
    }

    public boolean createArena(final String name, final String kitName, final Selection selection, final Location spawn) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        if (getArena(name) != null) {
            return false;
        }
        if (selection == null || !selection.isSelected()) {
            return false;
        }
        FfaArena arena = new FfaArena(name, kitName, true, config.getDefaultRegenIntervalMinutes());
        if (spawn != null) {
            arena.addSpawn(spawn);
        }
        RegenZone zone = zoneManager.create(arena, selection);
        if (zone == null) {
            return false;
        }
        arena.setZone(zone);
        arenas.put(normalize(name), arena);
        save();
        scheduleRegen(arena);
        return true;
    }

    public boolean enableArena(final FfaArena arena) {
        if (arena == null) return false;
        arena.setEnabled(true);
        save();
        scheduleRegen(arena);
        return true;
    }

    public boolean disableArena(final FfaArena arena) {
        if (arena == null) return false;
        arena.setEnabled(false);
        cancelRegen(arena);
        save();
        return true;
    }

    public boolean forceRegen(final FfaArena arena, final Callback onDone) {
        if (arena == null) return false;
        if (arena.isResetting()) return false;
        RegenZone zone = arena.getZone();
        if (zone == null || zone.isResetting()) return false;

        arena.setResetting(true);
        extension.getPlayerManager().getPlayersInArena(arena).forEach(player ->
                extension.getPlayerManager().leave(player, LeaveReason.REGEN));

        zone.reset(() -> {
            arena.setResetting(false);
            if (onDone != null) {
                onDone.call();
            }
        });
        return true;
    }

    public void shutdown() {
        regenTasks.values().forEach(api::cancelTask);
        regenTasks.clear();
    }

    private void loadArenas() {
        arenas.clear();
        ConfigurationSection section = data.getConfigurationSection("arenas");
        if (section == null) {
            return;
        }

        for (String arenaName : section.getKeys(false)) {
            String path = "arenas." + arenaName;
            String kit = data.getString(path + ".kit", "none");
            boolean enabled = data.getBoolean(path + ".enabled", true);
            int interval = data.getInt(path + ".regen-interval-minutes", config.getDefaultRegenIntervalMinutes());

            FfaArena arena = new FfaArena(arenaName, kit, enabled, interval);
            List<String> spawns = data.getStringList(path + ".spawns");
            for (Location location : LocationUtil.deserializeAll(spawns)) {
                arena.addSpawn(location);
            }

            arenas.put(normalize(arenaName), arena);
        }
    }

    public void save() {
        data.set("arenas", null);
        ConfigurationSection section = data.createSection("arenas");
        for (FfaArena arena : arenas.values()) {
            ConfigurationSection arenaSection = section.createSection(arena.getName());
            arenaSection.set("enabled", arena.isEnabled());
            arenaSection.set("kit", arena.getKitName());
            arenaSection.set("regen-interval-minutes", arena.getRegenIntervalMinutes());
            arenaSection.set("spawns", LocationUtil.serializeAll(arena.getSpawns()));
        }
        try {
            data.save(file);
        } catch (IOException ignored) {
        }
    }

    private void scheduleAll() {
        for (FfaArena arena : arenas.values()) {
            scheduleRegen(arena);
        }
    }

    private void scheduleRegen(final FfaArena arena) {
        cancelRegen(arena);
        if (!arena.isEnabled()) {
            return;
        }
        int interval = arena.getRegenIntervalMinutes();
        if (interval <= 0) {
            return;
        }
        long ticks = interval * 60L * 20L;
        WrappedTask task = api.doSyncRepeat(() -> forceRegen(arena, null), ticks, ticks);
        regenTasks.put(normalize(arena.getName()), task);
    }

    private void cancelRegen(final FfaArena arena) {
        WrappedTask task = regenTasks.remove(normalize(arena.getName()));
        if (task != null) {
            api.cancelTask(task);
        }
    }

    private String normalize(final String name) {
        return name.toLowerCase();
    }
}
