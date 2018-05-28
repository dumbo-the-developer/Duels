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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import me.realized.duels.util.CollectionUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class PlayerData {

    public static final String METADATA_KEY = PlayerData.class.getSimpleName();

    private final ItemStack[] inventory;
    private final ItemStack[] armor;
    private final Collection<PotionEffect> effects;
    private final double health;
    private final int hunger;

    @Getter
    private final Location location;

    public PlayerData(final Player player) {
        this.inventory = player.getInventory().getContents().clone();
        this.armor = player.getInventory().getArmorContents().clone();
        this.effects = player.getActivePotionEffects();
        this.health = player.getHealth();
        this.hunger = player.getFoodLevel();
        this.location = player.getLocation().clone();
    }

    private PlayerData(final ItemStack[] inventory, final ItemStack[] armor, final Collection<?> effects, final double health, final int hunger,
        final Location location) {
        this.inventory = inventory;
        this.armor = armor;
        this.effects = CollectionUtil.tryConvert(effects, PotionEffect.class);
        this.health = health;
        this.hunger = hunger;
        this.location = location;
    }

    public void restore(final Player player) {
        player.addPotionEffects(effects);
        player.setHealth(health);
        player.setFoodLevel(hunger);
        player.getInventory().setArmorContents(armor);

        for (final ItemStack item : inventory) {
            if (item != null) {
                player.getInventory().addItem(item);
            }
        }
        player.updateInventory();
    }

    public Map<String, Object> to() {
        final Map<String, Object> data = new HashMap<>();
        data.put("inventory", inventory);
        data.put("armor", armor);
        data.put("effects", effects);
        data.put("health", health);
        data.put("hunger", hunger);
        data.put("location", location);
        return data;
    }

    public static Optional<PlayerData> from(final Object value) {
        if (value == null || !(value instanceof Map)) {
            return Optional.empty();
        }

        final Map data = (Map) value;
        return Optional
            .of(new PlayerData((ItemStack[]) data.get("inventory"), (ItemStack[]) data.get("armor"), (Collection<?>) data.get("effects"), (double) data.get("health"),
                (int) data.get("hunger"), (Location) data.get("location")));
    }
}
