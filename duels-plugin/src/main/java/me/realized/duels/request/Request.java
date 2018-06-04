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

package me.realized.duels.request;

import java.util.UUID;
import javax.annotation.Nullable;
import lombok.Getter;
import me.realized.duels.api.arena.Arena;
import me.realized.duels.api.kit.Kit;
import me.realized.duels.setting.Setting;
import org.bukkit.entity.Player;

public class Request implements me.realized.duels.api.request.Request {

    @Getter
    private final UUID sender;
    @Getter
    private final UUID target;
    @Getter
    private final Setting setting;
    @Getter
    private final long creation;

    Request(final Player sender, final Player target, final Setting setting) {
        this.sender = sender.getUniqueId();
        this.target = target.getUniqueId();
        this.setting = setting.lightCopy();
        this.creation = System.currentTimeMillis();
    }

    @Nullable
    @Override
    public Kit getKit() {
        return setting.getKit();
    }

    @Nullable
    @Override
    public Arena getArena() {
        return setting.getArena();
    }

    @Override
    public boolean canBetItems() {
        return setting.isItemBetting();
    }

    @Override
    public int getBet() {
        return setting.getBet();
    }
}
