package com.meteordevelopments.duelsffa.arena;

import com.meteordevelopments.duels.api.Duels;
import com.meteordevelopments.duelsffa.FfaExtension;
import com.meteordevelopments.duelsffa.selection.Selection;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RegenZoneManager {

    private final FfaExtension extension;
    private final Duels api;
    private final File folder;
    private final Map<String, RegenZone> zones = new HashMap<>();

    public RegenZoneManager(final FfaExtension extension, final FfaArenaManager arenaManager) {
        this.extension = extension;
        this.api = extension.getApi();
        this.folder = new File(extension.getDataFolder(), "zones");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.getName().toLowerCase().endsWith(".schem")) {
                    continue;
                }
                String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
                FfaArena arena = arenaManager.getArena(name);
                if (arena == null) {
                    continue;
                }
                try {
                    RegenZone zone = new RegenZone(extension, api, arena, file);
                    zones.put(name.toLowerCase(), zone);
                    arena.setZone(zone);
                } catch (Exception ex) {
                    api.warn("[DuelsFFA Extension] Could not load regen zone '" + name + "'.");
                }
            }
        }
    }

    public RegenZone get(final String name) {
        if (name == null) return null;
        return zones.get(name.toLowerCase());
    }

    public RegenZone create(final FfaArena arena, final Selection selection) {
        if (arena == null) return null;
        if (zones.containsKey(arena.getName().toLowerCase())) {
            return null;
        }
        RegenZone zone = new RegenZone(extension, api, arena, folder, selection);
        zones.put(arena.getName().toLowerCase(), zone);
        arena.setZone(zone);
        return zone;
    }

    public Collection<RegenZone> getZones() {
        return zones.values();
    }
}
