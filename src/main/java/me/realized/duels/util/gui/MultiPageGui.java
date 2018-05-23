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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import me.realized.duels.util.inventory.InventoryBuilder;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class MultiPageGui<P extends JavaPlugin> extends AbstractGui<P> {

    private final String title;
    private final int size, prevPageSlot, nextPageSlot;
    private final Collection<? extends Button> buttons;
    private final List<Page> pages = new ArrayList<>();

    public MultiPageGui(final P plugin, final String title, final int rows, final Collection<? extends Button> buttons) {
        super(plugin);
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("title cannot be null or empty");
        }

        this.title = title;

        if (rows <= 0 || rows > 5) {
            throw new IllegalArgumentException("rows out of range, must be between 1 - 5");
        }

        this.size = rows * 9 + 9;
        this.prevPageSlot = size - 9;
        this.nextPageSlot = size - 1;

        if (buttons == null) {
            throw new IllegalArgumentException("buttons cannot be null");
        }

        this.buttons = buttons;
    }

    public void calculatePages() {
        pages.clear();

        final ItemStack prevPage = ItemBuilder.of(Material.PAPER).name("&aPrevious Page").build();
        final ItemStack nextPage = ItemBuilder.of(Material.PAPER).name("&aNext Page").build();
        final int maxSize = size - 9;
        final int pages = buttons.size() / maxSize + (buttons.size() % maxSize > 0 ? 1 : 0);

        if (pages == 0) {
            this.pages.add(new Page(
                InventoryBuilder
                    .of(title, 18)
                    .set(4, ItemBuilder.of(Material.REDSTONE_BLOCK).name("&cThis page is empty.").build())
                    .fillRange(9, 18, ItemBuilder.of(Material.STAINED_GLASS_PANE).name(" ").build()).build())
            );
            return;
        }

        int i = 0;
        int pageNum = 1;
        int slot = 0;
        Page page = null;

        for (final Button button : buttons) {
            if (i % maxSize == 0) {
                final Page prev = page;
                page = new Page(
                    InventoryBuilder.of(title + " (" + pageNum + "/" + pages + ")", size)
                        .fillRange(prevPageSlot, nextPageSlot + 1, ItemBuilder.of(Material.STAINED_GLASS_PANE).name(" ").build()).build()
                );

                if (prev != null) {
                    page.inventory.setItem(prevPageSlot, prevPage);
                    page.previous = prev;
                    prev.inventory.setItem(nextPageSlot, nextPage);
                    prev.next = page;
                }

                this.pages.add(page);
                slot = 0;
                pageNum++;
            }

            set(page.inventory, slot, button);
            i++;
            slot++;
        }
    }

    @Override
    public void open(final Player... players) {
        for (final Player player : players) {
            player.openInventory(pages.get(0).inventory);
        }
    }

    @Override
    public boolean isPart(final Inventory inventory) {
        return pages.stream().anyMatch(page -> page.inventory.equals(inventory));
    }

    @Override
    public void on(final Player player, final Inventory top, final InventoryClickEvent event) {
        final Inventory clicked = event.getClickedInventory();

        if (clicked == null) {
            return;
        }

        final ItemStack item = event.getCurrentItem();

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        event.setCancelled(true);

        if (!clicked.equals(top)) {
            return;
        }

        final Optional<Page> result = pages.stream().filter(page -> page.inventory.equals(clicked)).findFirst();

        if (!result.isPresent()) {
            return;
        }

        final Page page = result.get();
        final int slot = event.getSlot();

        if (slot == nextPageSlot && page.next != null) {
            player.openInventory(page.next.inventory);
        } else if (slot == prevPageSlot && page.previous != null) {
            player.openInventory(page.previous.inventory);
        } else {
            final Optional<Button> cached = get(clicked, slot);

            if (!cached.isPresent()) {
                return;
            }

            cached.get().onClick(player);
        }
    }

    private class Page {

        private final Inventory inventory;
        private Page previous, next;

        private Page(final Inventory inventory) {
            this.inventory = inventory;
        }
    }
}
