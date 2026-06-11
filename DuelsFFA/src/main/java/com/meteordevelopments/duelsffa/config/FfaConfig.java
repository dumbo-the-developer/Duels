package com.meteordevelopments.duelsffa.config;

import com.meteordevelopments.duelsffa.FfaExtension;
import org.bukkit.configuration.file.FileConfiguration;

public class FfaConfig {

    private final String lobby;
    private final int defaultRegenIntervalMinutes;
    private final boolean preventLeaveArena;

    private final boolean weFastMode;
    private final boolean weCopyEntities;
    private final boolean weCopyBiomes;
    private final boolean pasteIgnoreAir;
    private final boolean weFlushQueue;

    private final boolean faweCleanupEnabled;
    private final String faweClipboardFolder;
    private final int faweCleanupMaxAgeHours;
    private final int faweCleanupMaxTotalMB;

    public FfaConfig(final FfaExtension extension) {
        final FileConfiguration config = extension.getConfig();
        this.lobby = config.getString("lobby", "");
        this.defaultRegenIntervalMinutes = config.getInt("default-regen-interval-minutes", 30);
        this.preventLeaveArena = config.getBoolean("prevent-leave-arena", true);

        this.weFastMode = config.getBoolean("worldedit.fast-mode", true);
        this.weCopyEntities = config.getBoolean("worldedit.copy-entities", false);
        this.weCopyBiomes = config.getBoolean("worldedit.copy-biomes", false);
        this.pasteIgnoreAir = config.getBoolean("worldedit.paste-ignore-air", false);
        this.weFlushQueue = config.getBoolean("worldedit.flush-queue", true);

        this.faweCleanupEnabled = config.getBoolean("fawe.clipboard-cleanup.enabled", false);
        this.faweClipboardFolder = config.getString("fawe.clipboard-cleanup.folder", "plugins/FastAsyncWorldEdit/clipboard");
        this.faweCleanupMaxAgeHours = config.getInt("fawe.clipboard-cleanup.max-age-hours", 24);
        this.faweCleanupMaxTotalMB = config.getInt("fawe.clipboard-cleanup.max-total-mb", 1024);
    }

    public String getLobby() {
        return lobby;
    }

    public int getDefaultRegenIntervalMinutes() {
        return defaultRegenIntervalMinutes;
    }

    public boolean isPreventLeaveArena() {
        return preventLeaveArena;
    }

    public boolean isWeFastMode() {
        return weFastMode;
    }

    public boolean isWeCopyEntities() {
        return weCopyEntities;
    }

    public boolean isWeCopyBiomes() {
        return weCopyBiomes;
    }

    public boolean isPasteIgnoreAir() {
        return pasteIgnoreAir;
    }

    public boolean isWeFlushQueue() {
        return weFlushQueue;
    }

    public boolean isFaweCleanupEnabled() {
        return faweCleanupEnabled;
    }

    public String getFaweClipboardFolder() {
        return faweClipboardFolder;
    }

    public int getFaweCleanupMaxAgeHours() {
        return faweCleanupMaxAgeHours;
    }

    public int getFaweCleanupMaxTotalMB() {
        return faweCleanupMaxTotalMB;
    }
}
