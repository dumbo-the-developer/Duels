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

package me.realized.duels.gui.setting.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.cache.Setting;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class KitSelectButton extends BaseButton {

    private static final String LORE_TEMPLATE = "&7Selected Kit: &9%s";

    public KitSelectButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Material.DIAMOND_SWORD).name("&eKit Selection").build());
    }

    @Override
    public void update(final Player player) {
        if (config.isUseOwnInventoryEnabled()) {
            setLore("&cThis option is currently", "&cunavailable. Your inventory", "&cwill be used instead", "&cin the duel.");
            return;
        }

        final Setting setting = settingCache.getSafely(player);
        setLore(String.format(LORE_TEMPLATE, setting.getKit() != null ? setting.getKit().getName() : "Random"));
    }

    @Override
    public void onClick(final Player player) {
        if (config.isUseOwnInventoryEnabled()) {
            player.sendMessage(ChatColor.RED + "This option is currently unavailable. Your inventory will be used instead in the duel.");
            return;
        }

        kitManager.getGui().open(player);
    }
}
