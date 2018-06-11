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

package me.realized.duels.player;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class PlayerInfo {

    @Getter
    private final List<ItemStack> inventory;
    private final ItemStack[] armor;
    private final Collection<PotionEffect> effects;
    private final double health;
    private final int hunger;

    @Getter
    private final Location location;

    public PlayerInfo(final Player player, final boolean inventory) {
        this.inventory = inventory ? Lists.newArrayList(player.getInventory().getContents().clone()) : Collections.emptyList();
        this.armor = inventory ? player.getInventory().getArmorContents().clone() : new ItemStack[0];
        this.effects = player.getActivePotionEffects();
        this.health = player.getHealth();
        this.hunger = player.getFoodLevel();
        this.location = player.getLocation().clone();
    }

    public void restore(final Player player) {
        player.addPotionEffects(effects);
        player.setHealth(health);
        player.setFoodLevel(hunger);

        if (armor.length > 0) {
            player.getInventory().setArmorContents(armor);
        }

        inventory.stream().filter(Objects::nonNull).forEach(item -> player.getInventory().addItem(item));
        player.updateInventory();
    }
}
