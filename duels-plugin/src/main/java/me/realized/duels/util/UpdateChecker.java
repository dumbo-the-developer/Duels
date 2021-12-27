package me.realized.duels.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.function.BiConsumer;
import org.bukkit.plugin.Plugin;

public final class UpdateChecker {

    private static final String API_URL = "https://api.spigotmc.org/legacy/update.php?resource=%s";

    private final Plugin plugin;
    private final int id;

    public UpdateChecker(final Plugin plugin, final int id) {
        this.plugin = plugin;
        this.id = id;
    }

    public void check(final BiConsumer<Boolean, String> callback) {
        final String currentVersion = plugin.getDescription().getVersion();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(String.format(API_URL, id)).openStream()))) {
                final String latestVersion = reader.readLine();

                if (latestVersion == null) {
                    return;
                }

                final boolean updateAvailable = NumberUtil.isLower(currentVersion, latestVersion);
                callback.accept(updateAvailable, updateAvailable ? latestVersion : currentVersion);
            } catch (IOException ignored) {}
        });
    }
}
