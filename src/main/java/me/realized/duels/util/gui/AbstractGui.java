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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import me.realized.duels.util.inventory.Slots;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractGui<P extends JavaPlugin> implements Updatable {

    protected final P plugin;
    private final Map<Inventory, Map<Integer, Button<P>>> buttons = new HashMap<>();

    public AbstractGui(final P plugin) {
        this.plugin = plugin;
    }

    public abstract void open(final Player... players);

    public abstract boolean isPart(final Inventory inventory);

    public abstract boolean hasViewers();

    public boolean removeIfEmpty() {
        return false;
    }

    public abstract void on(final Player player, final Inventory top, final InventoryClickEvent event);

    public void on(final Player player, final Inventory inventory, final InventoryCloseEvent event) {}

    public void on(final Player player, final Set<Integer> rawSlots, final InventoryDragEvent event) {
        event.setCancelled(true);
    }

    public Button<P> get(final Inventory inventory, final int slot) {
        final Map<Integer, Button<P>> buttons;
        return (buttons = this.buttons.get(inventory)) != null ? buttons.get(slot) : null;
    }

    public void set(final Inventory inventory, final int slot, final Button<P> button) {
        buttons.computeIfAbsent(inventory, result -> new HashMap<>()).put(slot, button);
        inventory.setItem(slot, button.getDisplayed());
    }

    public void set(final Inventory inventory, final int from, final int to, final int height, final Button<P> button) {
        Slots.run(from, to, height, slot -> set(inventory, slot, button));
    }

    public void set(final Inventory inventory, final int from, final int to, final Button<P> button) {
        Slots.run(from, to, slot -> set(inventory, slot, button));
    }

    public void update(final Player player, final Inventory inventory, final Button<P> button) {
        final Map<Integer, Button<P>> cached = buttons.get(inventory);

        if (cached == null) {
            return;
        }

        button.update(player);

        for (final Map.Entry<Integer, Button<P>> entry : cached.entrySet()) {
            if (entry.getValue().equals(button)) {
                inventory.setItem(entry.getKey(), button.getDisplayed());
                return;
            }
        }
    }

    @Override
    public void update(final Player player) {
        buttons.forEach((inventory, data) -> data.forEach((slot, button) -> {
            button.update(player);
            inventory.setItem(slot, button.getDisplayed());
        }));
    }
}
