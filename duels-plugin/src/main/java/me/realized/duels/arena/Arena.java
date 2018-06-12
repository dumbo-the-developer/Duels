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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.arena.ArenaSetPositionEvent;
import me.realized.duels.api.event.arena.ArenaStateChangeEvent;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.kit.Kit;
import me.realized.duels.setting.Setting;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Arena extends BaseButton implements me.realized.duels.api.arena.Arena {

    @Getter
    private final String name;
    @Getter
    private Map<Integer, Location> positions = new HashMap<>();
    @Getter
    private boolean disabled;
    @Getter
    private Match match;
    @Getter(value = AccessLevel.PACKAGE)
    @Setter(value = AccessLevel.PACKAGE)
    private Countdown countdown;

    public Arena(final DuelsPlugin plugin, final String name) {
        super(plugin, ItemBuilder.of(Material.EMPTY_MAP).name("&e" + name).build());
        this.name = name;
    }

    @Nullable
    @Override
    public Location getPosition(final int pos) {
        return positions.get(pos);
    }

    @Override
    public void setPosition(@Nullable final CommandSender source, final int pos, @Nonnull final Location location) {
        final ArenaSetPositionEvent event = new ArenaSetPositionEvent(source, this, pos, location);
        plugin.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        positions.put(pos, location);
    }

    @Override
    public void setPosition(final int pos, @Nonnull final Location location) {
        setPosition(null, pos, location);
    }

    @Override
    public void setDisabled(@Nullable final CommandSender source, final boolean disabled) {
        final ArenaStateChangeEvent event = new ArenaStateChangeEvent(source, this, disabled);
        plugin.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        this.disabled = event.isDisabled();
    }

    @Override
    public void setDisabled(final boolean disabled) {
        setDisabled(null, disabled);
    }

    @Override
    public boolean isUsed() {
        return match != null;
    }

    public boolean isAvailable() {
        return !disabled && !isUsed() && positions.get(1) != null && positions.get(2) != null;
    }

    public void startMatch(final Kit kit, final Map<UUID, List<ItemStack>> items, final int bet) {
        this.match = new Match(kit, items, bet);
    }

    public void endMatch() {
        match = null;
    }

    public void startCountdown() {
        final List<String> messages = config.getCdMessages();

        if (messages.isEmpty()) {
            return;
        }

        this.countdown = new Countdown(this, messages);
        countdown.runTaskTimer(plugin, 0L, 20L);
    }

    boolean isCounting() {
        return countdown != null;
    }

    @Override
    public boolean has(@Nonnull final Player player) {
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

    @Override
    public void onClick(final Player player) {
        final Setting setting = settingManager.getSafely(player);
        setting.setArena(this);
        setting.openGui(player);
        lang.sendMessage(player, "DUEL.on-select.arena", "arena", name);
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
