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

package me.realized.duels.gui.setting;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.setting.buttons.ArenaSelectButton;
import me.realized.duels.gui.setting.buttons.CancelButton;
import me.realized.duels.gui.setting.buttons.ItemBettingButton;
import me.realized.duels.gui.setting.buttons.KitSelectButton;
import me.realized.duels.gui.setting.buttons.RequestDetailsButton;
import me.realized.duels.gui.setting.buttons.RequestSendButton;
import me.realized.duels.util.gui.SinglePageGui;
import me.realized.duels.util.inventory.ItemBuilder;
import me.realized.duels.util.inventory.Slots;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SettingGui extends SinglePageGui<DuelsPlugin> {

    public SettingGui(final DuelsPlugin plugin) {
        super(plugin, "Request Settings", 3);

        final ItemStack spacing = ItemBuilder.of(Material.STAINED_GLASS_PANE).name(" ").build();
        Slots.run(2, 7, slot -> inventory.setItem(slot, spacing));
        Slots.run(11, 16, slot -> inventory.setItem(slot, spacing));
        Slots.run(20, 25, slot -> inventory.setItem(slot, spacing));

        set(4, new RequestDetailsButton(plugin));
        set(12, new KitSelectButton(plugin));
        set(13, new ArenaSelectButton(plugin));
        set(14, new ItemBettingButton(plugin));
        set(0, 2, 3, new RequestSendButton(plugin));
        set(7, 9, 3, new CancelButton(plugin));
    }
}
