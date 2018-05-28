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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import me.realized.duels.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Match {

    @Getter
    private final long creation;
    @Getter
    private final Map<Player, Boolean> players = new HashMap<>();
    @Getter
    private final Kit kit;
    @Getter
    private final Map<UUID, List<ItemStack>> items;
    @Getter
    private final int bet;

    Match(final Kit kit, final Map<UUID, List<ItemStack>> items, final int bet) {
        this.creation = System.currentTimeMillis();
        this.kit = kit;
        this.items = items;
        this.bet = bet;
    }

    public enum EndReason {

        OPPONENT_QUIT,
        OPPONENT_DEFEAT,
        PLUGIN_DISABLE,
        MAX_TIME_REACHED,
        OTHER
    }
}
