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

package me.realized.duels.data;

import java.util.HashMap;
import java.util.Map;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.kit.Kit;
import org.bukkit.inventory.ItemStack;

public class KitData {

    private final String name;
    private final ItemData displayed;
    private final Map<String, Map<Integer, ItemData>> items = new HashMap<>();

    public KitData(final Kit kit) {
        this.name = kit.getName();
        this.displayed = new ItemData(kit.getDisplayed());

        for (final Map.Entry<String, Map<Integer, ItemStack>> entry : kit.getItems().entrySet()) {
            final Map<Integer, ItemData> data = new HashMap<>();
            entry.getValue().forEach(((slot, item) -> data.put(slot, new ItemData(item))));
            items.put(entry.getKey(), data);
        }
    }

    public Kit toKit(final DuelsPlugin plugin) {
        final Kit kit = new Kit(plugin, name, displayed.toItemStack());

        for (final Map.Entry<String, Map<Integer, ItemData>> entry : items.entrySet()) {
            final Map<Integer, ItemStack> data = new HashMap<>();
            entry.getValue().forEach(((slot, itemData) -> data.put(slot, itemData.toItemStack())));
            kit.getItems().put(entry.getKey(), data);
        }

        return kit;
    }
}
