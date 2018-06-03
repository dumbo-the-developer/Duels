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

package me.realized.duels.util.gui;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import me.realized.duels.util.Loadable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public class GuiListener<P extends JavaPlugin> implements Loadable, Listener {

    private final Multimap<UUID, AbstractGui<P>> privateGuis = HashMultimap.create();
    private final List<AbstractGui<P>> publicGuis = new ArrayList<>();

    public GuiListener(final P plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() {}

    @Override
    public void handleUnload() {
        privateGuis.clear();
        publicGuis.clear();
    }

    public void addGui(final AbstractGui<P> gui) {
        publicGuis.add(gui);
    }

    public <T extends AbstractGui<P>> T addGui(final Player player, final T gui) {
        privateGuis.put(player.getUniqueId(), gui);
        return gui;
    }

    public void removeGui(final Player player, final AbstractGui<P> gui) {
        final Collection<AbstractGui<P>> guis = privateGuis.asMap().get(player.getUniqueId());

        if (guis != null) {
            guis.remove(gui);
        }
    }

    private List<AbstractGui<P>> get(final Player player) {
        final List<AbstractGui<P>> guis = new ArrayList<>(publicGuis);

        if (privateGuis.containsKey(player.getUniqueId())) {
            guis.addAll(privateGuis.get(player.getUniqueId()));
        }

        return guis;
    }

    @EventHandler
    public void on(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final Inventory top = player.getOpenInventory().getTopInventory();

        if (top == null) {
            return;
        }

        for (final AbstractGui<P> gui : get(player)) {
            if (gui.isPart(top)) {
                gui.on(player, top, event);
                break;
            }
        }
    }

    @EventHandler
    public void on(final InventoryDragEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final Inventory inventory = event.getInventory();

        for (final AbstractGui<P> gui : get(player)) {
            if (gui.isPart(inventory)) {
                gui.on(player, event.getRawSlots(), event);
                break;
            }
        }
    }

    @EventHandler
    public void on(final InventoryCloseEvent event) {
        final Player player = (Player) event.getPlayer();
        final Inventory inventory = event.getInventory();

        for (final AbstractGui<P> gui : get(player)) {
            if (gui.isPart(inventory)) {
                gui.on(player, event.getInventory(), event);
                break;
            }
        }
    }

    @EventHandler
    public void on(final InventoryOpenEvent event) {
        final Player player = (Player) event.getPlayer();
        final Inventory inventory = event.getInventory();

        for (final AbstractGui<P> gui : get(player)) {
            if (gui.isPart(inventory)) {
                final Collection<AbstractGui<P>> guis = privateGuis.asMap().get(event.getPlayer().getUniqueId());

                if (guis == null) {
                    return;
                }

                guis.removeIf(privateGui -> privateGui.removeIfEmpty() && !privateGui.equals(gui) && !privateGui.hasViewers());
                break;
            }
        }
    }
}
