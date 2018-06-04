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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import me.realized._duels.utilities.Helper;
import me.realized._duels.utilities.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GUI<T> {

    private final int BUTTON_PREVIOUS;
    private final int BUTTON_NEXT;

    private final String title;
    private final int rows;
    private final GUIListener listener;
    private final Map<Inventory, Integer> pages = new LinkedHashMap<>();
    private final Map<Integer, T> data = new LinkedHashMap<>();

    public GUI(String title, List<T> entries, int rows, GUIListener listener) {
        this.title = title;

        if (rows < 1 || rows > 5) {
            rows = 5;
        }

        this.BUTTON_PREVIOUS = rows * 9;
        this.BUTTON_NEXT = rows * 9 + 8;
        this.rows = rows;
        this.listener = listener;
        update(entries);
    }

    public void update(List<T> entries) {
        entries = new ArrayList<>(entries);
        Map<Inventory, Integer> previousPages = new HashMap<>(pages);
        pages.clear();
        data.clear();

        Iterator<T> iterator = entries.iterator();

        while (iterator.hasNext()) {
            T entry = iterator.next();

            if (entry instanceof GUIItem && !((GUIItem) entry).filter()) {
                iterator.remove();
            }
        }

        int maxInvSize = rows * 9;
        int total = entries.size() / maxInvSize + (entries.size() % maxInvSize > 0 ? 1 : 0);

        if (!entries.isEmpty()) {
            ItemStack next = ItemBuilder.builder().type(Material.PAPER).name("&aNext Page").build();
            ItemStack previous = ItemBuilder.builder().type(Material.PAPER).name("&aPrevious Page").build();
            ItemStack separator = ItemBuilder.builder().type(Material.STAINED_GLASS_PANE).name(" ").build();
            ItemStack defaultDisplayed;

            if (!Helper.isPre1_8()) {
                defaultDisplayed = ItemBuilder.builder().type(Material.BARRIER).build();
            } else {
                defaultDisplayed = ItemBuilder.builder().type(Material.REDSTONE_BLOCK).build();
            }

            for (int page = 1; page <= total; page++) {
                Inventory current = Bukkit.createInventory(null, maxInvSize + 9, title + " (page " + page + "/" + total + ")");

                if (page < total) {
                    if (page != 1) {
                        current.setItem(BUTTON_PREVIOUS, previous);
                    }

                    current.setItem(BUTTON_NEXT, next);
                } else {
                    if (page != 1) {
                        current.setItem(BUTTON_PREVIOUS, previous);
                    }
                }

                for (int i = BUTTON_PREVIOUS; i <= BUTTON_NEXT; i++) {
                    if (current.getItem(i) == null) {
                        current.setItem(i, separator);
                    }
                }

                int end = page != total ? maxInvSize : (entries.size() % maxInvSize != 0 ? entries.size() % maxInvSize : maxInvSize);

                for (int start = 0; start < end; start++) {
                    T entry = entries.get(start + ((page - 1) * maxInvSize));
                    ItemStack displayed;

                    if (entry instanceof GUIItem) {
                        displayed = ((GUIItem) entry).toDisplay();
                        data.put(start + ((page - 1) * maxInvSize), entry);
                    } else {
                        displayed = defaultDisplayed;
                    }

                    current.setItem(start, displayed);
                }

                pages.put(current, page);
            }
        } else {
            Inventory current = Bukkit.createInventory(null, 54, title + " (page 1/1)");
            ItemStack defaultDisplayed;

            if (!Helper.isPre1_8()) {
                defaultDisplayed = ItemBuilder.builder().type(Material.BARRIER).name(ChatColor.RED + "There was nothing to load to the GUI.").build();
            } else {
                defaultDisplayed = ItemBuilder.builder().type(Material.REDSTONE_BLOCK).name(ChatColor.RED + "There was nothing to load to the GUI.").build();
            }

            current.setItem(22, defaultDisplayed);
            pages.put(current, 1);
        }

        for (Map.Entry<Inventory, Integer> entry : previousPages.entrySet()) {
            Iterator<HumanEntity> entityIterator = entry.getKey().getViewers().iterator();

            while (entityIterator.hasNext()) {
                HumanEntity entity = entityIterator.next();
                entityIterator.remove();

                if (entry.getValue() > total) {
                    entity.closeInventory();
                    entity.sendMessage(ChatColor.RED + "[Duels] The inventory you were looking was deleted.");
                } else {
                    for (Map.Entry<Inventory, Integer> newEntry : pages.entrySet()) {
                        if (newEntry.getValue().equals(entry.getValue())) {
                            listener.onSwitch((Player) entity, newEntry.getKey());
                            break;
                        }
                    }
                }
            }
        }
    }

    private Inventory getByPage(int page) {
        for (Map.Entry<Inventory, Integer> entry : pages.entrySet()) {
            if (entry.getValue() == page) {
                return entry.getKey();
            }
        }

        return null;
    }

    public boolean isPage(Inventory another) {
        for (Inventory inventory : pages.keySet()) {
            if (another.getTitle().equals(inventory.getTitle())) {
                return true;
            }
        }

        return false;
    }

    public T getData(Player player, Inventory inventory, int slot) {
        if (pages.get(inventory) == null) {
            return null;
        }

        int page = pages.get(inventory);

        if (slot >= BUTTON_PREVIOUS) {
            if (inventory.getItem(slot).getType() == Material.PAPER) {
                if (slot == BUTTON_NEXT) {
                    listener.onSwitch(player, getByPage(++page));
                } else if (slot == BUTTON_PREVIOUS) {
                    listener.onSwitch(player, getByPage(--page));
                }
            }

            return null;
        }

        return data.get((page - 1) * (rows * 9) + slot);
    }

    public void close(String warning) {
        for (Inventory inventory : pages.keySet()) {
            Iterator<HumanEntity> entityIterator = inventory.getViewers().iterator();

            while (entityIterator.hasNext()) {
                HumanEntity entity = entityIterator.next();
                entityIterator.remove();
                entity.closeInventory();

                if (warning != null) {
                    entity.sendMessage(ChatColor.RED + warning);
                }
            }
        }
    }

    public Inventory getFirst() {
        return pages.keySet().iterator().next();
    }

    GUIListener getListener() {
        return listener;
    }
}
