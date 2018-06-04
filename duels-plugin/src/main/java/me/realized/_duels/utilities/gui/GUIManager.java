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

package me.realized._duels.utilities.gui;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public class GUIManager implements Listener {

    private List<GUI> registered = new ArrayList<>();

    public GUIManager(JavaPlugin instance) {
        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    public void register(GUI gui) {
        registered.add(gui);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory top = player.getOpenInventory().getTopInventory();
        Inventory clicked = event.getClickedInventory();

        if (top == null || clicked == null) {
            return;
        }

        for (GUI gui : registered) {
            if (!gui.isPage(top)) {
                continue;
            }

            event.setCancelled(true);

            if (!clicked.equals(top)) {
                continue;
            }

            gui.getListener().onClick(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(InventoryCloseEvent event) {
        for (GUI gui : registered) {
            if (gui.isPage(event.getInventory())) {
                gui.getListener().onClose(event);
            }
        }
    }
}
