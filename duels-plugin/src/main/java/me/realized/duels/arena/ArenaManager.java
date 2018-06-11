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

package me.realized.duels.arena;

import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.arena.ArenaCreateEvent;
import me.realized.duels.api.event.arena.ArenaRemoveEvent;
import me.realized.duels.config.Config;
import me.realized.duels.data.ArenaData;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.gui.MultiPageGui;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.projectiles.ProjectileSource;

public class ArenaManager implements Loadable, me.realized.duels.api.arena.ArenaManager, Listener {

    private final DuelsPlugin plugin;
    private final Config config;
    private final File file;
    @Getter
    private final List<Arena> arenas = new ArrayList<>();
    @Getter
    private final MultiPageGui<DuelsPlugin> gui;

    public ArenaManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.file = new File(plugin.getDataFolder(), "arenas.json");
        // TODO: 03/06/2018 Replace to config message vv
        gui = new MultiPageGui<>(plugin, "Arena Selection", 1, arenas);
        plugin.getGuiListener().addGui(gui);
    }

    @Override
    public void handleLoad() throws IOException {
        if (config.isCdEnabled()) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        if (!file.exists()) {
            file.createNewFile();
            return;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
            final List<ArenaData> data = plugin.getGson().fromJson(reader, new TypeToken<List<ArenaData>>() {}.getType());

            if (data != null) {
                data.forEach(arenaData -> arenas.add(arenaData.toArena(plugin)));
            }
        }

        Log.info(this, "Loaded " + arenas.size() + " arena(s).");
        gui.calculatePages();
    }

    @Override
    public void handleUnload() throws IOException {
        if (arenas.isEmpty()) {
            return;
        }

        final List<ArenaData> data = new ArrayList<>();

        for (final Arena arena : arenas) {
            data.add(new ArenaData(arena));
        }

        if (!file.exists()) {
            file.createNewFile();
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file))) {
            writer.write(plugin.getGson().toJson(data));
            writer.flush();
        }

        arenas.clear();
    }

    @Nullable
    @Override
    public Arena get(@Nonnull final String name) {
        return arenas.stream().filter(arena -> arena.getName().equals(name)).findFirst().orElse(null);
    }

    @Nullable
    @Override
    public Arena get(@Nonnull final Player player) {
        return arenas.stream().filter(arena -> arena.has(player)).findFirst().orElse(null);
    }

    public boolean remove(final CommandSender source, final String name) {
        return arenas.removeIf(arena -> {
            if (arena.getName().equals(name)) {
                final ArenaRemoveEvent event = new ArenaRemoveEvent(source, arena);
                plugin.getServer().getPluginManager().callEvent(event);
                return true;
            }

            return false;
        });
    }

    public void create(final CommandSender source, final String name) {
        final Arena arena = new Arena(plugin, name);
        arenas.add(arena);
        final ArenaCreateEvent event = new ArenaCreateEvent(source, arena);
        plugin.getServer().getPluginManager().callEvent(event);
    }

    @Override
    public boolean isInMatch(@Nonnull final Player player) {
        return get(player) != null;
    }

    public Set<Player> getPlayers() {
        return arenas.stream().flatMap(arena -> arena.getPlayers().stream()).collect(Collectors.toSet());
    }

    public Arena randomArena() {
        final List<Arena> available = arenas.stream().filter(Arena::isAvailable).collect(Collectors.toList());

        if (available.isEmpty()) {
            return null;
        }

        return available.get(ThreadLocalRandom.current().nextInt(available.size()));
    }

    @EventHandler(ignoreCancelled = true)
    public void on(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Arena arena = get((Player) event.getEntity());

        if (arena == null || !arena.isCounting()) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void on(final ProjectileLaunchEvent event) {
        final ProjectileSource shooter = event.getEntity().getShooter();

        if (shooter == null || !(shooter instanceof Player)) {
            return;
        }

        final Arena arena = get((Player) shooter);

        if (arena == null || !arena.isCounting()) {
            return;
        }

        event.setCancelled(true);
        // TODO: 06/06/2018 add message
    }

    @EventHandler(ignoreCancelled = true)
    public void on(final PlayerMoveEvent event) {
        final Location from = event.getFrom();
        final Location to = event.getTo();

        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        final Arena arena = get(event.getPlayer());

        if (arena == null || !arena.isCounting()) {
            return;
        }

        event.setTo(event.getFrom());
    }
}
