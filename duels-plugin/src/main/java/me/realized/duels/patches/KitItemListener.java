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

package me.realized.duels.patches;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.util.compat.Tags;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class KitItemListener implements Listener {

    private final ArenaManager arenaManager;

    public KitItemListener(final DuelsPlugin plugin) {
        this.arenaManager = plugin.getArenaManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void on(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();

        if (arenaManager.isInMatch(player)) {
            return;
        }

        ItemStack item = event.getCurrentItem();

        if (item == null || item.getType() == Material.AIR || Tags.hasNoKey(item, "KitItem")) {
            return;
        }

        event.setCurrentItem(null);
    }

    @EventHandler
    public void on(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (!event.hasItem() || arenaManager.isInMatch(player)) {
            return;
        }

        final ItemStack item = event.getItem();

        if (item.getType() == Material.AIR || Tags.hasNoKey(item, "KitItem")) {
            return;
        }

        event.setCancelled(true);
        player.getInventory().remove(item);
    }

    @EventHandler
    public void on(final PlayerPickupItemEvent event) {
        if (arenaManager.isInMatch(event.getPlayer())) {
            return;
        }

        final Item item = event.getItem();

        if (item.getItemStack().getType() == Material.AIR || Tags.hasNoKey(item.getItemStack(), "KitItem")) {
            return;
        }

        event.setCancelled(true);
        item.remove();
    }
}
