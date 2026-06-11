package com.meteordevelopments.duelsffa;

import com.meteordevelopments.duels.api.extension.DuelsExtension;
import com.meteordevelopments.duelsffa.arena.FfaArenaManager;
import com.meteordevelopments.duelsffa.arena.FfaPlayerManager;
import com.meteordevelopments.duelsffa.command.CommandRegistrar;
import com.meteordevelopments.duelsffa.command.FfaCommand;
import com.meteordevelopments.duelsffa.command.FfaCommandFallbackListener;
import com.meteordevelopments.duelsffa.config.FfaConfig;
import com.meteordevelopments.duelsffa.config.Lang;
import com.meteordevelopments.duelsffa.listener.FfaListener;
import com.meteordevelopments.duelsffa.selection.SelectionManager;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public final class FfaExtension extends DuelsExtension {

    private FfaConfig configuration;
    private Lang lang;
    private SelectionManager selectionManager;
    private FfaArenaManager arenaManager;
    private FfaPlayerManager playerManager;
    private CommandRegistrar commandRegistrar;
    private FfaCommand command;

    @Override
    public void onEnable() {
        this.configuration = new FfaConfig(this);
        this.lang = new Lang(this);
        this.selectionManager = new SelectionManager(this);
        this.playerManager = new FfaPlayerManager(this);
        this.arenaManager = new FfaArenaManager(this);

        api.registerListener(new FfaListener(this));

        this.commandRegistrar = new CommandRegistrar(api);
        this.command = new FfaCommand(this);
        if (!commandRegistrar.register(command)) {
            api.warn("[DuelsFFA Extension] Failed to register /ffa command. Using fallback listener.");
            api.registerListener(new FfaCommandFallbackListener(command));
        }

        if (configuration.isFaweCleanupEnabled()) {
            scheduleFaweClipboardCleanup();
        }
    }

    @Override
    public void onDisable() {
        if (commandRegistrar != null && command != null) {
            commandRegistrar.unregister(command);
        }
        if (playerManager != null) {
            playerManager.shutdown();
        }
        if (arenaManager != null) {
            arenaManager.shutdown();
        }
    }

    public FfaConfig getConfiguration() {
        return configuration;
    }

    public Lang getLang() {
        return lang;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public FfaArenaManager getArenaManager() {
        return arenaManager;
    }

    public FfaPlayerManager getPlayerManager() {
        return playerManager;
    }

    private void scheduleFaweClipboardCleanup() {
        final String folderPath = configuration.getFaweClipboardFolder();
        final int maxAgeHours = configuration.getFaweCleanupMaxAgeHours();
        final long maxTotalBytes = Math.max(0, configuration.getFaweCleanupMaxTotalMB()) * 1024L * 1024L;

        api.doAsyncRepeat(() -> {
            try {
                File folder = new File(folderPath);
                if (!folder.exists() || !folder.isDirectory()) return;

                long now = System.currentTimeMillis();
                long maxAgeMs = maxAgeHours * 60L * 60L * 1000L;

                File[] files = folder.listFiles();
                if (files == null || files.length == 0) return;

                for (File f : files) {
                    if (!f.isFile()) continue;
                    if (maxAgeMs > 0 && now - f.lastModified() > maxAgeMs) {
                        f.delete();
                    }
                }

                if (maxTotalBytes <= 0) return;

                files = folder.listFiles(File::isFile);
                if (files == null || files.length == 0) return;

                long total = 0;
                for (File f : files) total += f.length();
                if (total <= maxTotalBytes) return;

                Arrays.sort(files, Comparator.comparingLong(File::lastModified));
                for (File f : files) {
                    if (total <= maxTotalBytes) break;
                    long len = f.length();
                    if (f.delete()) total -= len;
                }
            } catch (Exception ignored) {
            }
        }, 20L * 60L, 20L * 60L);
    }
}
