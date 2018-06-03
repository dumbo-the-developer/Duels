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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.kit.Kit;
import me.realized.duels.setting.Setting;
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
    private Map<Integer, Location> positions = new HashMap<>();
    @Getter
    @Setter
    private boolean disabled;
    @Getter
    private Match match;

    public Arena(final DuelsPlugin plugin, final String name) {
        super(plugin, ItemBuilder.of(Material.EMPTY_MAP).name("&e" + name).build());
        this.name = name;
    }

    public boolean isAvailable() {
        return !disabled && !isUsed() && positions.get(1) != null && positions.get(2) != null;
    }

    public boolean isUsed() {
        return match != null;
    }

    public void startMatch(final Kit kit, final Map<UUID, List<ItemStack>> items, final int bet) {
        this.match = new Match(kit, items, bet);
    }

    public void endMatch() {
        match = null;
    }

    public boolean has(final Player player) {
        return isUsed() && !match.getPlayerMap().getOrDefault(player, true);
    }

    public void add(final Player player) {
        if (isUsed()) {
            match.getPlayerMap().put(player, false);
        }
    }

    public void remove(final Player player) {
        if (isUsed() && match.getPlayerMap().containsKey(player)) {
            match.getPlayerMap().put(player, true);
        }
    }

    public int size() {
        return isUsed() ? (int) match.getPlayerMap().entrySet().stream().filter(entry -> !entry.getValue()).count() : 0;
    }

    public Player first() {
        return isUsed() ? match.getPlayerMap().entrySet().stream().filter(entry -> !entry.getValue()).findFirst().map(Entry::getKey).orElse(null) : null;
    }

    public Set<Player> getPlayers() {
        return isUsed() ? match.getPlayers() : Collections.emptySet();
    }

    public void setPosition(final int pos, final Location location) {
        positions.put(pos, location);
    }

    @Override
    public void onClick(final Player player) {
        final Setting setting = settingManager.getSafely(player);
        setting.setArena(this);
        setting.openGui(player);
        // todo: replace to config message
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
