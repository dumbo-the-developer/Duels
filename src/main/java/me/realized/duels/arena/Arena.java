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

package me.realized.duels.arena;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.cache.Setting;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.kit.Kit;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Arena extends BaseButton {

    @Getter
    private final String name;
    @Getter
    private final Set<UUID> players = new HashSet<>();
    @Getter
    private Map<Integer, Location> positions = new HashMap<>();
    @Getter
    @Setter
    private boolean disabled;
    @Getter
    private boolean used;
    @Getter
    private Match current;

    public Arena(final DuelsPlugin plugin, final String name) {
        super(plugin, ItemBuilder.of(Material.EMPTY_MAP).name("&e" + name).build());
        this.name = name;
    }

    public boolean isAvailable() {
        return !disabled && !used && positions.get(1) != null && positions.get(2) != null;
    }

    public void setUsed(final boolean used) {
        this.used = used;

        if (!used) {
            this.players.clear();
            this.current = null;
        }
    }

    public void setPosition(final int pos, final Location location) {
        positions.put(pos, location);
    }

    public boolean has(final UUID uuid) {
        return players.contains(uuid);
    }

    public boolean has(final Player player) {
        return has(player.getUniqueId());
    }

    public void add(final Player player) {
        players.add(player.getUniqueId());
    }

    public void remove(final Player player) {
        players.remove(player.getUniqueId());
    }

    public int size() {
        return players.size();
    }

    public UUID getFirst() {
        return players.iterator().next();
    }

    public void setMatch(final Kit kit, final Map<UUID, List<ItemStack>> items, final int bet) {
        this.current = new Match(kit, items, bet);
    }

    @Override
    public void onClick(final Player player) {
        final Setting setting = settingCache.getSafely(player);
        setting.setArena(this);
        setting.openGui(player);
        player.sendMessage(ChatColor.GREEN + "Selected Arena: " + name);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        final Arena arena = (Arena) other;
        return Objects.equals(name, arena.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
