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

package me.realized.duels.gui.betting.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.setting.Setting;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class DetailsButton extends BaseButton {

    private static final String[] LORE_TEMPLATE = {"&7Kit: &9%s", "&7Arena: &9%s", "&7Bet: &6%s"};

    private final Setting setting;

    public DetailsButton(final DuelsPlugin plugin, final Setting setting) {
        super(plugin, ItemBuilder.of(Material.SIGN).name("&eMatch Details").build());
        this.setting = setting;
    }

    @Override
    public void update(final Player player) {
        final String[] lore = LORE_TEMPLATE.clone();
        lore[0] = String.format(lore[0], setting.getKit() != null ? setting.getKit().getName() : "Random");
        lore[1] = String.format(lore[1], setting.getArena() != null ? setting.getArena().getName() : "Random");
        lore[2] = String.format(lore[2], "$" + setting.getBet());
        setLore(lore);
    }
}
