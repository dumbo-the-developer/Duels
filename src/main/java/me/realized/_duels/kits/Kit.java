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

package me.realized._duels.kits;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import me.realized._duels.utilities.gui.GUIItem;
import me.realized._duels.utilities.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Kit implements GUIItem {

    private final String name;
    private ItemStack displayed;
    private Map<KitManager.Type, Map<Integer, ItemStack>> items = new HashMap<>();

    public Kit(String name, PlayerInventory playerInventory) {
        this.name = name;
        this.displayed = ItemBuilder.builder().type(Material.DIAMOND_SWORD).name("&7&l" + name)
            .lore(Arrays.asList("&aClick to send", "&aa duel request", "&awith this kit!")).build();

        Map<Integer, ItemStack> inventory = new HashMap<>();

        for (int i = 0; i < playerInventory.getSize(); i++) {
            ItemStack item = playerInventory.getContents()[i];

            if (item == null) {
                continue;
            }

            inventory.put(i, item.clone());
        }

        Map<Integer, ItemStack> armor = new HashMap<>();

        if (playerInventory.getHelmet() != null) {
            armor.put(1, playerInventory.getHelmet().clone());
        }

        if (playerInventory.getChestplate() != null) {
            armor.put(2, playerInventory.getChestplate().clone());
        }

        if (playerInventory.getLeggings() != null) {
            armor.put(3, playerInventory.getLeggings().clone());
        }

        if (playerInventory.getBoots() != null) {
            armor.put(4, playerInventory.getBoots().clone());
        }

        items.put(KitManager.Type.INVENTORY, inventory);
        items.put(KitManager.Type.ARMOR, armor);
    }

    public Kit(String name, ItemStack displayed, Map<KitManager.Type, Map<Integer, ItemStack>> items) {
        this.name = name;
        this.displayed = displayed;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public ItemStack getDisplayed() {
        return displayed;
    }

    public void setDisplayed(ItemStack displayed) {
        this.displayed = displayed;
    }

    public Map<KitManager.Type, Map<Integer, ItemStack>> getItems() {
        return items;
    }

    public void equip(Player... players) {
        for (Player player : players) {
            for (Map.Entry<Integer, ItemStack> entry : items.get(KitManager.Type.INVENTORY).entrySet()) {
                player.getInventory().setItem(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<Integer, ItemStack> entry : items.get(KitManager.Type.ARMOR).entrySet()) {
                switch (entry.getKey()) {
                    case 1:
                        player.getInventory().setHelmet(entry.getValue());
                        break;
                    case 2:
                        player.getInventory().setChestplate(entry.getValue());
                        break;
                    case 3:
                        player.getInventory().setLeggings(entry.getValue());
                        break;
                    case 4:
                        player.getInventory().setBoots(entry.getValue());
                        break;
                }
            }
        }
    }

    @Override
    public ItemStack toDisplay() {
        return displayed;
    }

    @Override
    public boolean filter() {
        return true;
    }
}
