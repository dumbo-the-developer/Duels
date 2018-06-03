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

package me.realized.duels.teleport;

import java.util.function.Consumer;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.hooks.EssentialsHook;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.metadata.MetadataUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Handles force teleporting of players.
 */
public final class Teleport implements Loadable, Listener {

    private static final String METADATA_KEY = Teleport.class.getSimpleName();

    private final DuelsPlugin plugin;

    private EssentialsHook essentials;

    public Teleport(final DuelsPlugin plugin) {
        this.plugin = plugin;
        plugin.doSyncAfter(() -> plugin.getServer().getPluginManager().registerEvents(this, plugin), 1L);
    }

    @Override
    public void handleLoad() {
        this.essentials = plugin.getHookManager().getHook(EssentialsHook.class);
    }

    @Override
    public void handleUnload() {}

    public void tryTeleport(final Player player, final Location location, final Consumer<Player> failHandler) {
        if (location == null || location.getWorld() == null) {
            Log.error("Could not teleport " + player.getName() + "! Location is null");

            if (failHandler != null) {
                failHandler.accept(player);
            }
            return;
        }

        if (essentials != null) {
            essentials.setBackLocation(player, location);
        }

        final Chunk chunk = location.getChunk();

        if (!chunk.isLoaded()) {
            chunk.load();
        }

        MetadataUtil.put(plugin, player, METADATA_KEY, location.clone());

        if (!player.teleport(location)) {
            Log.error("Could not teleport " + player.getName() + "! Player is dead or is vehicle");

            if (failHandler != null) {
                failHandler.accept(player);
            }
        }
    }

    public void tryTeleport(final Player player, final Location location) {
        tryTeleport(player, location, null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(final PlayerTeleportEvent event) {
        final Player player = event.getPlayer();

        if (!event.isCancelled()) {
            MetadataUtil.remove(plugin, player, METADATA_KEY);
            return;
        }

        final Object value = MetadataUtil.removeAndGet(plugin, player, METADATA_KEY);

        if (value != null) {
            event.setCancelled(false);
            event.setTo((Location) value);
        }
    }
}
