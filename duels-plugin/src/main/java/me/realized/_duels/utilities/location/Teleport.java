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

package me.realized._duels.utilities.location;

import java.util.logging.Level;
import me.realized._duels.Core;
import me.realized._duels.configuration.MainConfig;
import me.realized._duels.utilities.Helper;
import me.realized._duels.utilities.Storage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class Teleport implements Listener {

    private final Core instance;
    private final MainConfig config;

    public Teleport(Core instance) {
        this.instance = instance;
        this.config = instance.getConfiguration();
        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    public boolean isAuthorizedFor(Player base, Location to) {
        if (to == null || to.getWorld() == null) {
            instance.logToFile(getClass(), base.getUniqueId() + " (" + base.getName() + ") is not authorized to teleport to " + to + "!", Level.WARNING);
            return false;
        }

        Helper.refreshChunk(to);
        Helper.updatePosition(base, to);

        if (config.isPatchesForceAllowTeleportation()) {
            return true;
        }

        PlayerTeleportEvent event = new PlayerTeleportEvent(base, base.getLocation(), to, PlayerTeleportEvent.TeleportCause.PLUGIN);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public void teleportPlayer(Player base, Location to) {
        if (config.isPatchesForceAllowTeleportation()) {
            Storage.get(base).set("teleportedTo", to);
        }

        base.teleport(to);

        if (!base.getLocation().equals(to)) {
            instance.logToFile(this, base.getUniqueId() + " (" + base.getName() + ") failed to teleport to " + to + "!", Level.WARNING);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(PlayerTeleportEvent event) {
        if (!config.isPatchesForceAllowTeleportation()) {
            return;
        }

        Storage storage = Storage.get(event.getPlayer());
        Object value = storage.get("teleportedTo");

        if (value == null) {
            return;
        }

        storage.remove("teleportedTo");
        event.setTo((Location) value);
        event.setCancelled(false);
    }
}
