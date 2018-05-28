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

package me.realized.duels.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Lang;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.profile.ProfileUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class UserDataManager implements Loadable, Listener {

    // TODO: 23/05/2018 Find a way to load all userdata to a cache to support command/sign/hologram leaderboard
    // TODO: 27/05/2018 Use ConcurrentHashMap
    private final DuelsPlugin plugin;
    private final Lang lang;
    private final File folder;
    private final Map<UUID, UserData> users = new ConcurrentHashMap<>();

    @Getter
    private volatile boolean loaded;

    public UserDataManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLang();
        this.folder = new File(plugin.getDataFolder(), "users");

        if (!folder.exists()) {
            folder.mkdir();
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() {
        plugin.doAsync(() -> {
            final File[] files = folder.listFiles();

            if (files != null && files.length > 0) {
                for (final File file : files) {
                    final String fileName = file.getName();

                    if (!fileName.endsWith(".yml")) {
                        continue;
                    }

                    final String name = fileName.substring(0, fileName.length() - 4);

                    if (!ProfileUtil.isUUID(name)) {
                        continue;
                    }

                    final UUID uuid = UUID.fromString(name);

                    if (users.containsKey(uuid)) {
                        continue;
                    }

                    try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
                        users.put(uuid, plugin.getGson().fromJson(reader, UserData.class));
                    } catch (IOException ex) {
                        plugin.getLogManager().log(Level.SEVERE, "Failed to load data of " + uuid + ": " + ex.getMessage());
                    }
                }
            }

            loaded = true;
        });
    }

    @Override
    public void handleUnload() {
        loaded = false;
        saveUsers(Bukkit.getOnlinePlayers());
        users.clear();
    }

    public Optional<UserData> get(final UUID uuid) {
        return Optional.ofNullable(users.get(uuid));
    }

    public Optional<UserData> get(final Player player) {
        return get(player.getUniqueId());
    }

    private void saveUsers(final Collection<? extends Player> players) {
        for (final Player player : players) {
            Optional.ofNullable(users.remove(player.getUniqueId())).ifPresent(data -> trySave(player, data));
        }
    }

    private Optional<UserData> tryLoad(final Player player) {
        final File file = new File(folder, player.getUniqueId() + ".json");

        if (!file.exists()) {
            return Optional.of(new UserData(player));
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
            final UserData data = plugin.getGson().fromJson(reader, UserData.class);

            if (!player.getName().equals(data.getName())) {
                data.setName(player.getName());
            }

            return Optional.of(data);
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.error("An error occured while loading userdata of " + player.getName() + ": " + ex.getMessage());
        }

        return Optional.empty();
    }

    private void trySave(final Player player, final UserData data) {
        final File file = new File(folder, player.getUniqueId() + ".json");

        if (!file.exists()) {
            try {
                file.createNewFile();

                try (Writer writer = new OutputStreamWriter(new FileOutputStream(file))) {
                    plugin.getGson().toJson(data, writer);
                    writer.flush();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                Log.error("An error occured while saving userdata of " + player.getName() + ": " + ex.getMessage());
            }
        }
    }

    private void loadUser(final Player player, final Consumer<Optional<UserData>> callback) {
        plugin.doAsync(() -> callback.accept(tryLoad(player)));
    }

    @EventHandler
    public void on(final PlayerJoinEvent event) {
        loadUser(event.getPlayer(), result -> {
            if (!result.isPresent()) {
                lang.sendMessage(event.getPlayer(), "ERROR.data-load-failure");
                return;
            }

            plugin.doSync(() -> users.put(event.getPlayer().getUniqueId(), result.get()));
        });
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        Optional.ofNullable(users.remove(event.getPlayer().getUniqueId())).ifPresent(data -> plugin.doAsync(() -> trySave(event.getPlayer(), data)));
    }
}
