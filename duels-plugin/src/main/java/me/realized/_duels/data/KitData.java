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

package me.realized._duels.data;

import java.util.HashMap;
import java.util.Map;
import me.realized._duels.kits.Kit;
import me.realized._duels.kits.KitManager;
import me.realized._duels.utilities.inventory.JSONItem;
import org.bukkit.inventory.ItemStack;

public class KitData {

    private final String name;
    private final JSONItem displayed;
    private final Map<KitManager.Type, Map<Integer, JSONItem>> items = new HashMap<>();

    public KitData(Kit kit) {
        this.name = kit.getName();
        this.displayed = JSONItem.fromItemStack(kit.getDisplayed());

        for (Map.Entry<KitManager.Type, Map<Integer, ItemStack>> entry : kit.getItems().entrySet()) {
            Map<Integer, JSONItem> saved = new HashMap<>();

            for (Map.Entry<Integer, ItemStack> nextEntry : entry.getValue().entrySet()) {
                saved.put(nextEntry.getKey(), JSONItem.fromItemStack(nextEntry.getValue()));
            }

            items.put(entry.getKey(), saved);
        }
    }

    public Kit toKit() {
        Map<Integer, ItemStack> inventory = new HashMap<>();

        for (Map.Entry<Integer, JSONItem> entry : items.get(KitManager.Type.INVENTORY).entrySet()) {
            inventory.put(entry.getKey(), entry.getValue().construct());
        }

        Map<Integer, ItemStack> armor = new HashMap<>();

        for (Map.Entry<Integer, JSONItem> entry : items.get(KitManager.Type.ARMOR).entrySet()) {
            armor.put(entry.getKey(), entry.getValue().construct());
        }

        Map<KitManager.Type, Map<Integer, ItemStack>> items = new HashMap<>();
        items.put(KitManager.Type.INVENTORY, inventory);
        items.put(KitManager.Type.ARMOR, armor);
        return new Kit(name, displayed.construct(), items);
    }
}
