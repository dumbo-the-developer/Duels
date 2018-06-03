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

package me.realized.duels.kit;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.setting.Setting;
import me.realized.duels.util.inventory.ItemBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Kit extends BaseButton {

    @Getter
    private final String name;
    @Getter
    private final Map<String, Map<Integer, ItemStack>> items = new HashMap<>();

    public Kit(final DuelsPlugin plugin, final String name, final ItemStack displayed) {
        super(plugin, displayed);
        this.name = name;
    }

    public Kit(final DuelsPlugin plugin, final String name, final PlayerInventory inventory) {
        this(plugin, name, ItemBuilder
            .of(Material.DIAMOND_SWORD)
            .name("&7&l" + name)
            .lore("&aClick to send", "&aa duel request", "&awith this kit!")
            .build());

        final Map<Integer, ItemStack> contents = new HashMap<>();

        for (int i = 0; i < inventory.getSize(); i++) {
            final ItemStack item = inventory.getItem(i);

            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            contents.put(i, item.clone());
        }

        items.put("INVENTORY", contents);

        final Map<Integer, ItemStack> armorContents = new HashMap<>();

        for (int i = inventory.getArmorContents().length - 1; i >= 0; i--) {
            final ItemStack item = inventory.getArmorContents()[i];

            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            armorContents.put(4 - i, inventory.getArmorContents()[i].clone());
        }

        items.put("ARMOR", armorContents);
    }

    public void equip(final Player player) {
        for (final Map.Entry<Integer, ItemStack> entry : items.get("INVENTORY").entrySet()) {
            player.getInventory().setItem(entry.getKey(), entry.getValue().clone());
        }

        final ItemStack[] armor = items.get("ARMOR").values().toArray(new ItemStack[4]);
        ArrayUtils.reverse(armor);
        player.getInventory().setArmorContents(armor);
        player.updateInventory();
    }

    @Override
    public void onClick(final Player player) {
        final Setting setting = settingManager.getSafely(player);
        setting.setKit(this);
        setting.openGui(player);
        player.sendMessage(ChatColor.GREEN + "Selected Kit: " + name);
    }
}
