package com.meteordevelopments.duelsffa.arena;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FfaArena {

    private final String name;
    private String kitName;
    private boolean enabled;
    private int regenIntervalMinutes;
    private final List<Location> spawns = new ArrayList<>();
    private RegenZone zone;
    private boolean resetting;

    private static final Random RANDOM = new Random();

    public FfaArena(final String name, final String kitName, final boolean enabled, final int regenIntervalMinutes) {
        this.name = name;
        this.kitName = kitName;
        this.enabled = enabled;
        this.regenIntervalMinutes = regenIntervalMinutes;
    }

    public String getName() {
        return name;
    }

    public String getKitName() {
        return kitName;
    }

    public void setKitName(final String kitName) {
        this.kitName = kitName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public int getRegenIntervalMinutes() {
        return regenIntervalMinutes;
    }

    public void setRegenIntervalMinutes(final int regenIntervalMinutes) {
        this.regenIntervalMinutes = regenIntervalMinutes;
    }

    public boolean isNoKit() {
        if (kitName == null) return true;
        String lower = kitName.toLowerCase();
        return lower.equals("none") || lower.equals("no-kit") || lower.equals("nokit");
    }

    public void addSpawn(final Location location) {
        if (location != null) {
            spawns.add(location);
        }
    }

    public List<Location> getSpawns() {
        return spawns;
    }

    public Location getRandomSpawn() {
        if (spawns.isEmpty()) {
            return null;
        }
        return spawns.get(RANDOM.nextInt(spawns.size()));
    }

    public RegenZone getZone() {
        return zone;
    }

    public void setZone(final RegenZone zone) {
        this.zone = zone;
    }

    public boolean isResetting() {
        return resetting;
    }

    public void setResetting(final boolean resetting) {
        this.resetting = resetting;
    }
}
