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

import java.util.Arrays;
import java.util.function.Consumer;
import lombok.Getter;
import me.realized.duels.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Button<P extends JavaPlugin> {

    protected final P plugin;
    @Getter
    private final ItemStack displayed;

    public Button(final P plugin, final ItemStack displayed) {
        this.plugin = plugin;
        this.displayed = displayed;
    }

    private void editMeta(final Consumer<ItemMeta> consumer) {
        final ItemMeta meta = getDisplayed().getItemMeta();
        consumer.accept(meta);
        getDisplayed().setItemMeta(meta);
    }

    protected void setDisplayName(final String name) {
        editMeta(meta -> meta.setDisplayName(StringUtil.color(name)));
    }

    protected void setLore(final String... lore) {
        editMeta(meta -> meta.setLore(StringUtil.color(Arrays.asList(lore))));
    }

    protected void setOwner(final String name) {
        editMeta(meta -> {
            if (displayed.getType() == Material.SKULL_ITEM && displayed.getDurability() == 3) {
                ((SkullMeta) meta).setOwner(name);
            }
        });
    }

    public void update(final Player player) {}

    public void onClick(final Player player) {}
}
