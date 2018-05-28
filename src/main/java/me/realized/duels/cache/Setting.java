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

package me.realized.duels.cache;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.Arena;
import me.realized.duels.gui.setting.SettingGui;
import me.realized.duels.kit.Kit;
import org.bukkit.entity.Player;

public class Setting {

    private final DuelsPlugin plugin;

    @Getter
    private UUID target;
    @Getter
    @Setter
    private Kit kit;
    @Getter
    @Setter
    private Arena arena;
    @Getter
    @Setter
    private int bet;
    @Getter
    @Setter
    private boolean itemBetting;
    private SettingGui gui;

    public Setting(final DuelsPlugin plugin) {
        this.plugin = plugin;
    }

    public void reset() {
        target = null;
        kit = null;
        arena = null;
        bet = 0;
        itemBetting = false;
    }

    public void setTarget(final Player target) {
        if (this.target != null && !this.target.equals(target.getUniqueId())) {
            reset();
        }

        this.target = target.getUniqueId();
    }

    public void updateGui(final Player player) {
        if (gui != null) {
            gui.update(player);
        }
    }

    public void openGui(final Player player) {
        (this.gui != null ? this.gui : (this.gui = plugin.getGuiListener().addGui(player, new SettingGui(plugin)))).open(player);
    }

    // Don't copy the gui since it won't be required to start a match.
    public Setting lightCopy() {
        final Setting copy = new Setting(plugin);
        copy.target = target;
        copy.kit = kit;
        copy.arena = arena;
        copy.bet = bet;
        copy.itemBetting = itemBetting;
        return copy;
    }
}
